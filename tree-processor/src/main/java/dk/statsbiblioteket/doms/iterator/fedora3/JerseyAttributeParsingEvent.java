package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;

import java.io.IOException;
import java.io.InputStream;

/**
 * An fedora attribute event, implemented from the jersey rest client
 */
public class JerseyAttributeParsingEvent extends AttributeParsingEvent {
    private WebResource resource;

    public JerseyAttributeParsingEvent(String localname,
                                       WebResource resource) {
        super(localname);
        this.resource = resource;
    }

    @Override
    public InputStream getText() throws IOException {
        ClientResponse response = resource.get(ClientResponse.class);
        if (response.getStatus() >= 200 && response.getStatus() < 300){
            return response.getEntityInputStream();
        }
        throw new IOException(response.getStatus()+"");
    }

    @Override
    public String getChecksum()
            throws
            IOException {
        //TODO
        return null;
    }
}
