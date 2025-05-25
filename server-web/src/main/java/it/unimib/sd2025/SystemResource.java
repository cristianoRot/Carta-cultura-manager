package it.unimib.sd2025;

import java.io.IOException;

import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * API REST per le statistiche di sistema.
 */
@Path("api/system")
public class SystemResource {

    /**
     * Ottiene le statistiche globali del sistema.
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemStats() {
        try {
            // Recupera i dati dal database
            String userCountStr = DatabaseConnection.get("stats:userCount");
            String totalAvailableStr = DatabaseConnection.get("stats:totalAvailable");
            String totalAllocatedStr = DatabaseConnection.get("stats:totalAllocated");
            String totalSpentStr = DatabaseConnection.get("stats:totalSpent");
            String totalVouchersStr = DatabaseConnection.get("stats:totalVouchers");
            String vouchersConsumedStr = DatabaseConnection.get("stats:vouchersConsumed");
            
            // Converte i dati nei tipi corretti
            int userCount = userCountStr != null && !userCountStr.equals("null") ? 
                Integer.parseInt(userCountStr) : 0;
            double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null") ? 
                Double.parseDouble(totalAvailableStr) : 0.0;
            double totalAllocated = totalAllocatedStr != null && !totalAllocatedStr.equals("null") ? 
                Double.parseDouble(totalAllocatedStr) : 0.0;
            double totalSpent = totalSpentStr != null && !totalSpentStr.equals("null") ? 
                Double.parseDouble(totalSpentStr) : 0.0;
            int totalVouchers = totalVouchersStr != null && !totalVouchersStr.equals("null") ? 
                Integer.parseInt(totalVouchersStr) : 0;
            int vouchersConsumed = vouchersConsumedStr != null && !vouchersConsumedStr.equals("null") ? 
                Integer.parseInt(vouchersConsumedStr) : 0;
            
            // Crea l'oggetto delle statistiche
            SystemStats stats = new SystemStats(
                userCount,
                totalAvailable,
                totalAllocated,
                totalSpent,
                totalVouchers,
                vouchersConsumed
            );
            
            return Response.ok(stats).build();
        } catch (IOException e) {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }
}
