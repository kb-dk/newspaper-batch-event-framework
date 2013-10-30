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

    private WebResource resource;

    public JerseyAttributeParsingEvent(String name,
                                       WebResource resource) {
        super(name);
        this.resource = resource;
    }

    @Override
    public InputStream getData() throws IOException {
        ClientResponse response = resource.path("/content").get(ClientResponse.class);
        if (response.getStatus() >= 200 && response.getStatus() < 300){
            return response.getEntityInputStream();
        }
        throw new IOException(response.getStatus()+"");
    }

    @Override
    public String getChecksum() throws IOException {
        String response = resource.queryParam("format", "XML").get(String.class);
        Matcher matcher = CHECKSUM_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
