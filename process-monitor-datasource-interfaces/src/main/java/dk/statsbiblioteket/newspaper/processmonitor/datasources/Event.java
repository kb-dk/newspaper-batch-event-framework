package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Date;

/**
 * An event that have taken place on a batch
 */
public class Event {



    public static final String Initial = "Initial";
    public static final String Added_to_shipping_container = "Added_to_shipping_container";
    public static final String Shipped_to_supplier = "Shipped_to_supplier";

    public static final String Shipped_from_supplier = "Shipped_from_supplier";

    public static final String Received_from_supplier = "Received_from_supplier";
    public static final String FollowUp = "FollowUp";
    public static final String Approved = "Approved";


    private String eventID;
    private boolean success;
    private String details;
    private Date date;

    /**
     * No-args constructor
     */
    public Event() {
    }

    /**
     * Get the event id. The event id
     *
     * @return
     */
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
}
