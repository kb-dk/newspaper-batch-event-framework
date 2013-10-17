package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This exception represents the work of a runnable component failing
 */
public class WorkException extends Exception {
    public WorkException() {
    }

    public WorkException(String message) {
        super(message);
    }

    public WorkException(String message,
                         Throwable cause) {
        super(message, cause);
    }

    public WorkException(Throwable cause) {
        super(cause);
    }

    public WorkException(String message,
                         Throwable cause,
                         boolean enableSuppression,
                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
