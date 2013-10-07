package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SBOIClientImpl implements SBOIInterface {

    private static final String SUCCESSEVENT = "success_event";
    private static final String FAILEVENT = "fail_event";
    private static final String RECORD_BASE = "recordBase:doms_sboiCollection";
    private static final String ROUND_TRIP_NO = "round_trip_no";
    private static final String BATCH_ID = "batch_id";
    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchEventClientImpl.class);
    private String summaLocation;


    public SBOIClientImpl(String summaLocation) {
        this.summaLocation = summaLocation;
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastEvents,
                                      List<String> pastEventsExclude,
                                      List<String> futureEvents)
            throws
            CommunicationException {

        return search(null, pastEvents, pastEventsExclude, futureEvents);
    }

    private Iterator<Batch> search(Long batchID,
                                   List<String> pastEvents,
                                   List<String> pastEventsExclude,
                                   List<String> futureEvents)
            throws
            CommunicationException {
        try {
            SearchWS
                    summaSearch =
                    new SearchWSService(new java.net.URL(summaLocation),
                                        new QName("http://statsbiblioteket.dk/summa/search",
                                                  "SearchWSService")).getSearchWS();
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.resultfields",
                          BATCH_ID + "," + ROUND_TRIP_NO + "," + SUCCESSEVENT + "," + FAILEVENT + "");
            jsonQuery.put("search.document.query", toQueryString(batchID, pastEvents, pastEventsExclude, futureEvents));
            jsonQuery.put("search.document.startindex", 0);
            jsonQuery.put("search.document.maxrecords", 10);

            String searchResultString = summaSearch.directJSON(jsonQuery.toString());

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPathSelector xPath = DOM.createXPathSelector();


            NodeList
                    nodeList =
                    xPath.selectNodeList(searchResultDOM, "//responsecollection/response/documentresult/record");


            List<Batch> results = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                Batch batch = new Batch();
                batch.setBatchID(Long.parseLong(DOM.selectString(node, "field[@name='" + BATCH_ID + "']")
                                                   .replaceAll("\\D", "")));
                batch.setRoundTripNumber(Integer.parseInt(DOM.selectString(node,
                                                                           "field[@name='" + ROUND_TRIP_NO + "']")
                                                             .replaceAll("\\D", "")));

                List<Event> events = new ArrayList<>();
                NodeList successEvents = DOM.selectNodeList(node, "field[@name='" + SUCCESSEVENT + "']");
                NodeList failEvents = DOM.selectNodeList(node, "field[@name='" + FAILEVENT + "']");
                for (int j = 0; j < successEvents.getLength(); j++) {
                    Node eventNode = successEvents.item(j);
                    Event event = new Event();
                    event.setSuccess(true);
                    try {
                        EventID eventId = EventID.valueOf(eventNode.getTextContent().trim());
                        event.setEventID(eventId);
                    } catch (IllegalArgumentException e) {
                        //illegal event it
                        continue;
                    }
                    events.add(event);

                }
                for (int j = 0; j < failEvents.getLength(); j++) {
                    Node eventNode = failEvents.item(j);
                    Event event = new Event();
                    event.setSuccess(false);
                    try {
                        EventID eventId = EventID.valueOf(eventNode.getTextContent().trim());
                        event.setEventID(eventId);
                    } catch (IllegalArgumentException e) {
                        //illegal event it
                        continue;
                    }
                    events.add(event);

                }
                batch.setEventList(events);
                results.add(batch);
            }
            return results.iterator();
        } catch (MalformedURLException e) {
            log.error("caught problemException", e);
            throw new CommunicationException(e);
        } catch (Exception e) {
            log.warn("Caught Unknown Exception", e);
            throw new CommunicationException(e);
        }
    }

    @Override
    public Batch getBatch(Long batchID)
            throws
            CommunicationException,
            NotFoundException {
        Iterator<Batch> result = search(batchID, null, null, null);
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

    private String toQueryString(Long batchID,
                                 List<String> successfulPastEvents,
                                 List<String> failedPastEvents,
                                 List<String> futureEvents) {

        String base = spaced(RECORD_BASE);
        if (batchID != null) {
            base = base + " " + BATCH_ID + ":B" + batchID.toString();
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
            }
        }
        return base + events.toString();

    }


}
