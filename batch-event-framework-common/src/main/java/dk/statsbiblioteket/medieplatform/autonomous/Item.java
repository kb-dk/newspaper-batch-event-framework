package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.List;

/** This class represents an Item, a specific thing on which work will be done */
public class Item {

    private String domsID;
    private List<Event> eventList;


    public String getDomsID() {
        return domsID;
    }

    public void setDomsID(String domsID) {
        this.domsID = domsID;
    }


    /**
     * Get the full ID in the form B<batchID>-RT<roundTripNumber>
     *
     * @return the full ID
     */
    public String getFullID() {
        return domsID;
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
