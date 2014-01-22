package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClient;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClientFactory;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperIDFormatter;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public class SBOIClientImplTest {

    @Test(groups = "integrationTest", enabled = true)
    public void testGetBatches() throws Exception {
        Properties props = getProperties();

        SBOIClientImpl summa = getSboiClient(props);
        Iterator<Batch> batches = summa.getBatches(
                false, Arrays.asList("Data_Received"), new ArrayList<String>(), Arrays.asList("Approved"));
        int count = 0;
        while (batches.hasNext()) {
            Batch next = batches.next();
            count++;
        }
        Assert.assertTrue(count > 0, "No batches Found");

    }

    private SBOIClientImpl getSboiClient(Properties props) throws
                                                           MalformedURLException,
                                                           JAXBException,
                                                           PIDGeneratorException {

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        DomsEventClient domsClient = factory.createDomsEventClient();

        return new SBOIClientImpl(
                props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL), new PremisManipulatorFactory(
                new NewspaperIDFormatter(), PremisManipulatorFactory.TYPE), domsClient);
    }

    private Properties getProperties() throws IOException {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));
        return props;
    }
}
