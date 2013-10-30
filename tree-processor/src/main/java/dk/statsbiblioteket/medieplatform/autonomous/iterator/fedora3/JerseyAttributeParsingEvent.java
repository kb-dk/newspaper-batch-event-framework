package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An fedora attribute event, implemented from the jersey rest client
 */
public class JerseyAttributeParsingEvent extends AttributeParsingEvent {
    private static final Pattern CHECKSUM_PATTERN = Pattern.compile("<dsChecksum>([^>]*)</dsChecksum>");
    /** The webresource that represents this datastream */
    private final WebResource resource;
    /** Contains the checksum. Retrieved and stored on first request */
    private String checksum;

    public JerseyAttributeParsingEvent(String name, WebResource resource) {
        super(name);
        this.resource = resource;
    }

    @Override
    public InputStream getData() throws IOException {
        ClientResponse response = resource.path("/content").get(ClientResponse.class);
        if (response.getStatus() >= 200 && response.getStatus() < 300){
            return response.getEntityInputStream();
        }
        throw new IOException("Unable to get content: " + response.getStatus());
    }

    @Override
    public String getChecksum() throws IOException {
        if (checksum == null) {
            String response = resource.queryParam("format", "XML").get(String.class);
            Matcher matcher = CHECKSUM_PATTERN.matcher(response);
            if (matcher.find()) {
                checksum = matcher.group(1);
            }
        }
        return checksum;
    }
}
