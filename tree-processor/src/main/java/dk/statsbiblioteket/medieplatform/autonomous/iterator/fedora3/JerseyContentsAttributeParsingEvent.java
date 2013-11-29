package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * An fedora attribute event, implemented from the jersey rest client. This one overrides the getChecksum method, as
 * the checksum is stored as an literal relation in RELS-INT for the content streams.
 */
public class JerseyContentsAttributeParsingEvent extends JerseyAttributeParsingEvent {
    protected static final String CONTENTS = "CONTENTS";
    protected static final String HAS_CHECKSUM = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasMD5";
    private static final String WHITESPACES = "\\s+";
    /** Contains the checksum. Retrieved and stored on first request */
    private WebResource resource;
    private String pid;

    public JerseyContentsAttributeParsingEvent(String name, WebResource resource, String pid) {
        super(
                name,
                null,
                resource.path("/datastreams/")
                        .path(CONTENTS));
        this.resource = resource;

        this.pid = pid;
    }

    @Override
    public synchronized String getChecksum() throws IOException {
        String checksum;
        if (super.getChecksum() == null) {
            String relationsShips = resource.path("relationships")
                                            .queryParam("format", "ntriples")
                                            .queryParam("subject", "info:fedora/" + pid + "/" + CONTENTS)
                                            .queryParam("predicate", URLEncoder.encode(HAS_CHECKSUM, "UTF-8"))
                                            .get(String.class);

            // Example of output: <info:fedora/uuid:0aecd996-ca16-4786-ad70-0d930034d767/CONTENTS> <http://doms.statsbiblioteket.dk/relations/default/0/1/#hasChecksum> "hejsa" .
            String[] splits = relationsShips.trim()
                                            .split(WHITESPACES);
            if (splits.length < 3) {
                //something funky
                return null;
            }
            checksum = splits[2].replaceAll("\"", "")
                                .trim()
                                .toLowerCase();
            setChecksum(checksum);
        }
        return super.getChecksum();
    }
}
