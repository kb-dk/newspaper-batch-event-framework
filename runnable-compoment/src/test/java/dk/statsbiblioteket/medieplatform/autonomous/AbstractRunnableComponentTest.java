package dk.statsbiblioteket.medieplatform.autonomous;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sun.jersey.core.util.Base64;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.util.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class AbstractRunnableComponentTest {
    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String ROUND_TRIP_TEMPLATE = "doms:Template_RoundTrip";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";

    @Test
    public void testBatchStructureFromFileSystem() throws Exception {
        Properties properties = new Properties(System.getProperties());

        File temp = createTempDir();
        properties.setProperty(ConfigConstants.AUTONOMOUS_BATCH_STRUCTURE_STORAGE_DIR, temp.getAbsolutePath());
        TestingRunnableComponent component = new TestingRunnableComponent(properties);

        String testData = "<test>hej, this is test data</test>";

        Batch batch = new Batch("5000");
        component.storeBatchStructure(batch, new ByteArrayInputStream(testData.getBytes()));
        InputStream retrieved = component.retrieveBatchStructure(batch);
        String retrievedString = toString(retrieved);
        Assert.assertEquals(testData, retrievedString);
    }

    private String toString(InputStream retrieved) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Streams.pipe(retrieved, out);
        return out.toString();
    }

    private File createTempDir() throws IOException {
        File temp = File.createTempFile("folder-name", "");
        temp.delete();
        temp.mkdir();
        return temp;
    }

    @Test
    public void testBatchStructureFromDoms() throws Exception {
        System.out.println("Testing batch structure storing vs. DOMS");
        Properties properties = new Properties(System.getProperties());
        //        properties.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));

        properties.setProperty(
                ConfigConstants.ITERATOR_USE_FILESYSTEM, Boolean.FALSE.toString());
        String username = "username";
        properties.setProperty(
                ConfigConstants.DOMS_USERNAME, username);
        String password = "password";
        properties.setProperty(
                ConfigConstants.DOMS_PASSWORD, password);

        TestingRunnableComponent component = new TestingRunnableComponent(properties);


        Batch batch = new Batch("5000");
        String pid = "uuid:" + UUID.randomUUID().toString();
        WireMockServer wireMockServer
                = new WireMockServer(wireMockConfig().port(8089)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();

        properties.setProperty(ConfigConstants.DOMS_URL, "http://localhost:" + wireMockServer.port() + "/fedora");


        WireMock.configureFor("localhost", wireMockServer.port());
        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects?pid=true&query=identifier~path:" + batch.getFullID() + "&maxResults=1&resultFormat=xml"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withBody(
                                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                        "<result xmlns=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:types=\"http://www.fedora.info/definitions/1/0/types/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/types/ http://localhost:7880/fedora/schema/findObjects.xsd\">\n" +
                                        "  <resultList>\n" +
                                        "  <objectFields>\n" +
                                        "      <pid>" + pid + "</pid>\n" +
                                        "  </objectFields>\n" +
                                        "  </resultList>\n" +
                                        "</result>")));

        String batchStructure = "<test>hej, this is test data</test>";
        givenThat(
                WireMock.put(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/BATCHSTRUCTURE?mimeType=text/xml&logMessage=Updating+batch+structure"))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .withRequestBody(
                                equalTo(batchStructure))
                        .willReturn(
                                aResponse().withStatus(201)));

        givenThat(
                WireMock.get(
                        urlEqualTo("/fedora/objects/" + URLEncoder.encode(pid) + "/datastreams/BATCHSTRUCTURE/content?asOfDateTime="))
                        .withHeader(
                                "Authorization", equalTo(encode(username, password.getBytes())))
                        .willReturn(
                                aResponse().withBody(batchStructure)));


        try {
            component.storeBatchStructure(batch, new ByteArrayInputStream(batchStructure.getBytes()));
            InputStream retrieved = component.retrieveBatchStructure(batch);
            String retrievedString = toString(retrieved);
            Assert.assertEquals(batchStructure, retrievedString);
        } finally {
            wireMockServer.stop();
        }
    }

    /**
     * Utility method to initialise an enhanced fedora object
     *
     * @return the enhanced fedora object
     * @throws java.net.MalformedURLException if the URL in "fedora.server" is invalid
     * @throws PIDGeneratorException          if the pid generator webservice choked again. Should not be possible
     * @throws javax.xml.bind.JAXBException   if jaxb fails to understand the wsdl
     */
    private EnhancedFedora getEnhancedFedora(Properties properties) throws
                                                                    MalformedURLException,
                                                                    PIDGeneratorException,
                                                                    JAXBException {
        return new EnhancedFedoraImpl(
                new Credentials(
                        properties.getProperty(ConfigConstants.DOMS_USERNAME, "username"),
                        properties.getProperty(ConfigConstants.DOMS_PASSWORD, "password")),
                properties.getProperty(ConfigConstants.DOMS_URL),
                properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL, "null"),
                null);
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
