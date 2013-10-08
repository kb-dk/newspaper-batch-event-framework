package dk.statsbiblioteket.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.netflix.curator.test.TestingServer;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class AutonomousComponentTest {
    private static final Long BATCHID = 40005l;
    private static final int ROUNDTRIPNUMBER = 1;
    TestingServer testingServer;
    AutonomousComponent autonoumous;
    MockupBatchEventClient eventClient;

    @BeforeMethod
    public void setUp() throws Exception {
        testingServer = new TestingServer();
        TestingComponent component = new TestingComponent();

        eventClient = new MockupBatchEventClient();
        Batch testBatch = new Batch();
        testBatch.setBatchID(BATCHID);
        testBatch.setRoundTripNumber(ROUNDTRIPNUMBER);
        Event testEvent = new Event();
        testEvent.setEventID(EventID.Data_Received);
        testEvent.setSuccess(true);
        testEvent.setDate(new Date());
        testEvent.setDetails("");

        testBatch.setEventList(new ArrayList<>(Arrays.asList(testEvent)));

        eventClient.setBatches(new ArrayList<>(Arrays.asList(testBatch)));

        CuratorFramework lockClient = CuratorFrameworkFactory.newClient(testingServer.getConnectString(), new ExponentialBackoffRetry(1000, 3));
        lockClient.start();

        autonoumous = new AutonomousComponent(component, new Properties(), lockClient, eventClient,1);
        autonoumous.setPastEvents(Arrays.asList(EventID.Data_Received.name()));
        autonoumous.setPastEventsExclude(stringList());
        autonoumous.setFutureEvents(stringList());
    }

    /**
     * This test performs the following tasks
     * 1. request a specific batch from the event client, to make sure the thing is there and in the right state
     * 2. starts the autonomous component, with parameters indicating that it should poll for batches in the state checked
     * above.
     * 3. Request the batch afterwards, to check that the new state have been added from the work in 2.
     * @throws Exception
     */
    @Test
    public void testPollAndWork() throws Exception {

        Batch batch = eventClient.getBatch(BATCHID, ROUNDTRIPNUMBER);
        List<Event> events = batch.getEventList();
        boolean testEventFound = false;
        for (Event event : events) {
            if (event.getEventID().equals(EventID.Data_Archived)){
                testEventFound = true;
            }
        }
        Assert.assertFalse("Found test event before test, invalid test",testEventFound);

        autonoumous.call();

        Batch batchAfter = eventClient.getBatch(BATCHID, ROUNDTRIPNUMBER);
        List<Event> eventsAfter = batchAfter.getEventList();

        for (Event event : eventsAfter) {
            if (event.getEventID().equals(EventID.Data_Archived)){
                testEventFound = true;
            }
        }
        Assert.assertTrue("Test event not found after test",testEventFound);

    }

    private List<String> stringList(){
        return new ArrayList<>();
    }
}
