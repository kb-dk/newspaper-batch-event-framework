package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.ws.commons.util.NamespaceContextImpl;
import org.xml.sax.InputSource;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An fedora attribute event, implemented from the jersey rest client
 */
public class JerseyAttributeParsingEvent extends AttributeParsingEvent {
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
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
            try {
                XPath xPath = XPATH_FACTORY.newXPath();
                NamespaceContextImpl context = new NamespaceContextImpl();
                context.startPrefixMapping("dp", "http://www.fedora.info/definitions/1/0/management/");
                xPath.setNamespaceContext(context);
                checksum = xPath.evaluate("//dp:dsChecksum", new InputSource(new ByteArrayInputStream(response.getBytes())));
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Invalid XPath. This is a programming error.", e);
            }
        }
        return checksum;
    }
}
