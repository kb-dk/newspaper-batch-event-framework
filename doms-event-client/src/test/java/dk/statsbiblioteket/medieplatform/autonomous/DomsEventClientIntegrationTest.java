package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.Date;
import java.util.Properties;

public class DomsEventClientIntegrationTest {

    @Test(groups = "integrationTest")
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


        String batchId = "400022025243";
        Integer roundTripNumber = 1;
        Date timestamp = new Date(0);
        String eventID = "Data_Received";
        String details = "Details here";
        doms.addEventToBatch(
                batchId, roundTripNumber, "agent", timestamp, details, eventID, true);

        Batch batch = doms.getBatch(batchId, roundTripNumber);
        Assert.assertEquals(batch.getBatchID(), batchId);
        Assert.assertEquals(batch.getRoundTripNumber(), roundTripNumber);

        boolean found = false;
        for (Event event : batch.getEventList()) {
            if (event.getEventID()
                     .equals(eventID)) {
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
            if (event.getEventID()
                     .equals(eventID)) {
                found = true;
                Assert.assertEquals(event.getDate(), timestamp);
                Assert.assertEquals(event.getDetails(), details);
                Assert.assertEquals(event.isSuccess(), true);
            }
        }
        Assert.assertTrue(found);


    }

}
