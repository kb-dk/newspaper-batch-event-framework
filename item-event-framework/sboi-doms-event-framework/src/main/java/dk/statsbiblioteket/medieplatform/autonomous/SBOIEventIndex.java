package dk.statsbiblioteket.medieplatform.autonomous;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of the {@link EventTrigger} interface using SBOI summa index and DOMS.
 * Uses the SolrJConnector to query the summa instance for items, and REST to get batch details from DOMS.
 */
public class SBOIEventIndex<T extends Item> implements EventTrigger<T> {

    public static final String SUCCESSEVENT = "success_event";
    public static final String EVENT = "event";
    public static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    public static final String UUID = "item_uuid";
    public static final String SORT_DATE = "initial_date";
    public static final String PREMIS_NO_DETAILS = "premis_no_details";
    private static final String OUTDATEDEVENT = "outdated_event";
    private static final String ITEMTYPE = "item_models";
    public static final String LAST_MODIFIED = "lastmodified_date";

    private static Logger log = org.slf4j.LoggerFactory.getLogger(SBOIEventIndex.class);
    private final PremisManipulatorFactory<T> premisManipulatorFactory;
    private DomsEventStorage<T> domsEventStorage;
    private final HttpSolrServer summaSearch;
    private final int pageSize;

    public SBOIEventIndex(String summaLocation, PremisManipulatorFactory<T> premisManipulatorFactory,
                          DomsEventStorage<T> domsEventStorage, int pageSize) throws MalformedURLException {
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
        this.pageSize = pageSize;
        summaSearch = new SolrJConnector(summaLocation).getSolrServer();
    }

    public static String anded(List<String> events) {
        StringBuilder result = new StringBuilder();
        for (String event : events) {
            result.append(" AND ").append(event);
        }
        return result.toString();
    }


    @Override
    public Iterator<T> getTriggeredItems(Query<T> query) throws CommunicationException {
        Iterator<T> sboiItems = search(true, query);
        ArrayList<T> result = new ArrayList<>();
        while (sboiItems.hasNext()) {
            T next = sboiItems.next();
            if (match(next, query)) {
                result.add(next);
            }
        }
        return result.iterator();
    }

    @Override
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> outdatedEvents, Collection<String> futureEvents) throws CommunicationException {
        return getTriggeredItems(pastSuccessfulEvents, outdatedEvents, futureEvents, new ArrayList<T>());
    }

    @Override
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> outdatedEvents, Collection<String> futureEvents,
                                            Collection<T> items) throws CommunicationException {
        Query<T> query = new Query<T>();
        if (futureEvents != null) {
            query.getFutureEvents().addAll(futureEvents);
        }
        if (pastSuccessfulEvents != null) {
            query.getPastSuccessfulEvents().addAll(pastSuccessfulEvents);
        }
        if (outdatedEvents != null){
            query.getOutdatedEvents().addAll(outdatedEvents);
        }
        if (items != null) {
            query.getItems().addAll(items);
        }
        return getTriggeredItems(query);
    }

    /**
     * Check that the item matches the requirements expressed in the three lists
     *
     * @param item                the item to check
     * @param query query that must be fulfilled
     *
     * @return true if the item match all requirements
     */
    private boolean match(Item item, Query<T> query) {
        Set<String> existingEvents = new HashSet<>();
        Set<String> successEvents = new HashSet<>();
        Set<String> failEvents = new HashSet<>();
        Set<String> outdatedEvents = new HashSet<>();
        for (Event event : item.getEventList()) {
            existingEvents.add(event.getEventID());
            if (event.isSuccess()) {
                successEvents.add(event.getEventID());
            } else {
                failEvents.add(event.getEventID());
            }
            if (item.getLastModified() != null) {
                if (!event.getDate().after(item.getLastModified())) {
                    outdatedEvents.add(event.getEventID());
                }
            }
        }
        final boolean successEventsGood = successEvents.containsAll(query.getPastSuccessfulEvents());


        boolean outdatedEventsGood = true;
        for (String outdatedEvent : query.getOutdatedEvents()) {
            outdatedEventsGood = outdatedEventsGood && (outdatedEvents.contains(outdatedEvent) || !existingEvents.contains(outdatedEvent));
        }
        boolean futureEventsGood = Collections.disjoint(existingEvents, query.getFutureEvents());


        //TODO we do not check for items or types for now
        return successEventsGood  && outdatedEventsGood && futureEventsGood;
    }


    /**
     * Perform a search for items matching the given criteria
     *
     * @return An iterator over the found items
     * @throws CommunicationException if the communication failed
     */
    public Iterator<T> search(boolean details, Query<T> query) throws CommunicationException {
       return new SolrProxyIterator<>(toQueryString(query),details,summaSearch,premisManipulatorFactory,domsEventStorage,pageSize);
    }


    protected static String spaced(String string) {
        return " " + string.trim() + " ";
    }

    protected static String quoted(String string) {
        return "\"" + string + "\"";
    }

    /**
     * Converts the query to a solr query string.
     * <ul>
     *
     * <li>The first part of the query is the Items, ie. the set of items which constrain the result set
     *</li><li>
     * The next part is the success events. Items must have these events with outcome success
     *</li><li>
     * The next part is the fail events. Items must have these events with the outcome failure
     *</li><li>
     * The next part is the future events. Items must not have these events in with any outcome.
     *</li><li>
     * The next part is the outdated events. Items must have these events (outcome not important) and must have received an update since this event was registered
     *</li><li>
     * The next part is the up2date events. Items must have these events (outcome not important) and must not have
     * received an update since this event was registered
     *</li><li>
     * The next part is the outdatedOrMissing events. This is a combined thingy. Items must have these events as outdated events, or not at all.
     *</li><li>
     * The next part is the item types. These are the content models that the items must have. This is not about the
     * events at all, but about the types of items that can be returned.
     * </li>
     *</ul>
     *
     * @param query the query
     * @return the query string
     */
    protected String toQueryString(Query<T> query) {
        String base = spaced(RECORD_BASE);

        String itemsString = "";

        if (!query.getItems().isEmpty()) {
            itemsString = getResultRestrictions(query.getItems());
        }

        List<String> events = new ArrayList<>();


        for (String successfulPastEvent : query.getPastSuccessfulEvents()) {
            events.add(spaced("+" + SUCCESSEVENT + ":" + quoted(successfulPastEvent)));
        }


        for (String outdatedEvent : query.getOutdatedEvents()) {
            StringBuilder f = new StringBuilder();
            f.append(" ( ");
            f.append(spaced("+" + OUTDATEDEVENT + ":" + quoted(outdatedEvent)));
            f.append(" OR ( ");
            f.append(spaced("-" + EVENT + ":" + quoted(outdatedEvent)));
            f.append(" ) )");
            events.add(f.toString());
        }

        for (String futureEvent : query.getFutureEvents()) {
            events.add(spaced("-" + EVENT + ":" + quoted(futureEvent)));
        }


        for (String type : query.getTypes()) {
            events.add(spaced("+" + ITEMTYPE + ":" + quoted(type)));
        }

        return base + itemsString + anded(events);
    }

    protected String getResultRestrictions(Collection<T> items) {
        StringBuilder itemsString = new StringBuilder();
        itemsString.append(" AND ( ");

        boolean first = true;
        for (Item item : items) {
            if (first) {
                first = false;
            } else {
                itemsString.append(" OR ");
            }
            itemsString.append(" ( ");

            itemsString.append("+").append(UUID).append(":\"").append(item.getDomsID()).append("\"");

            itemsString.append(" ) ");
        }
        itemsString.append(" ) ");

        return itemsString.toString();
    }
}
