package dk.statsbiblioteket.doms.iterator.common;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyAttributeEvent extends Event {
    private WebResource resource;

    public JerseyAttributeEvent(String localname, String path,WebResource resource) {
        super(localname, path, EventType.Attribute);
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
}
