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
     * Add a string to the evergrowing execution outcome message. This does not have to be set if success is true, but if the
     * component is not successful, it must be set.
     * @param message the message to add
     */
    public void addMessage(String message) {
        messages.add(message);
    }

    /**
     * Return the added messages as one string, each message separated by a newline
     * @return the messages as a string
     */
    public String toSummary(){
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
