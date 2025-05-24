package it.unimib.sd2025;

import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Gestore di eccezioni per errori di deserializzazione JSON (restituisce 400 invece di 500).
 */
@Provider
public class JsonException implements ExceptionMapper<JsonbException> {
  public Response toResponse(JsonbException ex) {
    return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).type("text/plain").build();
  }
}
