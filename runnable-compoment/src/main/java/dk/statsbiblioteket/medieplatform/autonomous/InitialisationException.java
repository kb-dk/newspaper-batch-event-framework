package dk.statsbiblioteket.medieplatform.autonomous;

/** Thrown on trouble initialising the system. */
public class InitialisationException extends RuntimeException {
    public InitialisationException(String message) {
        super(message);
    }

    public InitialisationException(String message, Throwable cause) {
        super(message, cause);
    }
}
