package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.Assert.assertEquals;

public class JerseyContentsAttributeParsingEventTest {

    private static final String PID = "contentTest:1";
    private WebResource objectResource;

    public WireMockServer wireMockServer;
    private String checksum = "checksum";

    @BeforeMethod(groups = "integrationTest")
    public void setUp() throws Exception {
        Properties properties = new Properties();
        String property = System.getProperty("integration.test.newspaper.properties");
        if (property != null) {
            File file = new File(property);
            if (file.exists()) {
                properties.load(new FileReader(file));
            }
        }
        String username = properties.getProperty(ConfigConstants.DOMS_USERNAME, "fedoraAdmin");
        System.out.println(username);
        Client client = Client.create();
        String password = properties.getProperty(ConfigConstants.DOMS_PASSWORD, "fedoraAdmin");
        client.addFilter(
                new HTTPBasicAuthFilter(
                        username, password));

        wireMockServer
                = new WireMockServer(wireMockConfig().port(8089)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        givenThat(
                WireMock.get(
                        urlEqualTo(
                                "/fedora/objects/" + PID + "/relationships?format=ntriples&subject=info:fedora/" + PID +
                                "/CONTENTS&predicate=http%3A%2F%2Fdoms.statsbiblioteket.dk%2Frelations%2Fdefault%2F0%2F1%2F%23hasMD5"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withBody(
                                        "<info:fedora/" + PID + "/CONTENTS> <http://doms.statsbiblioteket.dk/relations/default/0/1/#hasMD5> \"" + checksum + "\" .")));

        WebResource resource = client.resource("http://localhost:" + wireMockServer.port() + "/fedora");
        objectResource = resource.path("/objects/").path(PID);
    }

    @AfterMethod(groups = "integrationTest")
    public void tearDown() throws Exception {
        wireMockServer.stop();

    }

    @Test(groups = "integrationTest")
    public void testGetChecksum() throws Exception {

        AttributeParsingEvent attribute = new JerseyContentsAttributeParsingEvent(
                "testContentName", objectResource, PID);

        assertEquals(attribute.getChecksum(), checksum);
    }

    public String encode(String username, byte[] password) {
        try {

            final byte[] prefix = (username + ":").getBytes("UTF-8");
            final byte[] usernamePassword = new byte[prefix.length + password.length];

            System.arraycopy(prefix, 0, usernamePassword, 0, prefix.length);
            System.arraycopy(password, 0, usernamePassword, prefix.length, password.length);

            return "Basic " + new String(Base64.encode(usernamePassword), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            // This should never occur
            throw new RuntimeException(ex);
        }
    }
}
