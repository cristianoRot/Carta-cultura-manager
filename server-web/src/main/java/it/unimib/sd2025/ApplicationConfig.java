package it.unimib.sd2025;

import org.glassfish.jersey.server.ResourceConfig;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import jakarta.ws.rs.ApplicationPath;

/**
 * Configurazione principale dell'applicazione REST.
 * Registra automaticamente tutte le risorse presenti nel package
 * e abilita la generazione della documentazione OpenAPI.
 */
@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        // Effettua la scansione di tutte le classi presenti nel package di root
        packages("it.unimib.sd2025");

        // Abilita la feature OpenAPI di Jersey che esporrà la spec su /openapi
        register(OpenApiResource.class);
    }
} 