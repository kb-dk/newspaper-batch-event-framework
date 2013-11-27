package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractTests;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class IteratorForFedora3Test extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException, IOException {
        if (iterator == null) {


            Properties properties = new Properties();
            properties.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
            System.out
                  .println(properties.getProperty(ConfigConstants.DOMS_USERNAME));
            Client client = Client.create();
            client.addFilter(
                    new HTTPBasicAuthFilter(
                            properties.getProperty(ConfigConstants.DOMS_USERNAME),
                            properties.getProperty(ConfigConstants.DOMS_PASSWORD)));

            String pid;
            try {
                EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                        new Credentials(
                                properties.getProperty(
                                        ConfigConstants.DOMS_USERNAME),
                                properties.getProperty(ConfigConstants.DOMS_PASSWORD)),
                        properties.getProperty(ConfigConstants.DOMS_URL),
                        null,
                        null);
                pid = getPid(fedora);

            } catch (PIDGeneratorException | BackendMethodFailedException | JAXBException | BackendInvalidCredsException e) {
                throw new RuntimeException(e);
            }

            iterator = new IteratorForFedora3(
                    pid,
                    client,
                    properties.getProperty(ConfigConstants.DOMS_URL),
                    new ConfigurableFilter(
                            Arrays.asList("MODS", "FILM", "EDITION", "ALTO", "MIX"),
                            Arrays.asList("info:fedora/fedora-system:def/relations-external#hasPart")),
                    ConfigConstants.ITERATOR_DATAFILEPATTERN);
        }
        return iterator;
    }

    private String getPid(EnhancedFedoraImpl fedora) throws BackendInvalidCredsException, BackendMethodFailedException {
        String pid;
        List<String> pids = fedora.findObjectFromDCIdentifier("path:B400022028241-RT1");
        pid = pids.get(0);
        return pid;
    }

    @Test(groups = "integrationTest", enabled = true)
    public void testIterator() throws Exception {
        super.testIterator(true, false);
    }

    @Test(groups = "integrationTest", enabled = true)
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping(false, false);
    }
}
