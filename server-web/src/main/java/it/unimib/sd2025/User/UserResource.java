package it.unimib.sd2025.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

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

            DatabaseConnection.Increment("system/stats/userCount", 1);
            DatabaseConnection.Increment("system/stats/totalAvailable", 500.0);

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
            double balance = Double.parseDouble(DatabaseConnection.Get("users/" + fiscalCode + "/balance"));
            double contribAllocated = Double.parseDouble(DatabaseConnection.Get("users/" + fiscalCode + "/contribAllocated"));
            double contribSpent = Double.parseDouble(DatabaseConnection.Get("users/" + fiscalCode + "/contribSpent"));

            String json = String.format(Locale.US,
                "{\"balance\":%.2f,\"contribAllocated\":%.2f,\"contribSpent\":%.2f}",
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
            String vouchersCountStr = DatabaseConnection.Get("users/" + userId + "/vouchersCount");
            int vouchersCount = vouchersCountStr == null || vouchersCountStr.equals("null") ? 0 : Integer.parseInt(vouchersCountStr);

            Voucher[] vouchers = new Voucher[vouchersCount];

            for (int i = 0; i < vouchersCount; i++) 
            {
                String voucherId = DatabaseConnection.Get("users/" + userId + "/voucherIdByIndex-" + i);

                if (voucherId != null && !voucherId.equals("null")) 
                {
                    String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);

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
