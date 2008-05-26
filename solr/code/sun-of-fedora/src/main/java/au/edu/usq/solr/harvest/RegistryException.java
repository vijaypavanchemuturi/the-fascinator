package au.edu.usq.solr.harvest;

/**
 * General exception used to signal registry operation errors.
 * 
 * @author Oliver Lucido
 */
public class RegistryException extends Exception {

    public RegistryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(Throwable throwable) {
        super(throwable);
    }
}
