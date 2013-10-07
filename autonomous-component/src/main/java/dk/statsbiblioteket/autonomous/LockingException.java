package dk.statsbiblioteket.autonomous;

/**
 * General exception thrown when the locking framework fails
 */
public class LockingException extends Exception {
    public LockingException() {
    }

    public LockingException(String message) {
        super(message);
    }

    public LockingException(String message,
                            Throwable cause) {
        super(message, cause);
    }

    public LockingException(Throwable cause) {
        super(cause);
    }

    public LockingException(String message,
                            Throwable cause,
                            boolean enableSuppression,
                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
