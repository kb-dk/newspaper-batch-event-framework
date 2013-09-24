package dk.statsbiblioteket.newspaper.batcheventFramework;

/**
 * This class is to format and unformat ids from strings to T
 * @param <T> the batch
 */
public interface IDFormatter<T> {

    /**
     * Format a batch to String
     * @param batchID the batch id
     * @return as a string
     */
    String formatBatchID(T batchID);

    /**
     * Undo formatBatchID
     * @param batchID batchIDs as a string
     * @return as a T
     * @see IDFormatter#formatBatchID(Object)
     */
    T unformatBatchID(String batchID);

    /**
     * Format a full id
     * @param batchID the batchID
     * @param runNr the runNar
     * @return as a String
     */
    String formatFullID(T batchID, int runNr);

    /**
     * Undo formatFullID
     * @param fullID the full ID
     * @return as a SplitID because java does not have Pairs
     * @see #formatFullID(Object, int)
     */
    SplitID<T> unformatFullID(String fullID);

    public static class SplitID<T>{
        private T batchID;
        private int runNr;

        public SplitID(T batchID, int runNr) {
            this.batchID = batchID;
            this.runNr = runNr;
        }

        public T getBatchID() {
            return batchID;
        }

        public int getRunNr() {
            return runNr;
        }
    }

}
