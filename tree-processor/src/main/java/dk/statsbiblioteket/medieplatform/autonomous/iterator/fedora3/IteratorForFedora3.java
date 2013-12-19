package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.AbstractIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.util.xml.DOM;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator that iterates objects in a Fedora 3.x repository. It works directly on the
 * REST api.
 */
public class IteratorForFedora3 extends AbstractIterator<String> {
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
    private static final String OBJECTS = "/objects/";
    private static final String INFO_FEDORA = "<info:fedora/";
    private static final String FORMAT = "format";
    private static final String XML = "xml";
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    private static final String DATASTREAM_PROFILE_NAMESPACE = "http://www.fedora.info/definitions/1/0/management/";
    private static final String NTRIPLES = "ntriples";
    /** xpath used to pick out the datastream nodes from the list of datastreams */
    private final XPathExpression datastreamsXpath;
    /** Xpath used to select the dc identifier from the contents of a DC datastream */
    private final XPathExpression dcIdentifierXpath;
    /** Xpath used to pick out the checksum from a datastream profile */
    private final XPathExpression datastreamChecksumXpath;
    /** Xpath used to pick out the datastream name from a datastream profile */
    private final XPathExpression datastreamNameXpath;
    private final Client client;
    private final String restUrl;
    private final FedoraTreeFilter filter;
    private final String name;
    private final Logger log = LoggerFactory.getLogger(getClass());


    /**
     * Constructor.
     *
     * @param id      the fedora pid of the root object
     * @param client  the jersey client to use
     * @param restUrl the url to Fedora
     * @param filter  the fedora tree filter to know which relations and datastreams to use
     */
    public IteratorForFedora3(String id, Client client, String restUrl, FedoraTreeFilter filter,
                              String dataFilePattern) {
        super(id, dataFilePattern);
        this.client = client;
        if (!restUrl.endsWith(OBJECTS)) {
            restUrl = restUrl + OBJECTS;
        }
        this.restUrl = restUrl;
        this.filter = filter;
        try {
            XPath xPath = XPATH_FACTORY.newXPath();
            NamespaceContextImpl context = new NamespaceContextImpl();
            context.startPrefixMapping("dc", DC_NAMESPACE);
            context.startPrefixMapping("dp", DATASTREAM_PROFILE_NAMESPACE);
            xPath.setNamespaceContext(context);
            datastreamsXpath = xPath.compile("//@dsid");
            dcIdentifierXpath = xPath.compile("//dc:identifier");
            datastreamChecksumXpath = xPath.compile("//dp:dsChecksum");
            datastreamNameXpath = xPath.compile("/dp:datastreamProfile/dp:dsAltID");

        } catch (XPathExpressionException e) {
            throw new RuntimeException("Illegal XPath. This is a programming error.", e);
        }
        this.name = getNameFromId(id);
    }

    /**
     * Given an object id, get the name from dc:identifier
     *
     * @param id fedora ID of object
     *
     * @return The name found, or the id if none could be found.
     */
    private String getNameFromId(String id) {
        WebResource resource = client.resource(restUrl);
        String dcContent = resource.path(id).path("/datastreams/DC/content").queryParam(FORMAT, XML).get(String.class);
        NodeList nodeList;
        try {
            nodeList = (NodeList) dcIdentifierXpath.evaluate(
                    DOM.streamToDOM(new ByteArrayInputStream(dcContent.getBytes()), true), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath. This is a programming error.", e);
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            String textContent = nodeList.item(i).getTextContent();
            if (textContent.startsWith("path:")) {
                return textContent.substring("path:".length());
            }
        }
        return id;
    }

    /**
     * Parse the list of datastreams from the datastream xml list. Removes the ones that should
     * not be used, based on the fedora tree filter
     *
     * @param datastreamXml the datastream xml list
     *
     * @return the list of datastreams
     */
    private List<String> parseDatastreamsFromXml(String datastreamXml) {
        NodeList nodeList;
        try {
            nodeList = (NodeList) datastreamsXpath.evaluate(
                    DOM.streamToDOM(new ByteArrayInputStream(datastreamXml.getBytes()), true), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid XPath. This is a programming error.", e);
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String dsid = nodeList.item(i).getTextContent();
            if (filter.isAttributeDatastream(dsid)) {
                result.add(dsid);
            }

        }
        return result;
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        WebResource resource = client.resource(restUrl);
        //remember to not urlEncode the id here... Stupid fedora
        String relationsShips = resource.path(id).path("relationships").queryParam(FORMAT, NTRIPLES).get(String.class);
        List<String> children = parseRelationsToList(relationsShips);
        List<DelegatingTreeIterator> result = new ArrayList<>(children.size());
        for (String child : children) {
            try {
                DelegatingTreeIterator delegate = new IteratorForFedora3(
                        child, client, restUrl, filter, getDataFilePattern());
                result.add(delegate);
            } catch (Exception e) {
                log.warn("Unable to load child {}, ignoring as if it didn't exist", child, e);
            }
        }
        Collections.sort(
                result, new Comparator<DelegatingTreeIterator>() {
            @Override
            public int compare(DelegatingTreeIterator o1, DelegatingTreeIterator o2) {

                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        return result.iterator();
    }

    /**
     * Parse the relationships of the object into a list of fedora pids. Filters out the ones that
     * should be ignored as detailed in the fedora tree filter
     *
     * @param relationsShips the relationships
     *
     * @return the list of pids of the child objects.
     */
    private List<String> parseRelationsToList(String relationsShips) {
        ArrayList<String> result = new ArrayList<>();
        for (String line : relationsShips.split("\n")) {
            String[] tuple = line.split(" ");
            if (tuple.length >= 3 && tuple[2].startsWith(INFO_FEDORA)) {
                String predicate = tuple[1].substring(1, tuple[1].length() - 1);
                String child = tuple[2].substring(INFO_FEDORA.length(), tuple[2].length() - 1);
                if (filter.isChildRel(predicate)) {
                    result.add(child);
                }
            } else {
                log.debug("Ignoring line {}, while parsing predicates", line);
            }
        }
        return result;
    }

    @Override
    protected Iterator<String> initilizeAttributeIterator() {
        WebResource resource = client.resource(restUrl);
        String datastreamXml = resource.path(id).path("datastreams").queryParam(FORMAT, XML).get(String.class);

        return parseDatastreamsFromXml(datastreamXml).iterator();
    }

    /**
     * construct a Attribute parsing event for a node and attributeID. Uses jersey
     * to return an inputstream to the content
     *
     * @param nodeID      the identifier of the node that the attribute resides in
     * @param attributeID the identifier of the attribute.
     *
     * @return the attribute parsing event
     */
    @Override
    protected AttributeParsingEvent makeAttributeEvent(String nodeID, String attributeID) {
        if (attributeID.equals(JerseyContentsAttributeParsingEvent.CONTENTS)) {
            return new JerseyContentsAttributeParsingEvent(
                    name + "/" + attributeID.toLowerCase(), client.resource(restUrl).path(nodeID), nodeID);
        } else {
            String response = client.resource(restUrl)
                                    .path(nodeID)
                                    .path("/datastreams/")
                                    .path(attributeID)
                                    .queryParam(FORMAT, XML)
                                    .get(String.class);
            Document datastreamProfile = DOM.streamToDOM(new ByteArrayInputStream(response.getBytes()), true);


            String name = null;
            String checksum = null;
            try {
                name = datastreamNameXpath.evaluate(datastreamProfile);
                checksum = datastreamChecksumXpath.evaluate(datastreamProfile);
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Invalid XPath. This is a programming error.", e);
            }
            return new JerseyAttributeParsingEvent(
                    name, checksum, client.resource(restUrl).path(nodeID).path("/datastreams/").path(attributeID));
        }
    }


    @Override
    protected String getIdOfNode() {
        return name;
    }
}
