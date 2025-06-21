package it.unimib.sd2025.User;

import java.net.URI;
import java.net.URISyntaxException;

import it.unimib.sd2025.System.DatabaseConnection;
import it.unimib.sd2025.Voucher.Voucher;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/users")
public class UserResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(User user) 
    {
        try 
        {
            if (user.getName().isBlank() ||
                user.getSurname().isBlank() ||
                user.getEmail().isBlank() ||
                user.getFiscalCode().length() != 16)
                {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }

            if (DatabaseConnection.Exists("users/" + user.getFiscalCode())) 
            {
                return Response.status(Response.Status.CONFLICT)
                    .entity("User with fiscal code " + user.getFiscalCode() + " already exists.").build();
            }

            DatabaseConnection.Set("users/" + user.getFiscalCode(), JsonbBuilder.create().toJson(user));
            DatabaseConnection.Set("users/" + user.getFiscalCode() + "/balance", "500");
            DatabaseConnection.Set("users/" + user.getFiscalCode() + "/contribAllocated", "0");
            DatabaseConnection.Set("users/" + user.getFiscalCode() + "/contribSpent", "0");

            /* --------------- SBAGLIATO RIFARE -------------
            String userCountStr = DatabaseConnection.Get("system/stats/userCount");
            int userCount = userCountStr != null && !userCountStr.equals("null") ? Integer.parseInt(userCountStr) : 0;
            DatabaseConnection.Set("system/stats/userCount", String.valueOf(userCount + 1));
            
            String totalAvailableStr = DatabaseConnection.Get("system/stats/totalAvailable");
            double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null") ? Double.parseDouble(totalAvailableStr) : 0.0;
            DatabaseConnection.Set("system/stats/totalAvailable", String.valueOf(totalAvailable + 500.0));

            */

            try
            {
                var uri = new URI("/api/users/" + user.getFiscalCode());
                return Response.created(uri).build();
            }
            catch (URISyntaxException e) 
            {
                return Response.serverError().build();
            }
        } 
        catch (Exception e) 
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{fiscalCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("fiscalCode") String fiscalCode) 
    {
        try 
        {
            String userJson = DatabaseConnection.Get("users/" + fiscalCode);

            if (userJson == null || userJson.equals("null")) 
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            User user = JsonbBuilder.create().fromJson(userJson, User.class);

            return Response.ok(user).build();
        } 
        catch (Exception e) 
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{fiscalCode}/contribution")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserContribution(@PathParam("fiscalCode") String fiscalCode) 
    {
        try 
        {
            int balance = Integer.parseInt(DatabaseConnection.Get("users/" + fiscalCode + "/balance"));
            int contribAllocated = Integer.parseInt(DatabaseConnection.Get("users/" + fiscalCode + "/contribAllocated"));
            int contribSpent = Integer.parseInt(DatabaseConnection.Get("users/" + fiscalCode + "/contribSpent"));

            String json = String.format(
                "{\"balance\":%d,\"contribAllocated\":%d,\"contribSpent\":%d}",
                balance, contribAllocated, contribSpent
            );

            return Response.ok(json, MediaType.APPLICATION_JSON).build();
        } 
        catch (Exception e) 
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{userId}/vouchers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserVouchers(@PathParam("userId") String userId) 
    {
        try 
        {
            String vouchersCountStr = DatabaseConnection.Get("vouchersCount:" + userId);
            int vouchersCount = vouchersCountStr == null || vouchersCountStr.equals("null") ? 0 : Integer.parseInt(vouchersCountStr);

            Voucher[] vouchers = new Voucher[vouchersCount];

            for (int i = 0; i < vouchersCount; i++) 
            {
                String voucherId = DatabaseConnection.Get("voucherIdByIndex:" + userId + ":" + i);

                if (voucherId != null && !voucherId.equals("null")) 
                {
                    String voucherJson = DatabaseConnection.Get("voucher:" + voucherId);

                    if (voucherJson != null && !voucherJson.equals("null")) 
                    {
                        vouchers[i] = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
                    }
                }
            }

            return Response.ok(vouchers).build();
        } 
        catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
