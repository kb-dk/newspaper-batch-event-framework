package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.List;

/**
 * This class represents a batch, a specific thing on which work will be done
 */
public class Batch{

    private Long batchID;
    private int runNr = 0;
    private List<Event> eventList;

    /**
     * Constructor
     */
    public Batch() {
    }

    public int getRunNr() {
        return runNr;
    }

    public void setRunNr(int runNr) {
        this.runNr = runNr;
    }

    /**
     * Get the Batch id. This id can contain the runNr, but this is dependent on the datasource that provided this batch
     *
     * @return as above
     */
    public Long getBatchID() {
        return batchID;
    }

    /**
     * Set the batch id
     *
     * @param batchID to set
     */
    public void setBatchID(Long batchID) {
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
}
