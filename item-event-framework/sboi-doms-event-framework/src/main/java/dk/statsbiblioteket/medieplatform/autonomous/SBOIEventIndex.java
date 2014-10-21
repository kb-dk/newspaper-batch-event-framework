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
    public static final String FAILEVENT = "fail_event";
    public static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    public static final String UUID = "item_uuid";
    public static final String SORT_DATE = "initial_date";
    public static final String PREMIS_NO_DETAILS = "premis_no_details";
    private static final String OUTDATEDEVENT = "outdated_event";
    private static final String UP2DATEEVENT = "up2date_event";
    private static final String ITEMTYPE = "item_models";
    public static final String LAST_MODIFIED = "lastmodified_date";

    private static Logger log = org.slf4j.LoggerFactory.getLogger(SBOIEventIndex.class);
    private final PremisManipulatorFactory<T> premisManipulatorFactory;
    private DomsEventStorage<T> domsEventStorage;
    private final HttpSolrServer summaSearch;

    public SBOIEventIndex(String summaLocation, PremisManipulatorFactory<T> premisManipulatorFactory,
                          DomsEventStorage<T> domsEventStorage) throws MalformedURLException {
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
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
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents) throws CommunicationException {
        return getTriggeredItems(pastSuccessfulEvents, pastFailedEvents, futureEvents, new ArrayList<T>());
    }

    @Override
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents,
                                            Collection<T> items) throws CommunicationException {
        Query<T> query = new Query<T>();
        if (futureEvents != null) {
            query.getFutureEvents().addAll(futureEvents);
        }
        if (pastSuccessfulEvents != null) {
            query.getPastSuccessfulEvents().addAll(pastSuccessfulEvents);
        }
        if (pastFailedEvents != null) {
            query.getPastFailedEvents().addAll(pastFailedEvents);
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
        Set<String> up2dateEvents = new HashSet<>();
        for (Event event : item.getEventList()) {
            existingEvents.add(event.getEventID());
            if (event.isSuccess()) {
                successEvents.add(event.getEventID());
            } else {
                failEvents.add(event.getEventID());
            }
            if (item.getLastModified() != null) {
                if (event.getDate().after(item.getLastModified())) {
                    up2dateEvents.add(event.getEventID());
                } else {
                    outdatedEvents.add(event.getEventID());
                }
            }
        }
        final boolean successEventsGood = successEvents.containsAll(query.getPastSuccessfulEvents());

        final boolean failEventsGood = failEvents.containsAll(query.getPastFailedEvents());
        final boolean futureSuccessEventsGood = Collections.disjoint(query.getFutureEvents(), successEvents);
        final boolean futureFailEventsGood = Collections.disjoint(query.getFutureEvents(), failEvents);
        final boolean up2dateEventsGood = up2dateEvents.containsAll(query.getUp2dateEvents());
        final boolean outdatedEventsGood = outdatedEvents.containsAll(query.getOutdatedEvents());

        boolean outdatedOrMissingGood = true;
        for (String outdatedOrMissing : query.getOutdatedOrMissingEvents()) {
            outdatedOrMissingGood =  outdatedOrMissingGood && outdatedEvents.contains(outdatedOrMissing) || !existingEvents.contains(outdatedOrMissing);
        }

        //TODO we do not check for items or types for now
        return successEventsGood && failEventsGood && futureSuccessEventsGood && futureFailEventsGood && up2dateEventsGood && outdatedEventsGood && outdatedOrMissingGood;
    }


    /**
     * Perform a search for items matching the given criteria
     *
     * @return An iterator over the found items
     * @throws CommunicationException if the communication failed
     */
    public Iterator<T> search(boolean details, Query<T> query) throws CommunicationException {
       return new SolrProxyIterator<>(toQueryString(query),details,summaSearch,premisManipulatorFactory,domsEventStorage);
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

        for (String failedPastEvent : query.getPastFailedEvents()) {
            events.add(spaced("+" + FAILEVENT + ":" + quoted(failedPastEvent)));
        }
        for (String futureEvent : query.getFutureEvents()) {
            events.add(spaced("-" + SUCCESSEVENT + ":" + quoted(futureEvent)));
            events.add(spaced("-" + FAILEVENT + ":" + quoted(futureEvent)));
        }

        for (String outdatedEvent : query.getOutdatedEvents()) {
            events.add(spaced("+" + OUTDATEDEVENT + ":" + quoted(outdatedEvent)));
        }
        for (String up2dateEvent : query.getUp2dateEvents()) {
            events.add(spaced("+" + UP2DATEEVENT + ":" + quoted(up2dateEvent)));
        }
        for (String outdatedOrMissing : query.getOutdatedOrMissingEvents()) {
            events.add(" +( ( ");
            events.add(spaced("-" + SUCCESSEVENT + ":" + quoted(outdatedOrMissing)));
            events.add(spaced("-" + FAILEVENT + ":" + quoted(outdatedOrMissing)));
            events.add(" ) OR ");
            events.add(spaced("+" + OUTDATEDEVENT + ":" + quoted(outdatedOrMissing)));
            events.add(" ) ");
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
