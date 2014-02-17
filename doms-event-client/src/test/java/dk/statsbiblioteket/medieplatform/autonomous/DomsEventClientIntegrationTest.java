package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DomsEventClientIntegrationTest {

    @Test(groups = {"externalTest"})
    public void testAddEventToBatch1() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));


        DomsEventClient doms = factory.createDomsEventClient();


        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        Date timestamp = new Date(0);
        String eventID = "Data_Received";
        String details = "Details here";

        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                creds,
                props.getProperty(ConfigConstants.DOMS_URL).replaceFirst("/(objects)?/?$", ""),
                props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();


        try {
            doms.addEventToBatch(
                    batchId, roundTripNumber, "agent", timestamp, details, eventID, true);

            Batch batch = doms.getBatch(batchId, roundTripNumber);
            Assert.assertEquals(batch.getBatchID(), batchId);
            Assert.assertEquals(batch.getRoundTripNumber(), roundTripNumber);

            boolean found = false;
            for (Event event : batch.getEventList()) {
                if (event.getEventID().equals(eventID)) {
                    found = true;
                    Assert.assertEquals(event.getDate(), timestamp);
                    Assert.assertEquals(event.getDetails(), details);
                    Assert.assertEquals(event.isSuccess(), true);
                }
            }
            Assert.assertTrue(found);


            Integer newRoundTripNumber = roundTripNumber + 5;
            doms.addEventToBatch(
                    batchId, newRoundTripNumber, "agent", timestamp, details, eventID, true);

            batch = doms.getBatch(batchId, newRoundTripNumber);
            Assert.assertEquals(batch.getBatchID(), batchId);
            Assert.assertEquals(batch.getRoundTripNumber(), newRoundTripNumber);

            found = false;
            for (Event event : batch.getEventList()) {
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
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }

        }
    }

    /**
     * Create a round-trip object with an EVENTS datastream. Create a backup of the datastream and check
     * that it is actually identical to the original.
     *
     * @throws Exception
     */
    @Test(groups = {"externalTest"})
    public void testBackupEventsForBatch() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));

        DomsEventClient doms = factory.createDomsEventClient();

        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        Date timestamp = new Date(0);
        String eventID = "Data_Received";
        String details = "Details here";

        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                creds,
                props.getProperty(ConfigConstants.DOMS_URL).replaceFirst("/(objects)?/?$", ""),
                props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();


        try {

            doms.addEventToBatch(batchId, roundTripNumber, "agent", timestamp, details, eventID, true);

            String backupEvents = ((DomsEventClientCentral) doms).backupEventsForBatch(batchId, roundTripNumber);
            assertTrue(
                    backupEvents.matches("EVENTS_[0-9]{1,}"),
                    "Failed to create backup events datastream. Unexpected name '" + backupEvents + "'");
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            String originalEvents = fedora.getXMLDatastreamContents(pid, "EVENTS");
            String newEvents = fedora.getXMLDatastreamContents(pid, backupEvents);
            assertEquals(newEvents, originalEvents);
        } finally {
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatBatchID(batchId)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }

        }

    }

    /**
     * Create a round-trip object with an EVENTS datastream. Call the method to trigger a restart and check that
     * all events from the first failure are removed.
     *
     * @throws Exception
     */
    @Test(groups = {"externalTest"})
    public void testTriggerWorkflowRestart() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties props = new Properties();
        props.load(new FileInputStream(pathToProperties));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));

        DomsEventClient doms = factory.createDomsEventClient();

        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        String details = "Details here";

        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                creds,
                props.getProperty(ConfigConstants.DOMS_URL).replaceFirst("/(objects)?/?$", ""),
                props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();

        try {
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(100), details, "e1", true);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(200), details, "e2", true);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(300), details, "e3", true);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(400), details, "e4", false);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(500), details, "e5", true);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(600), details, "e6", false);
            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(700), details, "e7", true);

            doms.triggerWorkflowRestartFromFirstFailure(batchId, roundTripNumber, 10, 1000L);

            String pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            String events = fedora.getXMLDatastreamContents(pid, "EVENTS");
            assertTrue(events.contains("e1"));
            assertTrue(events.contains("e2"));
            assertTrue(events.contains("e3"));
            assertFalse(events.contains("e4"));
            assertFalse(events.contains("e5"));
            assertFalse(events.contains("e6"));
            assertFalse(events.contains("e7"));
        } finally {
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatBatchID(batchId)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }

        }

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

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty(ConfigConstants.DOMS_URL));
        factory.setUsername(props.getProperty(ConfigConstants.DOMS_USERNAME));
        factory.setPassword(props.getProperty(ConfigConstants.DOMS_PASSWORD));
        factory.setPidGeneratorLocation(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));

        DomsEventClient doms = factory.createDomsEventClient();

        String batchId = getRandomBatchId();
        Integer roundTripNumber = 1;
        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                creds,
                props.getProperty(ConfigConstants.DOMS_URL).replaceFirst("/(objects)?/?$", ""),
                props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null);
        NewspaperIDFormatter formatter = new NewspaperIDFormatter();

        try {
            String details = "Details here";

            doms.addEventToBatch(batchId, roundTripNumber, "agent", new Date(-1000L), details, "e1", false);

            doms.triggerWorkflowRestartFromFirstFailure(batchId, roundTripNumber, 10, 1000L);


            String pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            String events = fedora.getXMLDatastreamContents(pid, "EVENTS");
            assertFalse(events.contains("event"), events);
            doms.triggerWorkflowRestartFromFirstFailure(batchId, roundTripNumber, 10, 1000L);
        } finally {
            String pid = fedora.findObjectFromDCIdentifier(formatter.formatBatchID(batchId)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }
            pid = fedora.findObjectFromDCIdentifier(formatter.formatFullID(batchId, roundTripNumber)).get(0);
            if (pid != null) {
                fedora.deleteObject(pid, "cleaning up after test");
            }

        }

    }

    private String getRandomBatchId() {
        return "4000220252" + Math.round(Math.random() * 100);
    }


}
