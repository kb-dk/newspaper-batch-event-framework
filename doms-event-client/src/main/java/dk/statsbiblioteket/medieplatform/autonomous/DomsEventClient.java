package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Date;

/**
 * The doms event client. This client is the interface to the doms event storage. You use it to add events to batches,
 * create batches and to retrieve batches.
 */
public interface DomsEventClient {

    /**
     * Add an event to a batch in doms. Will create the batch if it does not currently exist
     *
     * @param batchId         the batch id
     * @param roundTripNumber the round trip number of the batch
     * @param agent           the agent of the event
     * @param timestamp       the timestamp of the event
     * @param details         details about the OUTCOME of the event
     * @param eventType       the type of event, from a controlled list
     * @param outcome         true if the event was a success, false otherwise
     *
     * @throws CommunicationException if communication with doms failed
     */
    void addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp, String details,
                         String eventType, boolean outcome) throws CommunicationException;

    /**
     * Create a batch and round trip object, without adding any events
     *
     * @param batchId         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return the pid of the doms object corresponding to the round trip
     * @throws CommunicationException if communication with doms failed
     */
    String createBatchRoundTrip(String batchId, int roundTripNumber) throws CommunicationException;

    /**
     * Retrieve a batch
     *
     * @param batchId         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return the batch
     * @throws NotFoundException      if the batch is not found
     * @throws CommunicationException if communication with doms failed
     */
    Batch getBatch(String batchId, Integer roundTripNumber) throws NotFoundException, CommunicationException;

    /**
     * Retrieve a batch
     *
     * @param domsID the id of the round trip object in doms
     *
     * @return the batch
     * @throws NotFoundException      if the batch is not found
     * @throws CommunicationException if communication with doms failed
     */
    Batch getBatch(String domsID) throws NotFoundException, CommunicationException;

    /**
     * This method
     * i) reads the EVENTS datastream
     * ii) Calculates the number of events to be removed from the EVENTS datastream starting with the
     * given eventId
     * iii) if there are no events to be removed it just returns zero, otherwise it
     * iv) makes a backup of the EVENTS datastream for this batch round trip
     * v) writes the modfied EVENTS datastream back to DOMS
     *
     * It is the job of implementers of this method to ensure that it correctly handles the possibility of concurrent
     * modification ie. that the datastream may have changed again between being read and being written. (In which case
     * one should return to step i).
     *
     * @param batchId
     * @param roundTripNumber
     * @param maxTries        the maximum number of attempts.
     * @param waitTime        the time in milliseconds to wait between attempts.
     * @param eventId         The eventId of the of the earliest event to be removed
     *
     * @return the number of events removed.
     * @throws CommunicationException
     */
    int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries, long waitTime,
                                               String eventId) throws CommunicationException, NotFoundException;

    /**
     * This method
     * i) reads the EVENTS datastream
     * ii) Calculates the number of events to be removed from the EVENTS datastream starting with the
     * earliest failure
     * iii) if there are no events to be removed it just returns zero, otherwise it
     * iv) makes a backup of the EVENTS datastream for this batch round trip
     * v) writes the modfied EVENTS datastream back to DOMS
     *
     * It is the job of implementers of this method to ensure that it correctly handles the possibility of concurrent
     * modification ie. that the datastream may have changed again between being read and being written. (In which case
     * one should return to step i).
     *
     * @param batchId
     * @param roundTripNumber
     * @param maxTries        the maximum number of attempts.
     * @param waitTime        the time in milliseconds to wait between attempts.
     *
     * @return the number of events removed.
     * @throws CommunicationException
     */
    int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries, long waitTime) throws
                                                                                                                 CommunicationException,
                                                                                                                 NotFoundException;


}
