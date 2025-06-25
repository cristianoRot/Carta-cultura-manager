package it.unimib.sd2025.System;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/system")
public class SystemResource 
{
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystemStats() 
    {
        try 
        {
            String userCountStr = DatabaseConnection.Get("system/stats/userCount");
            String totalAvailableStr = DatabaseConnection.Get("system/stats/totalAvailable");
            String totalAllocatedStr = DatabaseConnection.Get("system/stats/totalAllocated");
            String totalSpentStr = DatabaseConnection.Get("system/stats/totalSpent");
            String totalVouchersStr = DatabaseConnection.Get("system/stats/totalVouchers");
            String vouchersConsumedStr = DatabaseConnection.Get("system/stats/vouchersConsumed");
            
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
            
            SystemStats stats = new SystemStats(
                userCount,
                totalAvailable,
                totalAllocated,
                totalSpent,
                totalVouchers,
                vouchersConsumed
            );
            
            return Response.ok(stats).build();
        } 
        catch (Exception e) 
        {
            return Response.serverError().entity("Error communicating with database: " + e.getMessage()).build();
        }
    }
}
