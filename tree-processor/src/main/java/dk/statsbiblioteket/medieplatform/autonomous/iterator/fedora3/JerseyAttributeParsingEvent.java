package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/** An fedora attribute event, implemented from the jersey rest client */
public class JerseyAttributeParsingEvent extends AttributeParsingEvent {
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
    /** The webresource that represents this datastream */
    protected final WebResource resource;
    /** Contains the checksum. Retrieved and stored on first request */
    private String checksum;

    public JerseyAttributeParsingEvent(String name, String checksum, WebResource resource) {
        super(name);
        this.checksum = checksum;
        this.resource = resource;
    }

    @Override
    public InputStream getData() throws IOException {
        ClientResponse response = resource.path("/content").get(ClientResponse.class);
        if (response.getStatus() >= 200 && response.getStatus() < 300) {
            return response.getEntityInputStream();
        }
        throw new IOException("Unable to get content: " + response.getStatus());
    }

    @Override
    public synchronized String getChecksum() throws IOException {
        return checksum;
    }

    protected void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
