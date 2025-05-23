
package it.unimib.sd2025;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * API REST per la gestione degli utenti.
 */
@Path("api/users")
public class UserResource {

    /**
     * Registra un nuovo utente.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) {
        try {
            // Genera un ID se non è presente
            if (user.getId() == null || user.getId().isEmpty()) {
                user.setId(UUID.randomUUID().toString());
            }
            
            // Verifica se l'utente esiste già
            if (DatabaseClient.exists("user:" + user.getFiscalCode())) {
                return Response.status(Response.Status.CONFLICT)
                    .entity("User with fiscal code " + user.getFiscalCode() + " already exists.")
                    .build();
            }
            
            // Salva l'utente nel database
            DatabaseClient.set("user:" + user.getFiscalCode(), JsonbBuilder.create().toJson(user));
            
            // Inizializza il contributo dell'utente (500€)
            UserContribution contribution = new UserContribution(user.getFiscalCode(), 500.0, 0.0, 0.0, 500.0);
            DatabaseClient.set("contribution:" + user.getFiscalCode(), JsonbBuilder.create().toJson(contribution));
            
            // Aggiorna il conteggio degli utenti
            String userCountStr = DatabaseClient.get("stats:userCount");
            int userCount = userCountStr != null && !userCountStr.equals("null") ? Integer.parseInt(userCountStr) : 0;
            DatabaseClient.set("stats:userCount", String.valueOf(userCount + 1));
            
            // Aggiorna il totale contributi disponibili
            String totalAvailableStr = DatabaseClient.get("stats:totalAvailable");
            double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null") ? 
                Double.parseDouble(totalAvailableStr) : 0.0;
            DatabaseClient.set("stats:totalAvailable", String.valueOf(totalAvailable + 500.0));
            
            return Response.created(URI.create("/api/users/" + user.getFiscalCode())).entity(user).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }

    /**
     * Ottiene un utente tramite codice fiscale.
     */
    @GET
    @Path("/{fiscalCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("fiscalCode") String fiscalCode) {
        try {
            String userJson = DatabaseClient.get("user:" + fiscalCode);
            
            if (userJson == null || userJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }
            
            User user = JsonbBuilder.create().fromJson(userJson, User.class);
            return Response.ok(user).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }

    /**
     * Ottiene il contributo di un utente.
     */
    @GET
    @Path("/{userId}/contribution")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserContribution(@PathParam("userId") String userId) {
        try {
            String contributionJson = DatabaseClient.get("contribution:" + userId);
            
            if (contributionJson == null || contributionJson.equals("null")) {
                return Response.status(Response.Status.NOT_FOUND).entity("Contribution not found").build();
            }
            
            UserContribution contribution = JsonbBuilder.create().fromJson(contributionJson, UserContribution.class);
            return Response.ok(contribution).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }

    /**
     * Ottiene i buoni di un utente.
     */
    @GET
    @Path("/{userId}/vouchers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserVouchers(@PathParam("userId") String userId) {
        try {
            // Recupera i buoni dell'utente dal database
            String vouchersCountStr = DatabaseClient.get("vouchersCount:" + userId);
            int vouchersCount = vouchersCountStr != null && !vouchersCountStr.equals("null") ? 
                Integer.parseInt(vouchersCountStr) : 0;
            
            Voucher[] vouchers = new Voucher[vouchersCount];
            
            for (int i = 0; i < vouchersCount; i++) {
                String voucherId = DatabaseClient.get("voucherIdByIndex:" + userId + ":" + i);
                if (voucherId != null && !voucherId.equals("null")) {
                    String voucherJson = DatabaseClient.get("voucher:" + voucherId);
                    if (voucherJson != null && !voucherJson.equals("null")) {
                        vouchers[i] = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
                    }
                }
            }
            
            return Response.ok(vouchers).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }
}
