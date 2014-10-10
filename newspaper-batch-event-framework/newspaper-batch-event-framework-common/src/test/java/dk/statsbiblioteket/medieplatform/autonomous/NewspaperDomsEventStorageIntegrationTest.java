package dk.statsbiblioteket.medieplatform.autonomous;


import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class NewspaperDomsEventStorageIntegrationTest {

    @Test(groups = {"externalTest"})
    public void testAddEventToBatch1() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        DomsEventStorageFactory<Item> factory = new DomsEventStorageFactory<>();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
        factory.setItemFactory(new DomsItemFactory());

        DomsEventStorage<Item> domsEventStorage = factory.createDomsEventStorage();

        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        Date timestamp = new Date(0);
        String eventID = "Data_Received";
        String details = "Details here";

        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(creds,
                                                                  props.getProperty(ConfigConstants.DOMS_URL)
                                                                       .replaceFirst("/(objects)?/?$", ""),
                                                                  props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                                                                  null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();


        try {
            domsEventStorage.addEventToItem(new Batch(batchId, roundTripNumber),
                                                   "agent",
                                                   timestamp,
                                                   details,
                                                   eventID,
                                                   true);

            Item item = domsEventStorage.getItemFromFullID(Batch.formatFullID(batchId, roundTripNumber));
            Assert.assertEquals(item.getFullID(), Batch.formatFullID(batchId, roundTripNumber));

            boolean found = false;
            for (Event event : item.getEventList()) {
                if (event.getEventID().equals(eventID)) {
                    found = true;
                    Assert.assertEquals(event.getDate(), timestamp);
                    Assert.assertEquals(event.getDetails(), details);
                    Assert.assertEquals(event.isSuccess(), true);
                }
            }
            Assert.assertTrue(found);


            Integer newRoundTripNumber = roundTripNumber + 5;
            domsEventStorage.addEventToItem(new Batch(batchId, newRoundTripNumber),
                                                   "agent",
                                                   timestamp,
                                                   details,
                                                   eventID,
                                                   true);

            item = domsEventStorage.getItemFromFullID(Batch.formatFullID(batchId, newRoundTripNumber));
            Assert.assertEquals(item.getFullID(), Batch.formatFullID(batchId, newRoundTripNumber));

            found = false;
            for (Event event : item.getEventList()) {
                if (event.getEventID().equals(eventID)) {
                    found = true;
                    Assert.assertEquals(event.getDate(), timestamp);
                    Assert.assertEquals(event.getDetails(), details);
                    Assert.assertEquals(event.isSuccess(), true);
                }
            }
            Assert.assertTrue(found);
        } finally {
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatBatchID(batchId)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(Batch.formatFullID(batchId, roundTripNumber)))
                        .get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
        }
    }


    @Test(groups = {"externalTest"})
    public void testGetAllRoundtrips() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        NewspaperDomsEventStorageFactory factory = new NewspaperDomsEventStorageFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
        factory.setItemFactory(new BatchItemFactory());

        NewspaperDomsEventStorage domsEventStorage = factory.createDomsEventStorage();

        String batchId = getRandomBatchId();
        Date timestamp = new Date(0);
        String eventID = "Data_Received";
        String details = "Details here";
        final List<Batch> allRoundTrips = domsEventStorage.getAllRoundTrips(batchId);
        int n;
        if (allRoundTrips != null) {
            n = allRoundTrips.size();
            System.out.println("Found '" + n + "' events before test");
        } else {
            n = 0;
        }
        final Batch item1 = new Batch(batchId, 1);
        domsEventStorage.addEventToItem(item1, "agent", timestamp, details, eventID, true);
        final Batch item4 = new Batch(batchId, 4);
        domsEventStorage.addEventToItem(item4, "agent", timestamp, details, eventID, true);
        final Batch item2 = new Batch(batchId, 2);
        domsEventStorage.addEventToItem(item2, "agent", timestamp, details, eventID, true);
        List<Batch> roundtrips = domsEventStorage.getAllRoundTrips(batchId);
        assertEquals(roundtrips.size(), 3 + n);
        //Note that the following asserts fail if the sorting step in getAllRoundTrips() is removed
        //because the roundtrips are returned in the order created.
        assertEquals(roundtrips.get(0 + n).getFullID(), item1.getFullID());
        assertEquals(roundtrips.get(2 + n).getFullID(), item4.getFullID());
    }


    /**
     * In this test, we add a failure to the list with a timestamp earlier than all other events and then
     * trigger the restart. This should empty the event list. We also check that calling the trigger again does not
     * result in an error.
     *
     * @throws Exception
     */
    @Test(groups = {"externalTest"})
    public void testTriggerWorkflowRestartEmptyEventList() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        NewspaperDomsEventStorageFactory factory = new NewspaperDomsEventStorageFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));

        NewspaperDomsEventStorage eventStorer = factory.createDomsEventStorage();

        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(creds,
                                                                  props.getProperty(ConfigConstants.DOMS_URL)
                                                                       .replaceFirst("/(objects)?/?$", ""),
                                                                  props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                                                                  null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();

        try {
            String details = "Details here";

            final Batch batch = new Batch(batchId, roundTripNumber);
            eventStorer.addEventToItem(batch, "agent", new Date(-1000L), details, "e1", false);

            eventStorer.triggerWorkflowRestartFromFirstFailure(batch, 10, 1000L);


            String pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batch.getFullID())).get(0);
            String events = fedora.getXMLDatastreamContents(pid, "EVENTS");
            assertFalse(events.contains("event"), events);
            eventStorer.triggerWorkflowRestartFromFirstFailure(batch, 10, 1000L);
        } finally {
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatBatchID(batchId)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(Batch.formatFullID(batchId, roundTripNumber)))
                        .get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
        }
    }


    private String getRandomBatchId() {
        return "46662202" + Math.round(Math.random() * 10000);
    }
}