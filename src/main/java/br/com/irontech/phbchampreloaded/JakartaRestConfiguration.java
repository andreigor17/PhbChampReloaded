package br.com.irontech.phbchampreloaded;

import jakarta.ws.rs.core.Application;
import java.util.Set;

/**
 * Configures Jakarta RESTful Web Services for the application.
 *
 * @author Andre
 */
@jakarta.ws.rs.ApplicationPath("resources")
public class JakartaRestConfiguration extends Application {

    @Override
    public Set<Class<?>> getClasses() {

        // Registra os recursos JAX-RS
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(br.com.irontech.phbchampreloaded.resources.GSIResource.class);
        resources.add(br.com.irontech.phbchampreloaded.resources.JakartaEE10Resource.class);
    }
}
