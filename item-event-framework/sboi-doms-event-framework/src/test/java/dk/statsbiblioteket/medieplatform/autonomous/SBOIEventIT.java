package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class SBOIEventIT {

    @Test(groups = {"externalTest"}, enabled = true)
    public void testOutdatedOldItemsFromSumma() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);

        // Search for items
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getPastSuccessfulEvents().add("Data_Received");
        query.getTypes().add("doms:ContentModel_Roundtrip");
        Iterator<Item> items = summa.search(false, query);

        // Find an item with at least one current event, and at least one outdated event
        Event upToDateEvent = null;
        Event oldEvent = null;
        Item item = null;
        while (items.hasNext()) {

            Item it = items.next();
            HashMap<String, Event> eventMap = new HashMap<>();

            upToDateEvent = null;
            oldEvent = null;
            for (Event event : it.getEventList()) {
                Event existing = eventMap.get(event.getEventID());
                if (existing == null || existing.getDate().before(event.getDate())) {
                    eventMap.put(event.getEventID(), event);
                }
            }
            for (Event event : eventMap.values()) {
                if (event.isSuccess() && event.getDate().after(it.getLastModified())) {
                    upToDateEvent = event;
                }
                if (event.isSuccess() && event.getDate().before(it.getLastModified())) {
                    oldEvent = event;
                }
            }
            if (upToDateEvent != null && oldEvent != null ) {
                item = it;
                break;
            }
        }
        assertNotNull(item, "No item found with both old and current events");

        // Test1: Old event found
        EventTrigger.Query<Item> query2 = new EventTrigger.Query<>();
        query2.getOldEvents().add(oldEvent.getEventID());
        query2.getItems().add(item);
        query2.getTypes().add("doms:ContentModel_Roundtrip");
        Iterator<Item> items2 = summa.search(false, query2);
        Assert.assertTrue(items2.hasNext(), "No items Found");

        // Test2: Current event not found
        EventTrigger.Query<Item> query3 = new EventTrigger.Query<>();
        query3.getOldEvents().add(upToDateEvent.getEventID());
        query3.getItems().add(item);
        query3.getTypes().add("doms:ContentModel_Roundtrip");
        Iterator<Item> items3 = summa.search(false, query3);
        Assert.assertFalse(items3.hasNext(), "Unexpected item found");
    }

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetItemsFromSumma() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getOldEvents().add("Test_Event");
        query.getTypes().add("doms:ContentModel_Item");
        Iterator<Item> items = summa.search(false, query);
        Assert.assertTrue(items.hasNext(), "No items Found");
    }

    @Test(groups = {"externalTest"}, enabled = true)
    public void testGetItemsFromDoms() throws Exception {
        Properties props = getProperties();

        SBOIEventIndex<Item> summa = getSboiClient(props);
        EventTrigger.Query<Item> query = new EventTrigger.Query<>();
        query.getOldEvents().add("Test_Event");
        query.getTypes().add("doms:ContentModel_Item");
        Iterator<Item> items = summa.search(true, query);
        Assert.assertTrue(items.hasNext(), "No items Found");
    }

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
        ItemFactory<Item> itemFactory = Item::new;

        DomsEventStorage<Item> domsEventStorage =
                new DomsEventStorageFactory<>()
                        .setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL))
                        .setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME))
                        .setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD))
                        .setItemFactory(itemFactory)
                        .build();

        return new SBOIEventIndex<>(props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL),
                                           new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE,
                                                                          itemFactory),
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
