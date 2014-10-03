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
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SBOIEventIndexTest {

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatches() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex summa = getSboiClient(props);
        Iterator<Item> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"),
                                                                  new ArrayList<String>(),
                                                                  Arrays.asList("Roundtrip_Approved"));
        int count = 0;
        while (batches.hasNext()) {
            Item next = batches.next();

            count++;
        }
        Assert.assertTrue(count > 0, "No batches Found");

    }

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatchFromList() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex summa = getSboiClient(props);
        Iterator<Item> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"),
                                                                  new ArrayList<String>(),
                                                                  Arrays.asList("Roundtrip_Approved"));
        Item first = batches.next();
        Iterator< Item> batches2 = summa.search(
                false, Arrays.asList("Data_Received"), new ArrayList<String>(), Arrays.asList("Roundtrip_Approved"), Arrays.asList(first));

        assertEquals(
                batches2.next(),first);

    }


    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatchesFromList() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex summa = getSboiClient(props);
        Iterator<Item> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"),
                                                                  new ArrayList<String>(),
                                                                  Arrays.asList("Roundtrip_Approved"));
        Item first = batches.next();
        Item second = batches.next();

        Iterator<Item> batches2 = summa.search(
                false, Arrays.asList("Data_Received"), new ArrayList<String>(), Arrays.asList("Roundtrip_Approved"), Arrays.asList(first,second));

        HashSet<Item> results = new HashSet<>();
        results.add(first);
        results.add(second);
        assertTrue(results.contains(batches2.next()));
        assertTrue(results.contains(batches2.next()));
        try {
            batches2.next();
            fail();
        } catch (NoSuchElementException e){

        }
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
