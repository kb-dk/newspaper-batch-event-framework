package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/** The interface to the Summa Batch Object Index */
public interface EventTrigger<T extends Item> {


    /**
     * Perform a search for batches matching the given criteria. All results are checked against DOMS
     * as we do not trust that the SBOI have the most current edition.
     *
     *
     * @param query@return An iterator over the found batches
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if the communication failed
     */
    public Iterator<T> getTriggeredItems(Query<T> query) throws CommunicationException;


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
     * @deprecated use getTriggeredItems(Query) instead
     */
    @Deprecated
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents) throws
                                                                                            CommunicationException;

    /**
     * Perform a search for batches matching the given criteria. All results are checked against DOMS
     * as we do not trust that the SBOI have the most current edition.
     *
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents     Events that the batch must have experienced, but which failed
     * @param futureEvents         Events that the batch must not have experienced
     * @param batches              The resulting iterator will only contain hits from this collection.
     *
     * @return An iterator over the found batches
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if the communication failed
     * @deprecated use getTriggeredItems(Query) instead
     */
    @Deprecated
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents,
                                            Collection<T> batches) throws
                                                                                                  CommunicationException;

    public class Query<T extends Item> {
        private final Collection<String> pastSuccessfulEvents = new HashSet<>();
        private final Collection<String> pastFailedEvents = new HashSet<>();
        private final Collection<String> futureEvents = new HashSet<>();
        private final Collection<String> up2dateEvents = new HashSet<>();
        private final Collection<String> outdatedEvents = new HashSet<>();
        private final Collection<String> outdatedOrMissingEvents = new HashSet<>();
        private final Collection<String> types = new HashSet<>();
        private final Collection<T> items = new HashSet<>();


        public Collection<String> getPastSuccessfulEvents() {
            return pastSuccessfulEvents;
        }

        public Collection<String> getPastFailedEvents() {
            return pastFailedEvents;
        }

        public Collection<String> getFutureEvents() {
            return futureEvents;
        }

        public Collection<String> getOutdatedOrMissingEvents() {
            return outdatedOrMissingEvents;
        }

        public Collection<String> getTypes() {
            return types;
        }

        public Collection<String> getUp2dateEvents() {
            return up2dateEvents;
        }

        public Collection<String> getOutdatedEvents() {
            return outdatedEvents;
        }

        public Collection<T> getItems() {
            return items;
        }


    }
}
