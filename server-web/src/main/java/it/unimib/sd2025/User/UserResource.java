package it.unimib.sd2025.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.HashMap;

import it.unimib.sd2025.System.DatabaseConnection;
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

            if (userJson == null || userJson.equals("NOT_FOUND")) 
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

            if (vouchersJson == null || vouchersJson.equals("NOT_FOUND") || vouchersJson.isBlank()) 
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
            voucher.setUserId(fiscalCode);

            String voucherJson = JsonbBuilder.create().toJson(voucher);

            DatabaseConnection.Set("vouchers/" + voucherId, voucherJson);
            DatabaseConnection.Increment("users/" + fiscalCode + "/balance", -voucher.getAmount());
            DatabaseConnection.Increment("users/" + fiscalCode + "/contribAllocated", voucher.getAmount());

            DatabaseConnection.Increment("system/stats/totalVouchers", 1);
            DatabaseConnection.Increment("system/stats/totalAvailable", -voucher.getAmount());
            DatabaseConnection.Increment("system/stats/totalAllocated", voucher.getAmount());

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

    @POST
    @Path("/{fiscalCode}/voucher/{voucherId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response consumeVoucher(@PathParam("fiscalCode") String fiscalCode, @PathParam("voucherId") String voucherId) 
    {
        try 
        {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);

            if (voucherJson == null || voucherJson.equals("NOT_FOUND"))
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);

            if (!voucher.getUserId().equals(fiscalCode))
                return Response.status(Response.Status.FORBIDDEN).entity("Il Voucher non è di tua proprietà").build();

            if (voucher.getStatus().equals("consumed"))
                return Response.status(Response.Status.BAD_REQUEST).entity("Voucher già consumato").build();

            voucher.setStatus("consumed");
            voucher.setConsumedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            DatabaseConnection.Set("vouchers/" + voucherId, JsonbBuilder.create().toJson(voucher));
            
            DatabaseConnection.Increment("users/" + fiscalCode + "/contribAllocated", -voucher.getAmount());
            DatabaseConnection.Increment("users/" + fiscalCode + "/contribSpent", +voucher.getAmount());
            
            DatabaseConnection.Increment("system/stats/vouchersConsumed", 1);
            DatabaseConnection.Increment("system/stats/totalAllocated", -voucher.getAmount());
            DatabaseConnection.Increment("system/stats/totalSpent", voucher.getAmount());
            
            return Response.ok(voucher).build();
        } 
        catch (Exception ex) 
        {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{fiscalCode}/voucher/{voucherId}")
    public Response deleteVoucher(@PathParam("fiscalCode") String fiscalCode, @PathParam("voucherId") String voucherId)
    {
        try 
        {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);

            if (voucherJson == null || voucherJson.equals("NOT_FOUND"))
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);

            if (!voucher.getUserId().equals(fiscalCode))
                return Response.status(Response.Status.FORBIDDEN).entity("Il Voucher non è di tua proprietà").build();

            if (voucher.getStatus().equals("consumed"))
                return Response.status(Response.Status.BAD_REQUEST).entity("Voucher già consumato").build();

            DatabaseConnection.Delete("vouchers/" + voucherId);

            DatabaseConnection.Increment("users/" + fiscalCode + "/balance", voucher.getAmount());
            DatabaseConnection.Increment("users/" + fiscalCode + "/contribAllocated", -voucher.getAmount());

            DatabaseConnection.Increment("system/stats/totalVouchers", -1);
            DatabaseConnection.Increment("system/stats/totalAvailable", voucher.getAmount());
            DatabaseConnection.Increment("system/stats/totalAllocated", -voucher.getAmount());

            return Response.noContent().build();
        } 
        catch (Exception ex) 
        {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }

    @PUT
    @Path("/{fiscalCode}/voucher/{voucherId}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateVoucher(@PathParam("fiscalCode") String fiscalCode, @PathParam("voucherId") String voucherId, String category)
    {
        try
        {
            String voucherJson = DatabaseConnection.Get("vouchers/" + voucherId);

            if (voucherJson == null || voucherJson.equals("NOT_FOUND"))
                return Response.status(Response.Status.NOT_FOUND).entity("Voucher non trovato").build();

            Voucher voucher = JsonbBuilder.create().fromJson(voucherJson, Voucher.class);
            
            if (voucher.getStatus().equals("consumed"))
                return Response.status(Response.Status.BAD_REQUEST).entity("Impossibile modificare un voucher consumato").build();

            voucher.setCategory(category);
            DatabaseConnection.Set("vouchers/" + voucherId, JsonbBuilder.create().toJson(voucher));

            return Response.ok(voucher).build();
        } 
        catch (Exception ex) 
        {
            return Response.serverError().entity("Errore: " + ex.getMessage()).build();
        }
    }
}
