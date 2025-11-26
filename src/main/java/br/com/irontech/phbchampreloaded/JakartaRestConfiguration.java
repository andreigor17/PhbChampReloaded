package br.com.irontech.phbchampreloaded;

import br.com.irontech.phbchampreloaded.resources.GSIResource;
import br.com.irontech.phbchampreloaded.resources.JakartaEE10Resource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Configures Jakarta RESTful Web Services for the application.
 * @author Juneau
 */
@ApplicationPath("resources")
public class JakartaRestConfiguration extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Registra os recursos JAX-RS
        classes.add(GSIResource.class);
        classes.add(JakartaEE10Resource.class);
        return classes;
    }
}
