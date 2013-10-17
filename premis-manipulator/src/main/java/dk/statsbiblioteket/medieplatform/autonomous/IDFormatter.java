package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This class is to format and unformat ids from strings to Long
  */
public interface IDFormatter {

    /**
     * Format a batch to String
     * @param batchID the batch id
     * @return as a string
     */
    String formatBatchID(String batchID);

    /**
     * Undo formatBatchID
     * @param batchID batchIDs as a string
     * @return as a Long
     * @see IDFormatter#formatBatchID(String)
     */
    Long unformatBatchID(String batchID);

    /**
     * Format a full id
     * @param batchID the batchID
     * @param roundTripNumber the round trip number
     * @return as a String
     */
    String formatFullID(String batchID, int roundTripNumber);

    /**
     * Undo formatFullID
     * @param fullID the full ID
     * @return as a SplitID because java does not have Pairs
     * @see #formatFullID(String, int)
     */
    SplitID unformatFullID(String fullID);

    public static class SplitID{
        private String batchID;
        private int roundTripNumber;

        public SplitID(String batchID, int roundTripNumber) {
            this.batchID = batchID;
            this.roundTripNumber = roundTripNumber;
        }

        public String getBatchID() {
            return batchID;
        }

        public int getRoundTripNumber() {
            return roundTripNumber;
        }
    }

}
