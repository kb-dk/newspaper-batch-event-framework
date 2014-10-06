package dk.statsbiblioteket.medieplatform.autonomous;

/** This class represents a batch, a specific thing on which work will be done */
public class Batch extends Item{

    private String batchID;
    private Integer roundTripNumber = 1;

    /** Constructor */
    public Batch() {
    }

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
     * @param batchID the batch id
     * @param roundTripNumber the roundtrip number
     * @return a string of the format B{batchID}-RT{roundTripNumber}
     */
    public static String formatFullID(String batchID, int roundTripNumber){
        return "B" + batchID + "-RT" + roundTripNumber;
    }
}
