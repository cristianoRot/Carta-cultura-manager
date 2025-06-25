package it.unimib.sd2025;

import org.glassfish.jersey.server.ResourceConfig;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() 
    {
        packages("it.unimib.sd2025");
        register(OpenApiResource.class);
    }
} 