package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.AbstractIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

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
    private static final Pattern RELATIONS_PATTERN
            = Pattern.compile("<[^<>]*>\\s+<([^<>]*)>\\s+<info:fedora/([^<>]*)>\\s+\\.");
    private static final Pattern DATASTREAMS_PATTERN = Pattern.compile(Pattern.quote(
            "<datastream") + "\\s+dsid=\"([^\"]*)\"");
    private static final Pattern DC_IDENTIFIER_PATH_PATTERN = Pattern.compile(
            ("<dc:identifier>path:([^<]*)</dc:identifier>"));


    private final Client client;
    private final String restUrl;
    private FedoraTreeFilter filter;
    private String name;

    /**
     * Constructor.
     * @param id the fedora pid of the root object
     * @param name Name of object
     * @param client the jersey client to use
     * @param restUrl the url to Fedora
     * @param filter the fedora tree filter to know which relations and datastreams to use
     */
    public IteratorForFedora3(String id, String name, Client client, String restUrl, FedoraTreeFilter filter) {
        super(id);
        this.name = name;
        this.client = client;
        this.restUrl = restUrl;
        this.filter = filter;
    }

    /**
     * Parse the list of datastreams from the datastream xml list. Removes the ones that should
     * not be used, based on the fedora tree filter
     *
     * @param datastreamXml the datastream xml list
     * @return the list of datastreams
     */
    private List<String> parseDatastreamsFromXml(String datastreamXml) {
        Matcher matcher = DATASTREAMS_PATTERN.matcher(datastreamXml);
        ArrayList<String> result = new ArrayList<>();
        while (matcher.find()) {
            String dsid = matcher.group(1);
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
        String relationsShips
                = resource.path(id).path("relationships").queryParam("format",
                "ntriples").get(String.class);
        List<String> children = parseRelationsToList(relationsShips);
        List<DelegatingTreeIterator> result = new ArrayList<>(children.size());
        for (String child : children) {
            try {
                String DCcontent = resource.path(child).path("/datastreams/DC/content").queryParam("format", "xml").get(String.class);
                Matcher matcher = DC_IDENTIFIER_PATH_PATTERN.matcher(DCcontent);
                if (matcher.find()) {
                    name = matcher.group(1);
                }
                DelegatingTreeIterator delegate = makeDelegate(id, child, name);
                result.add(delegate);
            } catch (Exception e) {
                // Couldn't make delegate, ignore it
            }
        }
        return result.iterator();
    }

    /**
     * Parse the relationships of the object into a list of fedora pids. Filters out the ones that
     * should be ignored as detailed in the fedora tree filter
     *
     * @param relationsShips the relationships
     * @return the list of pids of the child objects.
     */
    private List<String> parseRelationsToList(String relationsShips) {
        Matcher matcher = RELATIONS_PATTERN.matcher(relationsShips);
        ArrayList<String> result = new ArrayList<>();
        while (matcher.find()) {
            String predicate = matcher.group(1);
            String child = matcher.group(2);
            if (filter.isChildRel(predicate)) {
                result.add(child);
            }
        }
        return result;
    }

    /**
     * Make a delegate iterator for the child
     * @param id the id of this node
     * @param childID the id of the child
     * @param childName the name of the child
     * @return the iterator for the child
     */
    private DelegatingTreeIterator makeDelegate(String id, String childID, String childName) {
        return new IteratorForFedora3(childID, childName, client, restUrl, filter);
    }

    @Override
    protected Iterator<String> initilizeAttributeIterator() {
        WebResource resource = client.resource(restUrl);
        String datastreamXml
                = resource.path(id).path("datastreams").queryParam("format", "xml").get(String.class);

        return parseDatastreamsFromXml(datastreamXml).iterator();
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
        return new JerseyAttributeParsingEvent(name+"." + attributeID.toLowerCase() + ".xml",
                client.resource(restUrl).path(nodeID).path("/datastreams/")
                        .path(attributeID));
    }

    @Override
    protected String getIdOfNode() {
        return name;
    }
}
