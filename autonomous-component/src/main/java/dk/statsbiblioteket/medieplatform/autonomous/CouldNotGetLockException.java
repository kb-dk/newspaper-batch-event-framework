package dk.statsbiblioteket.medieplatform.autonomous;


/**
 The component could not get the lock, and could thus not do work.
 */
public class CouldNotGetLockException extends Exception{
    public CouldNotGetLockException() {
    }

    public CouldNotGetLockException(String message) {
        super(message);
    }

    public CouldNotGetLockException(String message,
                                    Throwable cause) {
        super(message, cause);
    }

    public CouldNotGetLockException(Throwable cause) {
        super(cause);
    }

    public CouldNotGetLockException(String message,
                                    Throwable cause,
                                    boolean enableSuppression,
                                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
