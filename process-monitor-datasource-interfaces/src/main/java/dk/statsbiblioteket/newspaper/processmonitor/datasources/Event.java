package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Date;

/**
 * An event that have taken place on a batch
 */
public class Event {

    private EventID eventID;
    private boolean success;
    private String details;
    private Date date;

    /**
     * No-args constructor
     */
    public Event() {
    }

    public EventID getEventID() {
        return eventID;
    }

    public void setEventID(EventID eventID) {
        this.eventID = eventID;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
