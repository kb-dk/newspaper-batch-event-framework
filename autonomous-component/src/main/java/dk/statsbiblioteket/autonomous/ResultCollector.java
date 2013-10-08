package dk.statsbiblioteket.autonomous;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This class collects the result of a run of a component.
 */
public class ResultCollector {
    private boolean success;
    private Date timestamp = new Date();

    private List<String> messages;


    /**
     * Set the success Value of the execution
     * @param success the sucesss
     */
    public void setSuccess(boolean success) {
        this.success = success;
        messages = new LinkedList<>();
    }

    /**
     * Get the success value of the execution
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Add a specific failure to the result collector. All these parameters must be non-null and non-empty
     * @param reference the reference to the file/object that caused the failure
     * @param type the type of failure
     * @param component the component that failed
     * @param description Description of the failure.
     */
    public void addFailure(String reference, String type, String component, String description){

    }

    /**
     * Add a specific failure to the result collector. All these parameters must be non-null and non-empty
     * @param reference the reference to the file/object that caused the failure
     * @param type the type of failure
     * @param component the component that failed
     * @param description Description of the failure.
     * @param details additional details, can be null
     */
    public void addFailure(String reference, String type, String component, String description, String details){

    }

    /**
     * Return the added messages as one string, each message separated by a newline
     * @return the messages as a string
     */
    public String toReport(){
        StringBuilder builder = new StringBuilder();
        for (String message : messages) {
            builder.append(message).append("\n");
        }
        return builder.toString();
    }


    /**
     * Timestamp the event that this is the result of
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * The timestamp of the event
     * @return
     */
    public Date getTimestamp() {
        return timestamp;
    }
}
