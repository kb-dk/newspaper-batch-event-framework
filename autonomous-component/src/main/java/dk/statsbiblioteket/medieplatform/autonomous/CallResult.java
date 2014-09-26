package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.HashMap;
import java.util.Map;

/** Container for the result information for a result of invoking the call method on a autonomous components. */
public class CallResult {
    private final Map<Item, ResultCollector> results = new HashMap<>();
    private final String errorMessage;

    /**
     * Create a result for a fatal error.
     *
     * @param errorMessage Describes the error.
     */
    public CallResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    public CallResult() {
        errorMessage = null;
    }

    public void addResult(Item item, ResultCollector resultCollector) {
        results.put(item, resultCollector);
    }

    @Override
    public String toString() {
        StringBuilder resultString = new StringBuilder();
        if (getErrorMessage() != null) {
            resultString.append(getErrorMessage()).append("\n");
        }
        for (Map.Entry<Item, ResultCollector> result : results.entrySet()) {
            if (result.getValue().isSuccess()) {
                resultString.append("Worked on ").append(
                        result.getKey().getFullID()).append(" successfully\n");
            } else {
                resultString.append("Failed to process ").append(
                        result.getKey().getFullID()).append("\n");
            }

        }
        return resultString.toString();
    }

    /**
     * Will return 0 if the supplied map doesn't contains any failures. The following int values indicates
     * failures:<br>
     * 1: A batch check found a failure.
     * 2: A call invocation had to exit because of an unrecoverable problem.
     * </br>
     */
    public int containsFailures() {
        if (fatalErrorEncountered()) {
            return 2;
        }
        for (Map.Entry<Item, ResultCollector> result : results.entrySet()) {
            if (!result.getValue().isSuccess()) {
                return 1;
            }
        }
        return 0;
    }

    /** If the call invocation generated a fatal error, a message describing the error will be returned. */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** Returns <code>true</code> if a fatal error was encountered preventing the call() method from completing. */
    public boolean fatalErrorEncountered() {
        return errorMessage != null;
    }
}
