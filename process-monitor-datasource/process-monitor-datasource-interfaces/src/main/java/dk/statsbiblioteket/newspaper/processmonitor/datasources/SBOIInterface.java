package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Iterator;
import java.util.List;

/**
 * The interface to the Summa Batch Object Index
 */
public interface SBOIInterface {


    /**
     * Perform a search for batches matching the given criteria
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents Events that the batch must have experienced, but which failed
     * @param futureEvents Events that the batch must not have experienced
     * @return An iterator over the found batches
     * @throws CommunicationException if the communication failed
     */
    public Iterator<Batch> getBatches(List<EventID> pastSuccessfulEvents,
                                      List<EventID> pastFailedEvents,
                                      List<EventID> futureEvents) throws CommunicationException;

    /**
     * Retrieve a batch from the summa index
     * @param batchID the batch id
     * @param roundTripNumber the round trip number
     * @return the batch if found
     * @throws CommunicationException if the communication failed
     * @throws NotFoundException if the described batch could not be found
     */
    public Batch getBatch(Long batchID, Integer roundTripNumber) throws CommunicationException, NotFoundException;
}
