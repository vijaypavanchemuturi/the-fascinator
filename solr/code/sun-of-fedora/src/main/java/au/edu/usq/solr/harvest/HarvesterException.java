package au.edu.usq.solr.harvest;

public class HarvesterException extends Exception {

    public HarvesterException(String message) {
        super(message);
    }

    public HarvesterException(Throwable cause) {
        super(cause);
    }

    public HarvesterException(String message, Throwable cause) {
        super(message, cause);
    }

}
