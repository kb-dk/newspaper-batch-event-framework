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
     * This is the query object for a SBOI query.
     * For all the fields, if the field is not set, it does not constrain the search. If several
     * fields are set, the constraints are ANDed.
     * @param <T> the type of items
     */
    public class Query<T extends Item> {
        private final Collection<String> pastSuccessfulEvents = new HashSet<>();
        private final Collection<String> futureEvents = new HashSet<>();
        private final Collection<String> outdatedEvents = new HashSet<>();
        private final Collection<String> types = new HashSet<>();
        private final Collection<T> items = new HashSet<>();


        /**
         * Get the Past Successful Events. These are the events that the item must have experienced, and which
         * have a successful outcome
         * @return a modifiable collection, never null
         */
        public Collection<String> getPastSuccessfulEvents() {
            return pastSuccessfulEvents;
        }

        /**
         * Get the Future Events. These are the events that the item must not have experienced.
         * @return a modifiable collection, never null
         */
        public Collection<String> getFutureEvents() {
            return futureEvents;
        }

        /**
         * These are the types of objects, ie. content models, that the items must have
         * @return a modifiable collection, never null
         */
        public Collection<String> getTypes() {
            return types;
        }

        /**
         * Get the outdated Events. These are the events that the item must not have experienced OR for which
         * the item have been modified since it experienced the event
         * @return a modifiable collection, never null
         */
        public Collection<String> getOutdatedEvents() {
            return outdatedEvents;
        }

        /**
         * These are the items that can appear in the result set.
         * @return a modifiable collection, never null
         */
        public Collection<T> getItems() {
            return items;
        }


    }
}
