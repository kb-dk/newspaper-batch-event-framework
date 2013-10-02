package dk.statsbiblioteket.doms.iterator.fedora3;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.EventType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyAttributeEvent extends AttributeEvent {
    private WebResource resource;

    public JerseyAttributeEvent(String localname, WebResource resource) {
        super(localname, EventType.Attribute);
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
