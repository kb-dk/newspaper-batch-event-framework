package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.List;

/**
 * This class represents a batch, a specific thing on which work will be done
 */
public class Batch{

    private String batchID;
    private Integer roundTripNumber = 1;
    private List<Event> eventList;

    /**
     * Constructor
     */
    public Batch() {
    }

    /**
     * Constructor
     */
    public Batch(String batchID) {
        setBatchID(batchID);
    }

    /**
     * The round trip number. This will never be less than 1. It counts the number of times a batch
     * have been redelivered
     */
    public Integer getRoundTripNumber() {
        return roundTripNumber;
    }

    /**
     * Set the round trip number
     */
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
     * Get the List of events that this batch have experienced. Order is not important
     *
     * @return the list of events
     */
    public List<Event> getEventList() {
        return eventList;
    }

    /**
     * Set the events.
     *
     * @param eventList the event list
     */
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    /**
     * Get the full ID in the form B<batchID>-RT<roundTripNumber>
     * @return the full ID
     */
    public String getFullID(){
        return "B"+batchID+"-RT"+roundTripNumber;
    }
}
