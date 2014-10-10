package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of the {@link EventAccessor} and {@link EventTrigger} interface using SBOI summa index and DOMS.
 * Uses soap, json and xml to query the summa instance for batches, and REST to get batch details from DOMS.
 */
public class SBOIEventIndex<T extends Item> implements EventTrigger<T>, EventAccessor<T> {

    public static final String SUCCESSEVENT = "success_event";
    public static final String FAILEVENT = "fail_event";
    public static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    public static final String ROUND_TRIP_NO = "round_trip_no";
    public static final String BATCH_ID = "batch_id";
    public static final String UUID = "round_trip_uuid";
    public static final String PREMIS_NO_DETAILS = "premis_no_details";

    private static Logger log = org.slf4j.LoggerFactory.getLogger(SBOIEventIndex.class);
    private final PremisManipulatorFactory<T> premisManipulatorFactory;
    private DomsEventStorage<T> domsEventStorage;
    private final SearchWS summaSearch;

    public SBOIEventIndex(String summaLocation, PremisManipulatorFactory<T> premisManipulatorFactory,
                          DomsEventStorage<T> domsEventStorage) throws MalformedURLException {
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
        summaSearch = new SearchWSService(
                new java.net.URL(summaLocation),
                new QName("http://statsbiblioteket.dk/summa/search", "SearchWSService")).getSearchWS();

    }

    @Override
    public Iterator<T> findItems(boolean details, List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                    List<String> futureEvents) throws
                                                                                                 CommunicationException {

        return search(details, pastSuccessfulEvents, pastFailedEvents, futureEvents,null);
    }

    @Override
    public T getItem(String itemFullID) throws CommunicationException, NotFoundException {
        return domsEventStorage.getItemFromFullID(itemFullID);
    }

    @Override
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents) throws CommunicationException {
        return getTriggeredItems(pastSuccessfulEvents, pastFailedEvents, futureEvents, null);
    }

    @Override
    public Iterator<T> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                            Collection<String> pastFailedEvents, Collection<String> futureEvents,
                                            Collection<T> batches) throws CommunicationException {
        Iterator<T> sboiBatches = search(false, pastSuccessfulEvents, pastFailedEvents, futureEvents,batches);
        ArrayList<T> result = new ArrayList<>();
        while (sboiBatches.hasNext()) {
            T next = sboiBatches.next();
            T instead;
            try {
                instead = domsEventStorage.getItemFromDomsID(next.getDomsID());
            } catch (NotFoundException ignored) {
                continue;
            }
            if (match(instead, pastSuccessfulEvents, pastFailedEvents, futureEvents)) {
                result.add(instead);
            }
        }
        return result.iterator();

    }

    /**
     * Check that the item matches the requirements expressed in the three lists
     *
     * @param item                the item to check
     * @param pastSuccessfulEvents events that must be success
     * @param pastFailedEvents     events that must be failed
     * @param futureEvents         events that must not be there
     *
     * @return true if the item match all requirements
     */
    private boolean match(Item item, Collection<String> pastSuccessfulEvents, Collection<String> pastFailedEvents,
                          Collection<String> futureEvents) {
        Set<String> successEvents = new HashSet<>();
        Set<String> failEvents = new HashSet<>();
        for (Event event : item.getEventList()) {
            if (event.isSuccess()) {
                successEvents.add(event.getEventID());
            } else {
                failEvents.add(event.getEventID());
            }
        }
        return successEvents.containsAll(pastSuccessfulEvents) && failEvents.containsAll(pastFailedEvents) && Collections
                .disjoint(futureEvents, successEvents) && Collections.disjoint(futureEvents, failEvents);
    }




    /**
     * Perform a search for items matching the given criteria
     *
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents     Events that the batch must have experienced, but which failed
     * @param futureEvents         Events that the batch must not have experienced
     * @param items              if not null, the resulting iterator will only contain items from this set. If the
     *                             items is empty, the result will be empty.
     *
     * @return An iterator over the found items
     * @throws CommunicationException if the communication failed
     */
    public Iterator<T> search(boolean details, Collection<String> pastSuccessfulEvents, Collection<String> pastFailedEvents,
                                  Collection<String> futureEvents, Collection<T> items) throws CommunicationException {

        try {
            if (items != null && items.isEmpty()){
                //If the items constraint is set to no result, give no result.
                return new ArrayList<T>().iterator();
            }
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.resultfields", commaSeparate(UUID, BATCH_ID, ROUND_TRIP_NO, getPremisFieldName(details)));

            jsonQuery.put(
                    "search.document.query",
                    toQueryString(pastSuccessfulEvents, pastFailedEvents, futureEvents,items));
            jsonQuery.put("search.document.startindex", 0);
            //TODO fix this static maxrecords  (we can order on creation date)
            jsonQuery.put("search.document.maxrecords", 1000);

            String searchResultString;
            synchronized (summaSearch) {//TODO is this nessesary?
                searchResultString = summaSearch.directJSON(jsonQuery.toString());
            }

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPathSelector xPath = DOM.createXPathSelector();


            NodeList nodeList = xPath.selectNodeList(
                    searchResultDOM, "/responsecollection/response/documentresult/record");

            int hits = nodeList.getLength();
            if (items != null && hits > items.size()){
                hits = items.size();
            }
            List<T> results = new ArrayList<>(hits);

            for (int i = 0; i <hits; ++i) {
                Node node = nodeList.item(i);
                String uuid = DOM.selectString(node, "field[@name='" + UUID + "']");
                T result = null;
                if (!details) { //no details, so we can retrieve everything from Summa
                    String premis = DOM.selectString(node, "field[@name='" + PREMIS_NO_DETAILS + "']");
                    result = premisManipulatorFactory.createFromBlob(new ByteArrayInputStream(premis.getBytes()))
                                                     .toItem();
                    result.setDomsID(uuid);

                } else {//Details requested so go to DOMS
                    result = domsEventStorage.getItemFromDomsID(uuid);
                }

                results.add(result);

            }
            return results.iterator();
        } catch (Exception e) {
            log.warn("Caught Unknown Exception", e);
            throw new CommunicationException(e);
        }
    }

    protected static String commaSeparate(String... elements) {
        StringBuilder result = new StringBuilder();
        for (String element : elements) {
            if (element == null) {
                continue;
            }
            if (result.length() != 0) {
                result.append(",");
            }
            result.append(element);
        }
        return result.toString();
    }

    protected String getPremisFieldName(boolean details) {
        if (details) {
            return "";
        } else {
            return PREMIS_NO_DETAILS;
        }
    }

    protected static String spaced(String string) {
        return " " + string.trim() + " ";
    }

    protected static String quoted(String string) {
        return "\"" + string + "\"";
    }


    protected String toQueryString(Collection<String> pastSuccessfulEvents, Collection<String> pastFailedEvents, Collection<String> futureEvents, Collection<T> items) {
        String base = spaced(RECORD_BASE);

        StringBuilder batchesString = new StringBuilder();
        if (items != null ){
            batchesString.append(" ( ");

            boolean first = true;
            for (Item item : items) {
                if (first){
                    first = false;
                }  else {
                    batchesString.append(" OR ");
                }
                batchesString.append(" ( ");

                batchesString.append(UUID).append(":").append(item.getDomsID());

                batchesString.append(" ) ");

            }
            batchesString.append(" ) ");


        }

        StringBuilder events = new StringBuilder();
        if (pastSuccessfulEvents != null) {
            for (String successfulPastEvent : pastSuccessfulEvents) {
                events.append(spaced("+"+SUCCESSEVENT + ":" + quoted(successfulPastEvent)));
            }
        }
        if (pastFailedEvents != null) {
            for (String failedPastEvent : pastFailedEvents) {
                events.append(spaced("+"+FAILEVENT + ":" + quoted(failedPastEvent)));
            }
        }
        if (futureEvents != null) {
            for (String futureEvent : futureEvents) {
                events.append(spaced("-" + SUCCESSEVENT + ":" + quoted(futureEvent)));
                events.append(spaced("-" + FAILEVENT + ":" + quoted(futureEvent)));
            }
        }
        return base + batchesString.toString() + events.toString();

    }


}
