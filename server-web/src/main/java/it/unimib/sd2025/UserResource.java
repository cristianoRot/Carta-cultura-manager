package it.unimib.sd2025;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

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


            user.setId(UUID.randomUUID().toString());

            if (DatabaseConnection.Exists("user:" + user.getFiscalCode())) 
            {
                return Response.status(Response.Status.CONFLICT)
                    .entity("User with fiscal code " + user.getFiscalCode() + " already exists.").build();
            }

            DatabaseConnection.Set("user:" + user.getFiscalCode(), JsonbBuilder.create().toJson(user));

            UserContribution contribution = new UserContribution(user.getFiscalCode(), 500.0, 0.0, 0.0, 500.0);
            DatabaseConnection.Set("contribution:" + user.getFiscalCode(), JsonbBuilder.create().toJson(contribution));

            String userCountStr = DatabaseConnection.Get("stats:userCount");
            int userCount = userCountStr != null && !userCountStr.equals("null") ? Integer.parseInt(userCountStr) : 0;
            DatabaseConnection.Set("stats:userCount", String.valueOf(userCount + 1));

            String totalAvailableStr = DatabaseConnection.Get("stats:totalAvailable");
            double totalAvailable = totalAvailableStr != null && !totalAvailableStr.equals("null") ? Double.parseDouble(totalAvailableStr) : 0.0;
            DatabaseConnection.Set("stats:totalAvailable", String.valueOf(totalAvailable + 500.0));

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
        catch (IOException e) 
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
            String userJson = DatabaseConnection.Get("users " + fiscalCode);

            if (userJson == null || userJson.equals("null")) 
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            User user = JsonbBuilder.create().fromJson(userJson, User.class);

            return Response.ok(user).build();
        } 
        catch (IOException e) 
        {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/{userId}/contribution")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserContribution(@PathParam("userId") String userId) 
    {
        try 
        {
            String contributionJson = DatabaseConnection.Get("contribution:" + userId);

            if (contributionJson == null || contributionJson.equals("null")) 
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            UserContribution contribution = JsonbBuilder.create().fromJson(contributionJson, UserContribution.class);
            return Response.ok(contribution).build();
        } 
        catch (IOException e) 
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
        catch (IOException e) {
            return Response.serverError().build();
        }
    }
}
