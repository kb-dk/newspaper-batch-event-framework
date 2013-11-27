package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.HashMap;
import java.util.Map;

public class CallResult {
    private final Map<String,ResultCollector> results = new HashMap<>();
    private String errorMessage = null;

    /**
     * Create a result for a fatal error.
     * @param errorMessage Describes the error.
     */
    public CallResult(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    public CallResult() {}

    public void addResult(String batchID, ResultCollector resultCollector) {
        results.put(batchID, resultCollector);
    }

    @Override
    public String toString() {
        StringBuilder resultString = new StringBuilder();
        for (Map.Entry<String, ResultCollector> result : results.entrySet()) {
            if (result.getValue().isSuccess()) {
                resultString.append("Worked on " + result.getKey() + " successfully\n");
            } else {
                resultString.append("Failed to process " + result.getKey() + "\n");
            }

        }
        return resultString.toString();
    }

    /**
     * Will return 0 if the supplied map doesn't contains any failures. The following int values indicates failures:<br>
     *     1: A batch check found a failure.
     *     2: A batch check had to exit because of a unrecoverable problem (Not implemented).
     * </br>
     */
    public int containsFailures() {
        for (Map.Entry<String, ResultCollector> result : results.entrySet()) {
            if (!result.getValue().isSuccess()) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * If the Call generated a fatal error, a message describing the error will be returned.
     */
    public String getError() {
        return errorMessage;
    }
}
