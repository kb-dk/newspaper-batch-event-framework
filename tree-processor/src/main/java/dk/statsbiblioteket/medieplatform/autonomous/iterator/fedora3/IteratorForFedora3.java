package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.AbstractIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Iterator that iterates objects in a Fedora 3.x repository. It works directly on the
 * REST api, and parses xml with regular expressions. Not production ready.
 */
public class IteratorForFedora3 extends AbstractIterator<String> {
    // Regexp patterns to parse xml
    private static final Pattern MODEL_PATTERN = Pattern.compile(
            Pattern.quote("<model>")
                    + "\\s*info:fedora/([^<]*)"
                    + Pattern.quote("</model>"));
    private static final Pattern RELATIONS_PATTERN
            = Pattern.compile("<[^<>]*>\\s+<([^<>]*)>\\s+<info:fedora/([^<>]*)>\\s+\\.");
    private static final Pattern DATASTREAMS_PATTERN = Pattern.compile(Pattern.quote(
            "<datastream") + "\\s+dsid=\"([^\"]*)\"");

    //Default values
    public static final String USERNAME = "fedoraAdmin";
    public static final String PASSWORD = "fedoraAdminPass";
    public static final String FEDORA_LOCATION = "http://localhost:8080/fedora";
    public static final String PIDGENERATOR_LOCATION
            = "http://localhost:8080/pidgenerator-service";

    //The initial values of the properties
    private String username = USERNAME;
    private String password = PASSWORD;
    private String fedoraLocation = FEDORA_LOCATION;
    private String pidGeneratorLocation = PIDGENERATOR_LOCATION;

    private final List<String> types;
    private final Client client;
    private final String restUrl;
    private ContentModelFilter filter;
    private EnhancedFedora fedora;

    /**
     * Constructor.
     * @param id the fedora pid of the root object
     * @param client the jersey client to use
     * @param restUrl the url to Fedora
     * @param filter the content model filter to know which relations and datastreams to use
     */
    protected IteratorForFedora3(String id, Client client, String restUrl, ContentModelFilter filter)
            throws CommunicationException {
        super(id);
        this.client = client;
        this.restUrl = restUrl;
        this.filter = filter;
        types = getTypes(id, client);
        Credentials creds = new Credentials(username, password);
        try {
            fedora =  new EnhancedFedoraImpl(creds,
                    fedoraLocation.replaceFirst("/(objects)?/?$", ""),
                    pidGeneratorLocation,
                    null);
        } catch (Exception e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Get the list of content models of an object
     * @param id the pid of the object
     * @param client the jersey client
     * @return the list of pids of content models
     */
    private List<String> getTypes(String id, Client client) {
        WebResource resource = client.resource(restUrl);
        String profileXML = resource.path(id).queryParam("format", "xml").get(String.class);
        return parseModelsFromProfile(profileXML);

    }

    /**
     * Parse the content models from the object profile
     * @param profileXML the object profile in xml
     * @return the list of pids of content models
     */
    private List<String> parseModelsFromProfile(String profileXML) {
        Matcher matcher = MODEL_PATTERN.matcher(profileXML);
        ArrayList<String> result = new ArrayList<String>();
        while (matcher.find()) {
            String model = matcher.group(1);
            result.add(model);
        }
        return result;
    }

    /**
     * Parse the list of datastreams from the datastream xml list. Removes the one that should
     * not be used, based on the content model filter
     * @param datastreamXml the datastream xml list
     * @param types the content models of the object
     * @return the list of datastreams
     */
    private List<String> parseDatastreamsFromXml(String datastreamXml, List<String> types) {
        Matcher matcher = DATASTREAMS_PATTERN.matcher(datastreamXml);
        ArrayList<String> result = new ArrayList<String>();
        while (matcher.find()) {
            String dsid = matcher.group(1);
            if (filter.isAttributeDatastream(dsid, types)) {
                result.add(dsid);
            }

        }
        return result;
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        WebResource resource = client.resource(restUrl);
        //remember to not urlEncode the id here... Stupid fedora
        String relationsShips
                = resource.path(id).path("relationships").queryParam("format",
                "ntriples").get(String.class);
        List<String> children = parseRelationsToList(relationsShips, types);
        List<DelegatingTreeIterator> result = new ArrayList<>(children.size());
        for (String child : children) {
            try {
                DelegatingTreeIterator delegate = makeDelegate(id,child);
                result.add(delegate);
            } catch (Exception e) {
                // Couldn't make delegate, ignore it
            }
        }
        return result.iterator();
    }

    /**
     * Parse the relationships of the object into a list of fedora pids. Filters out the ones that
     * should be ignored as detailed in the content model filter
     * @param relationsShips the relationships
     * @param types the types of the current object
     * @return the list of pids of the child objects.
     */
    private List<String> parseRelationsToList(String relationsShips, List<String> types) {
        Matcher matcher = RELATIONS_PATTERN.matcher(relationsShips);
        ArrayList<String> result = new ArrayList<String>();
        while (matcher.find()) {
            String predicate = matcher.group(1);
            String child = matcher.group(2);
            if (filter.isChildRel(predicate, types)) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Make a delegate iterator for the child
     * @param id the id of this node
     * @param childID the id of the child
     * @return the iterator for the child
     */
    private DelegatingTreeIterator makeDelegate(String id, String childID)
            throws CommunicationException {
        return new IteratorForFedora3(childID, client, restUrl, filter);
    }

    @Override
    protected Iterator<String> initilizeAttributeIterator() {
        WebResource resource = client.resource(restUrl);
        String datastreamXml
                = resource.path(id).path("datastreams").queryParam("format", "xml").get(String.class);

        return parseDatastreamsFromXml(datastreamXml, types).iterator();
    }

    /**
     * construct a Attribute parsing event for a node and attributeID. Uses jersey
     * to return an inputstream to the content
     * @param nodeID the identifier of the node that the attribute resides in
     * @param attributeID the identifier of the attribute.
     * @return the attribute parsing event
     */
    @Override
    protected AttributeParsingEvent makeAttributeEvent(String nodeID, String attributeID) {
        return new JerseyAttributeParsingEvent(attributeID,
                client.resource(restUrl).path(nodeID).path("/datastreams/")
                        .path(attributeID).path("/content"));
    }
}
