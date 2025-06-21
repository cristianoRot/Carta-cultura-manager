package it.unimib.sd2025.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.HashMap;

import it.unimib.sd2025.System.DatabaseConnection;
import it.unimib.sd2025.Voucher.Voucher;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
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

            return Response.ok(userJson).build();
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
    @Path("/{fiscalCode}/vouchers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVouchers(@PathParam("fiscalCode") String fiscalCode) 
    {
        try
        {
            String vouchersJson = DatabaseConnection.Get("vouchers");

            if (vouchersJson == null || vouchersJson.equals("ERROR") || vouchersJson.isBlank()) 
                return Response.ok("[]", MediaType.APPLICATION_JSON).build();
            

            Jsonb jsonb = JsonbBuilder.create();
            Map<?, ?> rawVouchersMap = jsonb.fromJson(vouchersJson, Map.class);
            Map<String, Voucher> allVouchers = new HashMap<>(rawVouchersMap.size());

            for (var entry : rawVouchersMap.entrySet()) 
            {
                String voucherId = String.class.cast(entry.getKey());
                String voucherJson = jsonb.toJson(entry.getValue());
                Voucher v = jsonb.fromJson(voucherJson, Voucher.class);
                allVouchers.put(voucherId, v);
            }
            
            List<Voucher> userVouchers = new ArrayList<>();

            for (Voucher v : allVouchers.values()) 
            {
                if (fiscalCode.equals(v.getUserId()))
                    userVouchers.add(v);
            }

            String resultJson = jsonb.toJson(userVouchers);
            return Response.ok(resultJson, MediaType.APPLICATION_JSON).build();
        }
        catch (Exception e)
        {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{fiscalCode}/voucher")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVoucher(@PathParam("fiscalCode") String fiscalCode, Voucher voucher) 
    {
        try
        {
            String balance_s = DatabaseConnection.Get("users/" + fiscalCode + "/balance");
            double balance = Double.parseDouble(balance_s);

            if (voucher.getAmount() > balance) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"declined\",\"error\":\"Saldo insufficiente per creare il voucher richiesto.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }

            String voucherId = java.util.UUID.randomUUID().toString();
            voucher.setId(voucherId);

            String voucherJson = JsonbBuilder.create().toJson(voucher);

            DatabaseConnection.Set("vouchers/" + voucherId, voucherJson);
            DatabaseConnection.Increment("users/" + fiscalCode + "/balance", -voucher.getAmount());

            return Response.status(Response.Status.CREATED)
                           .entity(voucherJson)
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
        catch (Exception e)
        {
            return Response.serverError().build();
        }
    }
}
