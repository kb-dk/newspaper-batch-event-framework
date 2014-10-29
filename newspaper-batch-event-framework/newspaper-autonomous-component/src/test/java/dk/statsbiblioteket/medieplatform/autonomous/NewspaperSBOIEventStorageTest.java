package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class NewspaperSBOIEventStorageTest {

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatches() throws Exception {
        Properties props = getProperties();

        NewspaperSBOIEventStorage summa = getSboiClient(props);
        Iterator<Batch> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"), Arrays.asList("Roundtrip_Approved"));
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

        NewspaperSBOIEventStorage summa = getSboiClient(props);
        Iterator<Batch> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"), Arrays.asList("Roundtrip_Approved"));
        Batch first = batches.next();
        EventTrigger.Query<Batch> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        query.getItems().add(first);
        Iterator<Batch> batches2 = summa.getTriggeredItems(query);

        assertEquals(
                batches2.next(),first);

    }


    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatchesFromList() throws Exception {
        Properties props = getProperties();

        NewspaperSBOIEventStorage summa = getSboiClient(props);
        Iterator<Batch> batches = summa.findItems(false,
                                                                  Arrays.asList("Data_Received"), Arrays.asList("Roundtrip_Approved"));
        Batch first = batches.next();
        Batch second = batches.next();

        EventTrigger.Query<Batch> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        query.getItems().add(first);
        query.getItems().add(second);
        Iterator<Batch> batches2 = summa.getTriggeredItems(query);

        HashSet<Batch> results = new HashSet<>();
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


    private NewspaperSBOIEventStorage getSboiClient(Properties props) throws
                                                           MalformedURLException,
                                                           JAXBException,
                                                           PIDGeneratorException {

        DomsEventStorageFactory<Batch> factory = new NewspaperDomsEventStorageFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        DomsEventStorage<Batch> domsEventStorage = factory.createDomsEventStorage();

        return new NewspaperSBOIEventStorage(props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL),
                                                    new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE,
                                                                                          new BatchItemFactory()),
                                                    domsEventStorage,
                                                    Integer.parseInt(props.getProperty(ConfigConstants.SBOI_PAGESIZE,
                                                                                              "100")));
    }

    private Properties getProperties() throws IOException {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));
        return props;
    }
}
