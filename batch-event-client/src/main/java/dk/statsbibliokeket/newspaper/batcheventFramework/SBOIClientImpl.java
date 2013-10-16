package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.newspaper.batcheventFramework.PremisManipulatorFactory;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.SBOIInterface;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the SBOI interface. Uses soap, json and xml to query the summa instance for batches
 */
public class SBOIClientImpl
        implements SBOIInterface {

    private static final String SUCCESSEVENT = "success_event";
    private static final String FAILEVENT = "fail_event";
    private static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    private static final String ROUND_TRIP_NO = "round_trip_no";
    private static final String BATCH_ID = "batch_id";
    private static final String PREMIS = "premis";

    private static Logger log = org.slf4j
            .LoggerFactory
            .getLogger(BatchEventClientImpl.class);
    private String summaLocation;
    private final PremisManipulatorFactory premisManipulatorFactory;

    public SBOIClientImpl(String summaLocation, PremisManipulatorFactory premisManipulatorFactory1) {
        this.summaLocation = summaLocation;
        this.premisManipulatorFactory = premisManipulatorFactory1;
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastSuccessfulEvents,
                                      List<String> pastFailedEvents,
                                      List<String> futureEvents)
            throws
            CommunicationException {

        return search(null, null, pastSuccessfulEvents, pastFailedEvents, futureEvents);
    }

    /**
     * Perform a search for batches matching the given criteria
     * @param batchID the batch id. Can be null for all batches
     * @param roundTripNumber the round trip number. Can be null. These two should both be set or both be null
     * @param pastSuccessfulEvents Events that the batch must have sucessfully experienced
     * @param pastFailedEvents Events that the batch must have experienced, but which failed
     * @param futureEvents Events that the batch must not have experienced
     * @return An iterator over the found batches
     * @throws CommunicationException if the communication failed
     */
    private Iterator<Batch> search(Long batchID,
                                   Integer roundTripNumber,
                                   List<String> pastSuccessfulEvents,
                                   List<String> pastFailedEvents,
                                   List<String> futureEvents)
            throws
            CommunicationException {

        try {
            SearchWS summaSearch = new SearchWSService(new java.net.URL(summaLocation),
                                                       new QName("http://statsbiblioteket.dk/summa/search",
                                                                 "SearchWSService")).getSearchWS();
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.resultfields", PREMIS);
            jsonQuery.put("search.document.query",
                          toQueryString(batchID, roundTripNumber, pastSuccessfulEvents, pastFailedEvents, futureEvents));
            jsonQuery.put("search.document.startindex", 0);
            jsonQuery.put("search.document.maxrecords", 10);

            String searchResultString = summaSearch.directJSON(jsonQuery.toString());

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPathSelector xPath = DOM.createXPathSelector();


            NodeList nodeList =
                    xPath.selectNodeList(searchResultDOM, "//responsecollection/response/documentresult/record");

            List<Batch> results = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                Batch batch = premisManipulatorFactory
                        .createFromBlob(new ByteArrayInputStream(
                                DOM.selectString(node, "field[@name='" + PREMIS + "']").getBytes())).toBatch();
                results.add(batch);
            }
            return results.iterator();
        } catch (Exception e) {
            log.warn("Caught Unknown Exception", e);
            throw new CommunicationException(e);
        }
    }

    /**
     * Retrieve a batch from the summa index
     * @param batchID the batch id
     * @param roundTripNumber the round trip number
     * @return the batch if found
     * @throws CommunicationException if the communication failed
     * @throws NotFoundException if the described batch could not be found
     */
    @Override
    public Batch getBatch(Long batchID,
                          Integer roundTripNumber)
            throws
            CommunicationException,
            NotFoundException {
        Iterator<Batch> result = search(batchID, roundTripNumber, null, null, null);
        while (result.hasNext()) {
            return result.next();
        }
        throw new NotFoundException("batchid " + batchID.toString() + " not found");
    }

    private String spaced(String string) {
        return " " + string.trim() + " ";
    }

    private String quoted(String string) {
        return "\"" + string + "\"";
    }

    /**
     * Format the retrictions as a summa query string
     * @param batchID the batch id
     * @param roundTripNumber the round trip number
     * @param successfulPastEvents the successful events the batch must have experienced
     * @param failedPastEvents the failed events the batch must have experienced
     * @param futureEvents the events the batch must not have experienced
     * @return the query string
     */
    private String toQueryString(Long batchID,
                                 Integer roundTripNumber,
                                 List<String> successfulPastEvents,
                                 List<String> failedPastEvents,
                                 List<String> futureEvents) {

        String base = spaced(RECORD_BASE);
        if (batchID != null) {
            base = base + " " + BATCH_ID + ":B" + batchID.toString();
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
