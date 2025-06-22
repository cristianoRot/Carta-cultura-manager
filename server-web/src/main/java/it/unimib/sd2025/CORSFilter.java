package it.unimib.sd2025;

import java.io.IOException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Filtro per abilitare il CORS sulle risorse REST.
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Aggiunge le intestazioni CORS a tutte le risposte.
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization, *");
        responseContext.getHeaders().add("Access-Control-Expose-Headers", "Location");

        // Per le richieste pre-flight (OPTIONS), il container potrebbe rispondere con 404 o 405
        // se non c'è una risorsa corrispondente. In tal caso, sovrascriviamo la risposta
        // con un 200 OK per soddisfare il controllo pre-flight del browser.
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            responseContext.setStatus(Response.Status.OK.getStatusCode());
        }
    }
} 