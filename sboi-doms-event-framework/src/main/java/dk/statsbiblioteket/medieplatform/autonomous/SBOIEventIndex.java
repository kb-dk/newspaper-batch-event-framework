package dk.statsbiblioteket.medieplatform.autonomous;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Implementation of the {@link EventAccessor} and {@link EventTrigger} interface using SBOI summa index and DOMS.
 * Uses soap, json and xml to query the summa instance for batches, and REST to get batch details from DOMS. */
public class SBOIEventIndex implements EventTrigger, EventAccessor {

    private static final String SUCCESSEVENT = "success_event";
    private static final String FAILEVENT = "fail_event";
    private static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    private static final String ROUND_TRIP_NO = "round_trip_no";
    private static final String BATCH_ID = "batch_id";
    private static final String UUID = "round_trip_uuid";
    private static final String PREMIS_NO_DETAILS = "premis_no_details";

    private static Logger log = org.slf4j.LoggerFactory.getLogger(SBOIEventIndex.class);
    private final PremisManipulatorFactory premisManipulatorFactory;
    private DomsEventStorage domsEventStorage;
    private final SearchWS summaSearch;

    public SBOIEventIndex(String summaLocation, PremisManipulatorFactory premisManipulatorFactory,
                          DomsEventStorage domsEventStorage) throws MalformedURLException {
        this.premisManipulatorFactory = premisManipulatorFactory;
        this.domsEventStorage = domsEventStorage;
        summaSearch = new SearchWSService(
                new java.net.URL(summaLocation),
                new QName("http://statsbiblioteket.dk/summa/search", "SearchWSService")).getSearchWS();

    }

    @Override
    public Iterator<Batch> findBatches(boolean details, List<String> pastSuccessfulEvents,
                                       List<String> pastFailedEvents, List<String> futureEvents) throws CommunicationException {

        return search(details, null, null, pastSuccessfulEvents, pastFailedEvents, futureEvents);
    }

    @Override
    public Batch getBatch(String batchId, Integer roundTripNumber) throws CommunicationException {
        return domsEventStorage.getBatch(batchId, roundTripNumber);
    }

    @Override
    public Iterator<Batch> getTriggeredBatches(List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                               List<String> futureEvents) throws
                                                                                                       CommunicationException {
        Iterator<Batch> sboiBatches = findBatches(false, pastSuccessfulEvents, pastFailedEvents, futureEvents);
        ArrayList<Batch> result = new ArrayList<>();
        while (sboiBatches.hasNext()) {
            Batch next = sboiBatches.next();
            Batch instead = domsEventStorage.getBatch(next.getDomsID());
            if (match(instead, pastSuccessfulEvents, pastFailedEvents, futureEvents)) {
                result.add(instead);
            }
        }
        return result.iterator();

    }

    /**
     * Check that the batch matches the requirements expressed in the three lists
     *
     * @param batch                the batch to check
     * @param pastSuccessfulEvents events that must be success
     * @param pastFailedEvents     events that must be failed
     * @param futureEvents         events that must not be there
     *
     * @return true if the batch match all requirements
     */
    private boolean match(Batch batch, List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                          List<String> futureEvents) {
        Set<String> successEvents = new HashSet<>();
        Set<String> failEvents = new HashSet<>();
        for (Event event : batch.getEventList()) {
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
    private Iterator<Batch> search(boolean details, String batchID, Integer roundTripNumber,
                                   List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                   List<String> futureEvents) throws CommunicationException {

        try {
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.resultfields", commaSeparate(UUID, getPremisFieldName(details)));

            jsonQuery.put(
                    "search.document.query",
                    toQueryString(batchID, roundTripNumber, pastSuccessfulEvents, pastFailedEvents, futureEvents));
            jsonQuery.put("search.document.startindex", 0);
            jsonQuery.put("search.document.maxrecords", 1000);

            String searchResultString;
            synchronized (summaSearch) {//TODO is this nessesary?
                searchResultString = summaSearch.directJSON(jsonQuery.toString());
            }

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPathSelector xPath = DOM.createXPathSelector();


            NodeList nodeList = xPath.selectNodeList(
                    searchResultDOM, "/responsecollection/response/documentresult/record");

            List<Batch> results = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                String uuid = DOM.selectString(node, "field[@name='" + UUID + "']");
                Batch result = null;
                if (!details) { //no details, so we can retrieve everything from Summa
                    String premis = DOM.selectString(node, "field[@name='" + PREMIS_NO_DETAILS + "']");
                    result = premisManipulatorFactory.createFromBlob(
                            new ByteArrayInputStream(
                                    premis.getBytes())).toBatch();
                    result.setDomsID(uuid);

                } else {//Details requested so go to DOMS
                    result = domsEventStorage.getBatch(uuid);
                }

                results.add(result);

            }
            return results.iterator();
        } catch (Exception e) {
            log.warn("Caught Unknown Exception", e);
            throw new CommunicationException(e);
        }
    }

    private String commaSeparate(String... elements) {
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

    private String getPremisFieldName(boolean details) {
        if (details) {
            return "";
        } else {
            return PREMIS_NO_DETAILS;
        }
    }

    private String spaced(String string) {
        return " " + string.trim() + " ";
    }

    private String quoted(String string) {
        return "\"" + string + "\"";
    }

    /**
     * Format the restrictions as a summa query string
     *
     * @param batchID              the batch id
     * @param roundTripNumber      the round trip number
     * @param successfulPastEvents the successful events the batch must have experienced
     * @param failedPastEvents     the failed events the batch must have experienced
     * @param futureEvents         the events the batch must not have experienced
     *
     * @return the query string
     */
    private String toQueryString(String batchID, Integer roundTripNumber, List<String> successfulPastEvents,
                                 List<String> failedPastEvents, List<String> futureEvents) {

        String base = spaced(RECORD_BASE);
        if (batchID != null) {
            base = base + " " + BATCH_ID + ":B" + batchID;
        }
        if (roundTripNumber != null) {
            base = base + " " + ROUND_TRIP_NO + ":RT" + roundTripNumber.toString();
        }

        StringBuilder events = new StringBuilder();
        if (successfulPastEvents != null) {
            for (String successfulPastEvent : successfulPastEvents) {
                events.append(spaced(SUCCESSEVENT + ":" + quoted(successfulPastEvent)));
            }
        }
        if (failedPastEvents != null) {
            for (String failedPastEvent : failedPastEvents) {
                events.append(spaced(FAILEVENT + ":" + quoted(failedPastEvent)));
            }
        }
        if (futureEvents != null) {
            for (String futureEvent : futureEvents) {
                events.append(spaced("-" + SUCCESSEVENT + ":" + quoted(futureEvent)));
                events.append(spaced("-" + FAILEVENT + ":" + quoted(futureEvent)));
            }
        }
        return base + events.toString();

    }


}
