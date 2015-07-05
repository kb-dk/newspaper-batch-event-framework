package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractTests;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("deprecation")//Credentials
public class IteratorForFedora3TestWireMocked extends AbstractTests {

    private static final String PATH_B400022028241_RT1 = "path:B400022028241-RT1";
    private TreeIterator iterator;


    public WireMockServer wireMockServer;
    private boolean replay = true;

    private boolean record = false;

    /**
     * Control the replay/record behaivour in the ITconfig.properties
     *
     * @throws java.net.URISyntaxException
     * @throws java.io.IOException
     */
    @BeforeMethod(groups = {"standAloneTest"})
    public void shouldWeReplay() throws URISyntaxException, IOException {
        File file = new File(
                Thread.currentThread().getContextClassLoader().getResource("ITconfig.properties").toURI());
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        replay = Boolean.parseBoolean(properties.getProperty("replay", "true"));
        if (!replay) {
            record = Boolean.parseBoolean(properties.getProperty("record", "false"));
        }
    }

    @BeforeMethod(groups = {"standAloneTest"})
    public void setUpReplay() throws Exception {
        /*Generate these replay files by doing this

        wget http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.42/wiremock-1.42-standalone.jar
        java -jar wiremock-1.42-standalone.jar --proxy-all="http://achernar:7880/" --record-mappings --verbose

        Do the test you need to get the recording
        Stop the recording server with Ctrl-C when you have sufficient material

        It will generate two folders, mapping and __files. Copy these to src/test/resources/fedoraIteratorReplay
        */
        if (replay) {

            File file = new File(
                    Thread.currentThread().getContextClassLoader().getResource("ITconfig.properties").toURI());
            File srcTestResources = file.getParentFile();
            File fedoraIteratorReplay = new File(srcTestResources, "fedoraIteratorReplay");
            File mappings = new File(fedoraIteratorReplay, "mappings");

            wireMockServer = new WireMockServer(8089, new SingleRootFileSource(fedoraIteratorReplay), false);
            wireMockServer.start();

            wireMockServer.loadMappingsUsing(
                    new JsonFileMappingsLoader(
                            new SingleRootFileSource(
                                    mappings.getAbsolutePath())));
            WireMock.configureFor("localhost", wireMockServer.port());
        }
    }

    @AfterMethod(groups = {"standAloneTest"})
    public void tearDownReplay() throws Exception {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Override
    public TreeIterator getIterator() throws URISyntaxException, IOException {
        if (iterator == null) {
            Properties properties = new Properties();

            String property = System.getProperty("integration.test.newspaper.properties");
            if (property != null) {
                File file = new File(property);
                if (file.exists()) {
                    properties.load(new FileReader(file));
                }
            }


            Client client = Client.create();
            String username = properties.getProperty(ConfigConstants.DOMS_USERNAME, "fedoraAdmin");
            String password = properties.getProperty(ConfigConstants.DOMS_PASSWORD, "fedoraAdmin");
            System.out.println(username);
            client.addFilter(
                    new HTTPBasicAuthFilter(
                            username, password));

            String pid;
            String domsUrl;
            try {
                if (replay) {
                    //replay from the wireMockServer
                    domsUrl = "http://localhost:" + wireMockServer.port() + "/fedora";
                } else if (record) {
                    //request through the recording server
                    domsUrl = "http://localhost:8080/fedora";
                } else {
                    //Go directly to Fedora
                    domsUrl = properties.getProperty(ConfigConstants.DOMS_URL);
                }
                EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                        new Credentials(
                                username, password), domsUrl, null, null);
                pid = getPid(fedora);

            } catch (PIDGeneratorException | BackendMethodFailedException | JAXBException | BackendInvalidCredsException e) {
                throw new RuntimeException(e);
            }

            iterator = new IteratorForFedora3(
                    pid,
                    client,
                    domsUrl,
                    new ConfigurableFilter(
                            Arrays.asList("MODS", "FILM", "EDITION", "ALTO", "MIX"),
                            Arrays.asList("info:fedora/fedora-system:def/relations-external#hasPart")),
                    ConfigConstants.ITERATOR_DATAFILEPATTERN);
        }
        return iterator;
    }


    private String getPid(EnhancedFedoraImpl fedora) throws BackendInvalidCredsException, BackendMethodFailedException {
        String pid;
        List<String> pids = fedora.findObjectFromDCIdentifier(PATH_B400022028241_RT1);
        pid = pids.get(0);
        return pid;
    }

    @Test(groups = {"standAloneTest"})
    public void testIterator() throws Exception {
        super.testIterator(true, false);
    }

    @Test(groups = {"standAloneTest"})
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping(false, false);
    }
}
