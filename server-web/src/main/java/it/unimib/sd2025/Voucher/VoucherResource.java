package it.unimib.sd2025.Voucher;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import it.unimib.sd2025.System.DatabaseConnection;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * API REST per la gestione dei buoni (voucher).
 */
@Path("api/vouchers")
public class VoucherResource {

    private static final ConcurrentHashMap<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Restituisce (o crea) il lock associato all'utente.
     */
    private ReentrantLock getUserLock(String userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    /* ------------------------------------------------------------
     *                       CREATE VOUCHER
     * ------------------------------------------------------------ */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVoucher(CreateVoucherRequest request) {
        String userId = request.getUserId();
        ReentrantLock lock = getUserLock(userId);
        lock.lock();
        try {
            // Recupero dei contributi correnti dell'utente
            double balance = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/balance"));
            double allocated = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/contribAllocated"));

            if (request.getAmount() > balance) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Saldo insufficiente").build();
            }

            // Creazione del voucher
            String voucherId = UUID.randomUUID().toString();
            Voucher voucher = new Voucher(
                    voucherId,
                    request.getAmount(),
                    request.getCategory(),
                    "generated",
                    LocalDateTime.now().format(formatter),
                    null,
                    userId);

            // Aggiornamento dei contributi utente
            balance -= request.getAmount();
            allocated += request.getAmount();

            DatabaseConnection.Set("users/" + userId + "/balance", String.valueOf(balance));
            DatabaseConnection.Set("users/" + userId + "/contribAllocated", String.valueOf(allocated));

            // Persistenza del voucher
            DatabaseConnection.Set("vouchers/" + voucherId, JsonbBuilder.create().toJson(voucher));

            // Aggiorna indice dei voucher dell'utente
            int vouchersCount = parseIntSafe(DatabaseConnection.Get("users/" + userId + "/vouchersCount"));
            DatabaseConnection.Set("users/" + userId + "/voucherIdByIndex-" + vouchersCount, voucherId);
            DatabaseConnection.Set("users/" + userId + "/vouchersCount", String.valueOf(vouchersCount + 1));

            // Aggiornamento statistiche globali
            incrementInt("system/stats/totalVouchers", 1);
            incrementDouble("system/stats/totalAvailable", -request.getAmount());
            incrementDouble("system/stats/totalAllocated", request.getAmount());

            return Response.created(URI.create("/api/vouchers/" + voucherId)).entity(voucher).build();
        } catch (Exception ex) {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        } finally {
            lock.unlock();
        }
    }

    /* ------------------------------------------------------------
     *                       UPDATE VOUCHER (categoria)
     * ------------------------------------------------------------ */
    @PUT
    @Path("/{voucherId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVoucher(@PathParam("voucherId") String voucherId, UpdateVoucherRequest request) {
        try {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Impossibile modificare un voucher consumato").build();
            }

            voucher.setCategory(request.getCategory());
            DatabaseConnection.Set("vouchers/" + voucherId, JsonbBuilder.create().toJson(voucher));

            return Response.ok(voucher).build();
        } catch (Exception ex) {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }

    /* ------------------------------------------------------------
     *                       CONSUME VOUCHER
     * ------------------------------------------------------------ */
    @POST
    @Path("/{voucherId}/consume")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeVoucher(@PathParam("voucherId") String voucherId) {
        try {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Voucher già consumato").build();
            }

            String userId = voucher.getUserId();
            ReentrantLock lock = getUserLock(userId);
            lock.lock();
            try {
                // Aggiorna voucher
                voucher.setStatus("consumed");
                voucher.setConsumedAt(LocalDateTime.now().format(formatter));
                DatabaseConnection.Set("vouchers/" + voucherId, JsonbBuilder.create().toJson(voucher));

                // Aggiorna contributi utente
                double allocated = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/contribAllocated"));
                double spent = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/contribSpent"));

                allocated -= voucher.getAmount();
                spent += voucher.getAmount();

                DatabaseConnection.Set("users/" + userId + "/contribAllocated", String.valueOf(allocated));
                DatabaseConnection.Set("users/" + userId + "/contribSpent", String.valueOf(spent));

                // Aggiorna statistiche globali
                incrementInt("system/stats/vouchersConsumed", 1);
                incrementDouble("system/stats/totalAllocated", -voucher.getAmount());
                incrementDouble("system/stats/totalSpent", voucher.getAmount());

                return Response.ok(voucher).build();
            } finally {
                lock.unlock();
            }
        } catch (Exception ex) {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }

    /* ------------------------------------------------------------
     *                       DELETE VOUCHER
     * ------------------------------------------------------------ */
    @DELETE
    @Path("/{voucherId}")
    public Response deleteVoucher(@PathParam("voucherId") String voucherId) {
        try {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);
            if (voucherJson == null || voucherJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();
            }

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
            if ("consumed".equals(voucher.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Impossibile eliminare un voucher consumato").build();
            }

            String userId = voucher.getUserId();
            ReentrantLock lock = getUserLock(userId);
            lock.lock();
            try {
                // Aggiornamento contributi utente
                double balance = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/balance"));
                double allocated = parseDoubleSafe(DatabaseConnection.Get("users/" + userId + "/contribAllocated"));

                balance += voucher.getAmount();
                allocated -= voucher.getAmount();

                DatabaseConnection.Set("users/" + userId + "/balance", String.valueOf(balance));
                DatabaseConnection.Set("users/" + userId + "/contribAllocated", String.valueOf(allocated));

                // Aggiorna lista voucher utente
                int vouchersCount = parseIntSafe(DatabaseConnection.Get("users/" + userId + "/vouchersCount"));
                int deleteIndex = -1;
                for (int i = 0; i < vouchersCount; i++) {
                    String idAtIdx = DatabaseConnection.Get("users/" + userId + "/voucherIdByIndex-" + i);
                    if (voucherId.equals(idAtIdx)) {
                        deleteIndex = i;
                        break;
                    }
                }
                if (deleteIndex != -1) {
                    for (int i = deleteIndex; i < vouchersCount - 1; i++) {
                        String nextId = DatabaseConnection.Get("users/" + userId + "/voucherIdByIndex-" + (i + 1));
                        if (nextId != null && !nextId.equals("null")) {
                            DatabaseConnection.Set("users/" + userId + "/voucherIdByIndex-" + i, nextId);
                        }
                    }
                    DatabaseConnection.Delete("users/" + userId + "/voucherIdByIndex-" + (vouchersCount - 1));
                    DatabaseConnection.Set("users/" + userId + "/vouchersCount", String.valueOf(vouchersCount - 1));
                }

                // Rimuove il voucher
                DatabaseConnection.Delete("vouchers/" + voucherId);

                // Aggiorna statistiche globali
                incrementInt("system/stats/totalVouchers", -1);
                incrementDouble("system/stats/totalAvailable", voucher.getAmount());
                incrementDouble("system/stats/totalAllocated", -voucher.getAmount());

                return Response.noContent().build();
            } finally {
                lock.unlock();
            }
        } catch (Exception ex) {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }

    /* ============================================================
     *                        METODI AUSILIARI
     * ============================================================ */

    private int parseIntSafe(String str) {
        try {
            if (str == null || str.equals("null")) return 0;
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private double parseDoubleSafe(String str) {
        try {
            if (str == null || str.equals("null")) return 0.0;
            return Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private void incrementInt(String path, int delta) throws Exception {
        int current = parseIntSafe(DatabaseConnection.Get(path));
        DatabaseConnection.Set(path, String.valueOf(current + delta));
    }

    private void incrementDouble(String path, double delta) throws Exception {
        double current = parseDoubleSafe(DatabaseConnection.Get(path));
        DatabaseConnection.Set(path, String.valueOf(current + delta));
    }
}