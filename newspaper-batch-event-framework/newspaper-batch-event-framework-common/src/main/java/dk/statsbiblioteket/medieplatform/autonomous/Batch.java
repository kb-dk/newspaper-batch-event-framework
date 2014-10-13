package dk.statsbiblioteket.medieplatform.autonomous;

/** This class represents a batch, a specific thing on which work will be done */
public class Batch extends Item{

    /**
     * The batch id as a long, ie. without the B in the start of the string
     */
    private String batchID;

    /**
     * The round trip number
     */
    private Integer roundTripNumber = 1;

    /** Constructor */
    public Batch() { }

    /** Constructor */
    public Batch(String batchID) {
        setBatchID(batchID);
    }

    /** Constructor */
    public Batch(String batchID, int roundTripNumber) {
        setBatchID(batchID);
        setRoundTripNumber(roundTripNumber);
    }

    /**
     * The round trip number. This will never be less than 1. It counts the number of times a batch
     * have been redelivered
     */
    public Integer getRoundTripNumber() {
        return roundTripNumber;
    }

    /** Set the round trip number */
    public void setRoundTripNumber(Integer roundTripNumber) {
        this.roundTripNumber = roundTripNumber;
    }

    /**
     * Get the Batch id.
     *
     * @return as above
     */
    public String getBatchID() {
        return batchID;
    }

    /**
     * Set the batch id
     *
     * @param batchID to set
     */
    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }


    /**
     * Get the full ID in the form B<batchID>-RT<roundTripNumber>
     *
     * @return the full ID
     */
    @Override
    public String getFullID() {
          return formatFullID(batchID,roundTripNumber);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Batch: " + getFullID());
        if (getEventList() != null && !getEventList().isEmpty()) {
            sb.append(", eventList=").append(getEventList());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Batch)) {
            return false;
        }

        Batch batch = (Batch) o;

        if (!batchID.equals(batch.batchID)) {
            return false;
        }
        if (!roundTripNumber.equals(batch.roundTripNumber)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = batchID.hashCode();
        result = 31 * result + roundTripNumber.hashCode();
        return result;
    }

    /**
     * Format the batchid and roundtripnumber as a proper batch id
     * @param batchID the batch id without the leading B
     * @param roundTripNumber the roundtrip number
     * @return a string of the format B{batchID}-RT{roundTripNumber}
     */
    public static String formatFullID(String batchID, int roundTripNumber){
        return "B" + batchID + "-RT" + roundTripNumber;
    }


    public static class BatchRoundtripID {
        private String batchID;
        private int roundTripNumber;

        public BatchRoundtripID(String fullID) {
            String[] splits = fullID.split("-RT");
            if (splits.length == 2){
                String batchIDsplit = splits[0];
                if (batchIDsplit.startsWith("path:")){
                    batchIDsplit = batchIDsplit.replace("path:","");
                }
                if (batchIDsplit.startsWith("B")){
                    batchIDsplit = batchIDsplit.replace("B","");
                }
                try {
                    Long.parseLong(batchIDsplit);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("This is not a valid round trip id '" + fullID + "'",e);
                }

                batchID = batchIDsplit;
                String roundTripSplit = splits[1];
                try {
                    roundTripNumber = Integer.parseInt(roundTripSplit);
                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("This is not a valid round trip id '"+fullID+"'",e);
                }

            } else {
                throw new IllegalArgumentException("This is not a valid round trip id '" + fullID + "'");
            }
        }

        public BatchRoundtripID(String batchID, int roundTripNumber) {
            this.batchID = batchID;
            this.roundTripNumber = roundTripNumber;
        }

        public String getBatchID() {
            return batchID;
        }

        public int getRoundTripNumber() {
            return roundTripNumber;
        }

        public String batchDCIdentifier(){
            return "path:B"+batchID;
        }

        public String roundTripDCIdentifier(){
            return "path:B" + batchID + "-RT" + roundTripNumber;
        }
    }
}
