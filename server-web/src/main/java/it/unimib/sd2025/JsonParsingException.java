package it.unimib.sd2025;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Gestore di eccezioni per errori di parsing JSON (restituisce 400 invece di 500).
 */
@Provider
public class JsonParsingException implements ExceptionMapper<ProcessingException> {
  public Response toResponse(ProcessingException ex) {
    return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).type("text/plain").build();
  }
}
