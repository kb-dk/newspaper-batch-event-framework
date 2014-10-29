package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SBOIEventIndexTest {

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatches() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        Iterator<Item> batches = summa.getTriggeredItems(query);
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

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        Iterator<Item> batches = summa.getTriggeredItems(query);
        Item first = batches.next();
        query.getItems().add(first);
        Iterator<Item> batches2 = summa.getTriggeredItems(query);

        Assert.assertEquals(batches2.next(), first);

    }


    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetBatchesFromList() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        Iterator<Item> batches = summa.getTriggeredItems(query);
        Item first = batches.next();
        Item second = batches.next();
        query.getItems().add(first);
        query.getItems().add(second);

        Iterator<Item> batches2 = summa.getTriggeredItems(query);

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


    @Test(groups = {"externalTest"}, enabled = true)
    public void testNoDuplicates() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getFutureEvents().add("Roundtrip_Approved");
        Iterator<Item> batches = summa.getTriggeredItems(query);
        List<Item> list = new ArrayList<>();
        Set<Item> set = new HashSet<>();
        while (batches.hasNext()) {
            Item next = batches.next();
            list.add(next);
            set.add(next);
        }
        Assert.assertEquals(list.size(),set.size());


    }


    private SBOIEventIndex<Item> getSboiClient(Properties props) throws
                                                           MalformedURLException,
                                                           JAXBException,
                                                           PIDGeneratorException {

        DomsEventStorageFactory<Item> factory = new DomsEventStorageFactory<>();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setItemFactory(new DomsItemFactory());
        DomsEventStorage<Item> domsEventStorage = factory.createDomsEventStorage();

        return new SBOIEventIndex<>(props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL),
                                           new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE,
                                                                                 new DomsItemFactory()),
                                           domsEventStorage,
                                           Integer.parseInt(props.getProperty(ConfigConstants.SBOI_PAGESIZE, "100")));
    }

    private Properties getProperties() throws IOException {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));
        return props;
    }
}
