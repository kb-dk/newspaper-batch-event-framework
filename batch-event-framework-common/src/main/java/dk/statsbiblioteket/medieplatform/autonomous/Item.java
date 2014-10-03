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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Item)) {
            return false;
        }

        Item item = (Item) o;

        if (domsID != null ? !domsID.equals(item.domsID) : item.domsID != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return domsID != null ? domsID.hashCode() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Item: " + getDomsID());
        if (getEventList() != null && !getEventList().isEmpty()) {
            sb.append(", eventList=" + getEventList());
        }
        return sb.toString();
    }
}
