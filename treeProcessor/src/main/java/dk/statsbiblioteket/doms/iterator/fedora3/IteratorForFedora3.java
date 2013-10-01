package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class IteratorForFedora3 extends AbstractIterator<String> {


    private static final Pattern MODEL_PATTERN = Pattern.compile(
            Pattern.quote("<model>")
                    + "\\s*info:fedora/([^<]*)"
                    + Pattern.quote("</model>"));
    private static final Pattern RELATIONS_PATTERN = Pattern.compile("<[^<>]*>\\s+<([^<>]*)>\\s+<info:fedora/([^<>]*)>\\s+\\.");
    private static final Pattern DATASTREAMS_PATTERN = Pattern.compile(Pattern.quote("<datastream")
            + "\\s+dsid=\"([^\"]*)\"");


    private final List<String> types;
    private final Client client;
    private final String restUrl;
    private ContentModelFilter filter;

    protected IteratorForFedora3(String id, Client client, String restUrl, ContentModelFilter filter) {
        super(id);
        this.client = client;
        this.restUrl = restUrl;
        this.filter = filter;
        types = getTypes(id, client);
    }

    private List<String> getTypes(String id, Client client) {
        WebResource resource = client.resource(restUrl);
        String profileXML = resource.path(id).queryParam("format", "xml").get(String.class);
        return parseModelsFromProfile(profileXML);

    }

    private List<String> parseModelsFromProfile(String profileXML) {
        Matcher matcher = MODEL_PATTERN.matcher(profileXML);
        ArrayList<String> result = new ArrayList<String>();
        while (matcher.find()) {
            String model = matcher.group(1);
            result.add(model);
        }
        return result;
    }

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
    protected Iterator<TreeIterator> initializeChildrenIterator() {
        WebResource resource = client.resource(restUrl);
        //remember to not urlEncode the id here... Stupid fedora
        String relationsShips = resource.path(id).path("relationships").queryParam("format", "ntriples").get(String.class);
        List<String> children = parseRelationsToList(relationsShips, types);
        List<TreeIterator> result = new ArrayList<>(children.size());
        for (String child : children) {
            result.add(makeDelegate(id,child));
        }
        return result.iterator();
    }

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


    @Override
    protected Iterator<String> initilizeAttributeIterator() {
        WebResource resource = client.resource(restUrl);
        String datastreamXml = resource.path(id).path("datastreams").queryParam("format", "xml").get(String.class);

        return parseDatastreamsFromXml(datastreamXml, types).iterator();
    }


    protected AbstractIterator makeDelegate(String id, String childID) {
        return new IteratorForFedora3(childID, client, restUrl, filter);
    }

    @Override
    protected AttributeEvent makeAttributeEvent(String id, String attributeID) {
        return new JerseyAttributeEvent(getIdOfAttribute(attributeID),
                client.resource(restUrl).path(id).path("/datastreams/").path(attributeID).path("/content"));
    }
}


