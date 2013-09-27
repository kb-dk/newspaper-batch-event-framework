package dk.statsbiblioteket.newspaper.processmonitor.datasources;

public class NotWorkingProperlyException extends Exception {
    public NotWorkingProperlyException() {
    }

    public NotWorkingProperlyException(String message) {
        super(message);
    }

    public NotWorkingProperlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotWorkingProperlyException(Throwable cause) {
        super(cause);
    }

    public NotWorkingProperlyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
