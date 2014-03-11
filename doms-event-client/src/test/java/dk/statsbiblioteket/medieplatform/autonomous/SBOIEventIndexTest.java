package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public class SBOIEventIndexTest {

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatches() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex summa = getSboiClient(props);
        Iterator<Batch> batches = summa.getBatches(
                false, Arrays.asList("Data_Received"), new ArrayList<String>(), Arrays.asList("Approved"));
        int count = 0;
        while (batches.hasNext()) {
            Batch next = batches.next();
            count++;
        }
        Assert.assertTrue(count > 0, "No batches Found");

    }

    private SBOIEventIndex getSboiClient(Properties props) throws
                                                           MalformedURLException,
                                                           JAXBException,
                                                           PIDGeneratorException {

        DomsEventStorageFactory factory = new DomsEventStorageFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        DomsEventStorage domsEventStorage = factory.createDomsEventStorage();

        return new SBOIEventIndex(
                props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL), new PremisManipulatorFactory(
                new NewspaperIDFormatter(), PremisManipulatorFactory.TYPE), domsEventStorage);
    }

    private Properties getProperties() throws IOException {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));
        return props;
    }
}
