package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.NotFoundException;

import java.util.Iterator;
import java.util.List;

/** The interface to the Summa Batch Object Index */
public interface SBOIInterface {


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
    public Iterator<Batch> getBatches(boolean details, List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                      List<String> futureEvents) throws CommunicationException;

    /**
     * Perform a search for batches matching the given criteria. All results are checked against DOMS
     * as we do not trust that the SBOI have the most current edition.
     *
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents     Events that the batch must have experienced, but which failed
     * @param futureEvents         Events that the batch must not have experienced
     *
     * @return An iterator over the found batches
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if the communication failed
     */
    public Iterator<Batch> getCheckedBatches(boolean details, List<String> pastSuccessfulEvents,
                                             List<String> pastFailedEvents, List<String> futureEvents) throws
                                                                                                       CommunicationException;


    /**
     * Retrieve a batch from the summa index
     *
     * @param batchID         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return the batch if found
     * @throws CommunicationException                                         if the communication failed
     * @throws dk.statsbiblioteket.medieplatform.autonomous.NotFoundException if the described batch could not be found
     */
    public Batch getBatch(String batchID, Integer roundTripNumber) throws CommunicationException, NotFoundException;

    /**
     * Perform a search for batches matching the given criteria
     *
     * @param batchID              the batch id. Can be null for all batches
     * @param roundTripNumber      the round trip number. Can be null to match all round trips
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents     Events that the batch must have experienced, but which failed
     * @param futureEvents         Events that the batch must not have experienced
     *
     * @return An iterator over the found batches
     * @throws CommunicationException if the communication failed
     */
    public Iterator<Batch> search(boolean details, String batchID, Integer roundTripNumber,
                                  List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                  List<String> futureEvents) throws CommunicationException;

}
