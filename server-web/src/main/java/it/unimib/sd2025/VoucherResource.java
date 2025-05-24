package it.unimib.sd2025;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * API REST per la gestione dei buoni.
 */
@Path("api/vouchers")
public class VoucherResource {

    private static final ConcurrentHashMap<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    private ReentrantLock getUserLock(String userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    /**
     * Crea un nuovo buono.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVoucher(CreateVoucherRequest request) {
        ReentrantLock userLock = getUserLock(request.getUserId());
        userLock.lock();
        try {
            // Recupera il contributo dell'utente
            String contributionJson = DatabaseClient.get("contribution:" + request.getUserId());
            if (contributionJson == null || contributionJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("User contribution not found").build();
            }

            UserContribution contribution = JsonbBuilder.create().fromJson(contributionJson, UserContribution.class);

            // Verifica se c'è abbastanza contributo disponibile
            if (contribution.getAvailable() < request.getAmount()) {
                String errorMsg = String.format(
                    "Importo richiesto troppo alto. Disponibile: %.2f, richiesto: %.2f",
                    contribution.getAvailable(), request.getAmount()
                );
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"" + errorMsg + "\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }

            // Crea il nuovo buono
            String voucherId = UUID.randomUUID().toString();
            Voucher voucher = new Voucher(
                    voucherId,
                    request.getAmount(),
                    request.getCategory(),
                    "generated",
                    LocalDateTime.now().format(formatter),
                    null,
                    request.getUserId());

            // Aggiorna il contributo dell'utente
            contribution.setAvailable(contribution.getAvailable() - request.getAmount());
            contribution.setAllocated(contribution.getAllocated() + request.getAmount());
            DatabaseClient.set("contribution:" + request.getUserId(), JsonbBuilder.create().toJson(contribution));

            // Salva il buono nel database
            DatabaseClient.set("voucher:" + voucherId, JsonbBuilder.create().toJson(voucher));

            // Aggiorna l'indice dei buoni dell'utente
            String vouchersCountStr = DatabaseClient.get("vouchersCount:" + request.getUserId());
            int vouchersCount = vouchersCountStr != null && !vouchersCountStr.equals("null")
                    ? Integer.parseInt(vouchersCountStr)
                    : 0;

            DatabaseClient.set("voucherIdByIndex:" + request.getUserId() + ":" + vouchersCount, voucherId);
            DatabaseClient.set("vouchersCount:" + request.getUserId(), String.valueOf(vouchersCount + 1));

            // Aggiorna le statistiche globali
            updateGlobalStats(false, request.getAmount());

            return Response.created(URI.create("/api/vouchers/" + voucherId)).entity(voucher).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        } finally {
            userLock.unlock();
        }
    }

    /**
     * Aggiorna un buono esistente.
     */
    @PUT
    @Path("/{voucherId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVoucher(@PathParam("voucherId") String voucherId, UpdateVoucherRequest request) {
        String initialVoucherJson;
        Voucher initialVoucher;
        try {
            // Initial fetch to get userId
            initialVoucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (initialVoucherJson == null || initialVoucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found (initial fetch)").build();
            }
            initialVoucher = JsonbBuilder.create().fromJson(initialVoucherJson, Voucher.class);
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (initial fetch): " + e.getMessage())
                    .build();
        }

        ReentrantLock userLock = getUserLock(initialVoucher.getUserId()); 
        userLock.lock();
        try {
            
            String voucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);

            // Verifica che il buono non sia già stato consumato
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Cannot update a consumed voucher")
                        .build();
            }

            // Aggiorna la categoria del buono
            voucher.setCategory(request.getCategory());

            // Salva il buono aggiornato
            DatabaseClient.set("voucher:" + voucherId, JsonbBuilder.create().toJson(voucher));

            return Response.ok(voucher).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (inside lock): " + e.getMessage())
                    .build();
        } finally {
            userLock.unlock();
        }
    }

    /**
     * Segna un buono come consumato.
     */
    @POST
    @Path("/{voucherId}/consume")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeVoucher(@PathParam("voucherId") String voucherId) {
        String initialVoucherJson;
        Voucher initialVoucher;
        try {
           
            initialVoucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (initialVoucherJson == null || initialVoucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found (initial fetch)").build();
            }
            initialVoucher = JsonbBuilder.create().fromJson(initialVoucherJson, Voucher.class);
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (initial fetch): " + e.getMessage())
                    .build();
        }

        ReentrantLock userLock = getUserLock(initialVoucher.getUserId());
        userLock.lock();
        try {
            
            String voucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);

            // Verifica che il buono non sia già stato consumato
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Voucher already consumed")
                        .build();
            }

            // Aggiorna lo stato del buono
            voucher.setStatus("consumed");
            voucher.setConsumedAt(LocalDateTime.now().format(formatter));

            // Salva il buono aggiornato
            DatabaseClient.set("voucher:" + voucherId, JsonbBuilder.create().toJson(voucher));

            // Aggiorna il contributo dell'utente
            String contributionJson = DatabaseClient.get("contribution:" + voucher.getUserId());
            UserContribution contribution = JsonbBuilder.create().fromJson(contributionJson, UserContribution.class);

            contribution.setAllocated(contribution.getAllocated() - voucher.getAmount());
            contribution.setSpent(contribution.getSpent() + voucher.getAmount());

            DatabaseClient.set("contribution:" + voucher.getUserId(), JsonbBuilder.create().toJson(contribution));

            // Aggiorna le statistiche globali dei buoni consumati
            String vouchersConsumedStr = DatabaseClient.get("stats:vouchersConsumed");
            int vouchersConsumed = vouchersConsumedStr != null && !vouchersConsumedStr.equals("null")
                    ? Integer.parseInt(vouchersConsumedStr)
                    : 0;
            DatabaseClient.set("stats:vouchersConsumed", String.valueOf(vouchersConsumed + 1));

            // Aggiorna le statistiche globali dei contributi
            String totalAllocatedStr = DatabaseClient.get("stats:totalAllocated");
            String totalSpentStr = DatabaseClient.get("stats:totalSpent");

            double totalAllocated = totalAllocatedStr != null && !totalAllocatedStr.equals("null")
                    ? Double.parseDouble(totalAllocatedStr)
                    : 0.0;
            double totalSpent = totalSpentStr != null && !totalSpentStr.equals("null")
                    ? Double.parseDouble(totalSpentStr)
                    : 0.0;

            DatabaseClient.set("stats:totalAllocated", String.valueOf(totalAllocated - voucher.getAmount()));
            DatabaseClient.set("stats:totalSpent", String.valueOf(totalSpent + voucher.getAmount()));

            return Response.ok(voucher).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (inside lock): " + e.getMessage())
                    .build();
        } finally {
            userLock.unlock();
        }
    }

    /**
     * Elimina un buono.
     */
    @DELETE
    @Path("/{voucherId}")
    public Response deleteVoucher(@PathParam("voucherId") String voucherId) {
        String initialVoucherJson;
        Voucher initialVoucher;
        try {
           
            initialVoucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (initialVoucherJson == null || initialVoucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found (initial fetch)").build();
            }
            initialVoucher = JsonbBuilder.create().fromJson(initialVoucherJson, Voucher.class);
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (initial fetch): " + e.getMessage())
                    .build();
        }

        ReentrantLock userLock = getUserLock(initialVoucher.getUserId());
        userLock.lock();
        try {
            
            String voucherJson = DatabaseClient.get("voucher:" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher not found").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);

            // Verifica che il buono non sia già stato consumato
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Cannot delete a consumed voucher")
                        .build();
            }

            // Aggiorna il contributo dell'utente
            String contributionJson = DatabaseClient.get("contribution:" + voucher.getUserId());
            UserContribution contribution = JsonbBuilder.create().fromJson(contributionJson, UserContribution.class);

            contribution.setAvailable(contribution.getAvailable() + voucher.getAmount());
            contribution.setAllocated(contribution.getAllocated() - voucher.getAmount());

            DatabaseClient.set("contribution:" + voucher.getUserId(), JsonbBuilder.create().toJson(contribution));

            
            String userId = voucher.getUserId();
            String vouchersCountStr = DatabaseClient.get("vouchersCount:" + userId);
            int currentVoucherCount = 0;
            if (vouchersCountStr != null && !vouchersCountStr.equals("null")) {
                try {
                    currentVoucherCount = Integer.parseInt(vouchersCountStr);
                } catch (NumberFormatException e) {
                }
            }

            if (currentVoucherCount > 0) {
                boolean found = false;
                int deletedIndex = -1;
                for (int i = 0; i < currentVoucherCount; i++) {
                    String idAtIndex = DatabaseClient.get("voucherIdByIndex:" + userId + ":" + i);
                    if (voucherId.equals(idAtIndex)) {
                        found = true;
                        deletedIndex = i;
                        break;
                    }
                }

                if (found && deletedIndex != -1) {
                    for (int i = deletedIndex; i < currentVoucherCount - 1; i++) {
                        String nextVoucherId = DatabaseClient.get("voucherIdByIndex:" + userId + ":" + (i + 1));
                        if (nextVoucherId != null && !nextVoucherId.equals("null")) {
                            DatabaseClient.set("voucherIdByIndex:" + userId + ":" + i, nextVoucherId);
                        } else {
                            
                            DatabaseClient.delete("voucherIdByIndex:" + userId + ":" + i);
                        }
                    }
                    
                    DatabaseClient.delete("voucherIdByIndex:" + userId + ":" + (currentVoucherCount - 1));
                    
                    DatabaseClient.set("vouchersCount:" + userId, String.valueOf(currentVoucherCount - 1));
                } else {
                   
                }
            }
            

            // Elimina il buono
            DatabaseClient.delete("voucher:" + voucherId);

            // Aggiorna le statistiche globali
            String totalAllocatedStr = DatabaseClient.get("stats:totalAllocated");
            String totalAvailableStr = DatabaseClient.get("stats:totalAvailable");

            double totalAllocated = totalAllocatedStr != null && !totalAllocatedStr.equals("null")
                    ? Double.parseDouble(totalAllocatedStr)
                    : 0.0;
            double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null")
                    ? Double.parseDouble(totalAvailableStr)
                    : 0.0;

            DatabaseClient.set("stats:totalAllocated", String.valueOf(totalAllocated - voucher.getAmount()));
            DatabaseClient.set("stats:totalAvailable", String.valueOf(totalAvailable + voucher.getAmount()));

            // Aggiorna il conteggio dei buoni totali
            String totalVouchersStr = DatabaseClient.get("stats:totalVouchers");
            int totalVouchers = totalVouchersStr != null && !totalVouchersStr.equals("null")
                    ? Integer.parseInt(totalVouchersStr)
                    : 0;

            if (totalVouchers > 0) {
                DatabaseClient.set("stats:totalVouchers", String.valueOf(totalVouchers - 1));
            }

            return Response.noContent().build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database (inside lock): " + e.getMessage())
                    .build();
        } finally {
            userLock.unlock();
        }
    }

    /**
     * Aggiorna le statistiche globali quando viene creato un buono.
     */
    private void updateGlobalStats(boolean isConsume, double amount) throws IOException {
        // Incrementa il numero totale di buoni
        String totalVouchersStr = DatabaseClient.get("stats:totalVouchers");
        int totalVouchers = totalVouchersStr != null && !totalVouchersStr.equals("null")
                ? Integer.parseInt(totalVouchersStr)
                : 0;
        DatabaseClient.set("stats:totalVouchers", String.valueOf(totalVouchers + 1));

        // Aggiorna i contributi totali
        String totalAvailableStr = DatabaseClient.get("stats:totalAvailable");
        String totalAllocatedStr = DatabaseClient.get("stats:totalAllocated");

        double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null")
                ? Double.parseDouble(totalAvailableStr)
                : 0.0;
        double totalAllocated = totalAllocatedStr != null && !totalAllocatedStr.equals("null")
                ? Double.parseDouble(totalAllocatedStr)
                : 0.0;

        DatabaseClient.set("stats:totalAvailable", String.valueOf(totalAvailable - amount));
        DatabaseClient.set("stats:totalAllocated", String.valueOf(totalAllocated + amount));
    }
}
