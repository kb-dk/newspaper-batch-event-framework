package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for accessing details about events.
 */
public interface EventAccessor<T extends Item> {
    /**
     * Retrieve a batch
     *
     * @return the batch
     * @throws NotFoundException      if the batch is not found
     * @throws CommunicationException if communication with doms failed
     */
    T getItem(String itemFullID) throws NotFoundException, CommunicationException;

    /**
     * Perform a search for batches matching the given criteria
     *
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents     Events that the batch must have experienced, but which failed
     * @param futureEvents         Events that the batch must not have experienced
     *
     * @return An iterator over the found batches
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if the communication failed
     */
    public Iterator<T> findItems(boolean details, List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                    List<String> futureEvents) throws CommunicationException;

}
