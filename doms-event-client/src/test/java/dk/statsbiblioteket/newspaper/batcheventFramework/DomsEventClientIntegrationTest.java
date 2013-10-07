package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Properties;

public class DomsEventClientIntegrationTest {

    @Test(groups = "integrationTest")
    public void testAddEventToBatch1() throws Exception {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ITtest.properties"));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty("fedora.location"));
        factory.setUsername(props.getProperty("fedora.username"));
        factory.setPassword(props.getProperty("fedora.password"));
        factory.setPidGeneratorLocation(props.getProperty("pidgenerator.location"));



        DomsEventClient doms = factory.createDomsEventClient();


        Long batchId = 400022028242l;
        Integer roundTripNumber = 1;
        Date timestamp = new Date(0);
        EventID eventID = EventID.Data_Received;
        String details = "Details here";
        doms.addEventToBatch(batchId, roundTripNumber,
                "agent",
                timestamp,
                details,
                eventID,
                true);

        Batch batch = doms.getBatch(batchId, roundTripNumber);
        Assert.assertEquals(batch.getBatchID(),batchId);
        Assert.assertEquals(batch.getRoundTripNumber(),roundTripNumber);

        boolean found = false;
        for (Event event : batch.getEventList()) {
            if (event.getEventID().equals(eventID)){
                found = true;
                Assert.assertEquals(event.getDate(), timestamp);
                Assert.assertEquals(event.getDetails(),details);
                Assert.assertEquals(event.isSuccess(),true);
            }
        }
        Assert.assertTrue(found);


        Integer newRoundTripNumber = roundTripNumber + 5;
        doms.addEventToBatch(batchId, newRoundTripNumber,
                "agent",
                timestamp,
                details,
                eventID,
                true);

        batch = doms.getBatch(batchId, newRoundTripNumber);
        Assert.assertEquals(batch.getBatchID(),batchId);
        Assert.assertEquals(batch.getRoundTripNumber(),newRoundTripNumber);

        found = false;
        for (Event event : batch.getEventList()) {
            if (event.getEventID().equals(eventID)){
                found = true;
                Assert.assertEquals(event.getDate(),timestamp);
                Assert.assertEquals(event.getDetails(),details);
                Assert.assertEquals(event.isSuccess(),true);
            }
        }
        Assert.assertTrue(found);


    }

}
