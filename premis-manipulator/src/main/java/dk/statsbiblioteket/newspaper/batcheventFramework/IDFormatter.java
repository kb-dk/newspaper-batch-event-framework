package dk.statsbiblioteket.newspaper.batcheventFramework;

/**
 * This class is to format and unformat ids from strings to Long
  */
public interface IDFormatter {

    /**
     * Format a batch to String
     * @param batchID the batch id
     * @return as a string
     */
    String formatBatchID(Long batchID);

    /**
     * Undo formatBatchID
     * @param batchID batchIDs as a string
     * @return as a Long
     * @see IDFormatter#formatBatchID(Long)
     */
    Long unformatBatchID(String batchID);

    /**
     * Format a full id
     * @param batchID the batchID
     * @param runNr the runNar
     * @return as a String
     */
    String formatFullID(Long batchID, int runNr);

    /**
     * Undo formatFullID
     * @param fullID the full ID
     * @return as a SplitID because java does not have Pairs
     * @see #formatFullID(Long, int)
     */
    SplitID unformatFullID(String fullID);

    public static class SplitID{
        private Long batchID;
        private int runNr;

        public SplitID(Long batchID, int runNr) {
            this.batchID = batchID;
            this.runNr = runNr;
        }

        public Long getBatchID() {
            return batchID;
        }

        public int getRunNr() {
            return runNr;
        }
    }

}
