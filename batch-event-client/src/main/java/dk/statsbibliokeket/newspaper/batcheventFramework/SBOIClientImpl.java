package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.SBOIInterface;
import dk.statsbiblioteket.util.xml.DOM;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SBOIClientImpl implements SBOIInterface {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchEventClientImpl.class);
    private DomsEventClient doms;
    private String summaLocation;


    public SBOIClientImpl(DomsEventClient doms, String summaLocation) {
        this.doms = doms;
        this.summaLocation = summaLocation;
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastEvents, List<String> pastEventsExclude, List<String> futureEvents) throws CommunicationException {

        return search(null,pastEvents, pastEventsExclude, futureEvents);
    }

    private Iterator<Batch> search(Long batchID, List<String> pastEvents, List<String> pastEventsExclude, List<String> futureEvents) throws CommunicationException {
        try {
            SearchWS summaSearch = new SearchWSService(new java.net.URL(summaLocation),
                    new QName("http://statsbiblioteket.dk/summa/search", "SearchWSService")).getSearchWS();
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.resultfields", "batchuuid,runuuid");
            jsonQuery.put("search.document.query", toQueryString(batchID,pastEvents,pastEventsExclude,futureEvents));
            jsonQuery.put("search.document.startindex", 0);
            jsonQuery.put("search.document.maxrecords", 10);

            String searchResultString = summaSearch.directJSON(jsonQuery.toString());

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPath xPath = XPathFactory.newInstance().newXPath();


            NodeList nodeList = (NodeList) xPath.evaluate(
                    "//responsecollection/response/documentresult/record/field[@name='runuuid']",
                    searchResultDOM.getDocumentElement(), XPathConstants.NODESET);

            Long hitCount = Long.parseLong((String) (xPath.evaluate(
                    "//responsecollection/response/documentresult/@hitCount",
                    searchResultDOM.getDocumentElement(), XPathConstants.STRING)));

            List<Batch> results = new ArrayList<>(hitCount.intValue());
            for (int i=0; i<nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);

                results.add(doms.getBatch(node.getTextContent().trim()));
            }
            return results.iterator();
        } catch (MalformedURLException e) {
            log.error("caught problemException", e);
            throw new CommunicationException(e);
        } catch (XPathExpressionException e) {
            log.warn("Failed to execute method", e);
            throw new CommunicationException(e);
        } catch (Exception e) {
            log.warn("Caught Unknown Exception", e);
            throw new CommunicationException(e);
        }
    }

    @Override
    public Batch getBatch(Long batchID) throws CommunicationException, NotFoundException {
        Iterator<Batch> result = search(batchID, null, null, null);
        while (result.hasNext()) {
            return result.next();
        }
        throw new NotFoundException("batchid "+batchID.toString()+" not found");
    }


    private String toQueryString(Long batchID, List<String> successfulPastEvents, List<String> failedPastEvents, List<String> futureEvents) {
        String base = " recordBase:doms_sboiCollection ";
        if (batchID != null){
            base = base + " batchid=B"+batchID.toString();
        }

        StringBuilder events = new StringBuilder();
        if (successfulPastEvents != null){
            for (String successfulPastEvent : successfulPastEvents) {
                events.append(" successevent:\""+successfulPastEvent+"\" ");
            }
        }
        if (failedPastEvents != null){
            for (String failedPastEvent : failedPastEvents) {
                events.append(" failevent:\""+failedPastEvent+"\" ");
            }
        }
        if (futureEvents != null){
            for (String futureEvent : futureEvents) {
                events.append(" -sucessevent:\""+futureEvent+"\" ");
            }
        }
        return base + events.toString();

    }



}
