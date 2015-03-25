package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Date;

/** An event that have taken place on a batch */
public class Event {

    private String eventID;
    private boolean success;
    private String details;
    private Date date;

    /** No-args constructor */
    public Event() {
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((eventID == null) ? 0 : eventID.hashCode());
        result = prime * result + (success ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Event)) {
            return false;
        }
        Event other = (Event) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (eventID == null) {
            if (other.eventID != null) {
                return false;
            }
        } else if (!eventID.equals(other.eventID)) {
            return false;
        }
        if (success != other.success) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Event [eventID=" + eventID + ", success=" + success
                + ", details=" + details + ", date=" + date + "]";
    }

}
