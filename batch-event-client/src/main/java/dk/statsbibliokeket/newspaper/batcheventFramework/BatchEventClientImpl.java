package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.summasearch.SearchWS;
import dk.statsbiblioteket.doms.central.summasearch.SearchWSService;
import dk.statsbiblioteket.newspaper.batcheventFramework.CommunicationException;
import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClientFactory;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.util.xml.DOM;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BatchEventClientImpl implements BatchEventClient {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchEventClientImpl.class);
    private String summaLocation;
    private String domsUrl;
    private String domsUser;
    private String domsPass;
    private String urlToPidGen;

    public BatchEventClientImpl(String summaLocation, String domsUrl, String domsUser, String domsPass, String urlToPidGen) {
        this.summaLocation = summaLocation;
        this.domsUrl = domsUrl;
        this.domsUser = domsUser;
        this.domsPass = domsPass;
        this.urlToPidGen = urlToPidGen;
    }

    @Override
    public List<Batch> query(List<EventID> successfulPastEvents, List<EventID> failedPastEvents, List<EventID> futureEvents) throws CommunicationException {
        try {
            SearchWS summaSearch = new SearchWSService(new java.net.URL(summaLocation),
                    new QName("http://statsbiblioteket.dk/summa/search", "SearchWSService")).getSearchWS();
            JSONObject jsonQuery = new JSONObject();
            jsonQuery.put("search.document.query", toQueryString(successfulPastEvents,failedPastEvents,futureEvents));
            jsonQuery.put("search.document.startindex", 0);
            jsonQuery.put("search.document.maxrecords", 10);

            String searchResultString = summaSearch.directJSON(jsonQuery.toString());

            Document searchResultDOM = DOM.stringToDOM(searchResultString);
            XPath xPath = XPathFactory.newInstance().newXPath();


            NodeList nodeList = (NodeList) xPath.evaluate(
                    "//responsecollection/response/documentresult/record/field/shortrecord",
                    searchResultDOM.getDocumentElement(), XPathConstants.NODESET);

            java.lang.Long hitCount = java.lang.Long.parseLong((String) (xPath.evaluate(
                    "//responsecollection/response/documentresult/@hitCount",
                    searchResultDOM.getDocumentElement(), XPathConstants.STRING)));

            List<Batch> results = new ArrayList<>(hitCount.intValue());
            for (int i=0; i<nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                Batch batch = new Batch();
                batch.setBatchID(Long.parseLong(xPath.evaluate("batchID",node)));
                batch.setRoundTripNumber(Integer.parseInt(xPath.evaluate("roundTripNumber", node)));
                results.add(batch);
            }
            return results;
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

    private String toQueryString(List<EventID> successfulPastEvents, List<EventID> failedPastEvents, List<EventID> futureEvents) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void addEvent(Batch batch, String premisAgent, Date timestamp, String details, EventID eventID, boolean outcome) throws CommunicationException {
        DomsEventClientFactory domsEventClientFactory = new DomsEventClientFactory();
        domsEventClientFactory.setFedoraLocation(domsUrl);
        domsEventClientFactory.setUsername(domsUser);
        domsEventClientFactory.setPassword(domsPass);
        domsEventClientFactory.setPidGeneratorLocation(urlToPidGen);


        DomsEventClient domsEventClient = null;
        try {
            domsEventClient = domsEventClientFactory.createDomsEventClient();
        } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
            throw new CommunicationException(e);
        }

        domsEventClient.addEventToBatch(batch.getBatchID(),
                    batch.getRoundTripNumber(),
                    premisAgent,
                    timestamp,
                    details,
                    eventID,
                    outcome);

    }
}
