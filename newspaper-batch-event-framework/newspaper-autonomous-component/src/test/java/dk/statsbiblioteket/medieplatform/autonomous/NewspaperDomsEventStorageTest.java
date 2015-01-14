package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.ChecksumType;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class NewspaperDomsEventStorageTest {

    public static final String BATCH_ID = "400022028241";
    public static final int ROUND_TRIP_NUMBER = 1;

    @Test
    public void testAddEventToBatch3() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventStorage<Batch> doms = new NewspaperDomsEventStorage(fedora,
                                                                            PremisManipulatorFactory.TYPE,
                                                                            NewspaperDomsEventStorageFactory.BATCH_TEMPLATE,
                                                                            NewspaperDomsEventStorageFactory.ROUND_TRIP_TEMPLATE,
                                                                            NewspaperDomsEventStorageFactory.HAS_PART,
                                                                            DomsEventStorageFactory.EVENTS,
                                                                            new BatchItemFactory(),1,100);

        doms.addEventToItem(new Batch(BATCH_ID, ROUND_TRIP_NUMBER + 1),
                                   "agent",
                                   new Date(0),
                                   "Details here",
                                   "Data_Received",
                                   true);

        Assert.assertEquals(8, log.size());
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }
    }

    @Test
    public void testAddEventToBatch1() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventStorage<Batch> doms = new NewspaperDomsEventStorage(fedora,
                                                                            PremisManipulatorFactory.TYPE,
                                                                            NewspaperDomsEventStorageFactory.BATCH_TEMPLATE,
                                                                            NewspaperDomsEventStorageFactory.ROUND_TRIP_TEMPLATE,
                                                                            NewspaperDomsEventStorageFactory.HAS_PART,
                                                                            DomsEventStorageFactory.EVENTS,
                                                                            new BatchItemFactory(),1,100);

        doms.addEventToItem(new Batch(BATCH_ID, ROUND_TRIP_NUMBER),
                                   "agent",
                                   new Date(0),
                                   "Details here",
                                   "Data_Received",
                                   true);

        Assert.assertEquals(8, log.size());
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }
    }

    @Test
    public void testAddEventToBatch2() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupBatchNoRoundTripObject(log);
        DomsEventStorage<Batch> doms = new NewspaperDomsEventStorage(fedora,
                                                                     PremisManipulatorFactory.TYPE,
                                                                     NewspaperDomsEventStorageFactory.BATCH_TEMPLATE,
                                                                     NewspaperDomsEventStorageFactory.ROUND_TRIP_TEMPLATE,
                                                                     NewspaperDomsEventStorageFactory.HAS_PART,
                                                                     DomsEventStorageFactory.EVENTS,
                                                                     new BatchItemFactory(),1,100);

        doms.addEventToItem(new Batch(BATCH_ID, ROUND_TRIP_NUMBER),
                                   "agent",
                                   new Date(0),
                                   "Details here",
                                   "Data_Received",
                                   true);

        Assert.assertEquals(log.size(), 3);
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }
    }

    @Test
    public void testCreateBatchRoundTrip() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);
        NewspaperDomsEventStorage doms = new NewspaperDomsEventStorage(fedora,
                                                                              PremisManipulatorFactory.TYPE,
                                                                              NewspaperDomsEventStorageFactory.BATCH_TEMPLATE,
                                                                              NewspaperDomsEventStorageFactory.ROUND_TRIP_TEMPLATE,
                                                                              NewspaperDomsEventStorageFactory.HAS_PART,
                                                                              DomsEventStorageFactory.EVENTS,
                                                                              new BatchItemFactory(),1,100);
        doms.createBatchRoundTrip(new Batch(BATCH_ID, ROUND_TRIP_NUMBER).getFullID());
        Assert.assertEquals(log.size(), 6);
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }
    }

    /**
     * Tests the case where we remove all events after the first error.
     *
     * @throws Exception
     */
    @Test
    public void testTriggerWorkflowRestartFromError() throws Exception {
        EnhancedFedora enhancedFedora = Mockito.mock(EnhancedFedora.class);
        ObjectProfile objectProfile = new ObjectProfile();
        objectProfile.setObjectLastModifiedDate(new Date());
        Mockito.when(enhancedFedora.getObjectProfile(Matchers.anyString(), Matchers.anyLong()))
               .thenReturn(objectProfile);
        PremisManipulator manipulator = getPremisManipulator();
        Mockito.when(enhancedFedora.getXMLDatastreamContents(Matchers.anyString(),
                                                                    Matchers.anyString(),
                                                                    Matchers.anyLong()))
               .thenReturn(manipulator.toXML());
        ArrayList<String> pids = new ArrayList<>();
        pids.add("uuid:thepid");
        Mockito.when(enhancedFedora.findObjectFromDCIdentifier(Matchers.anyString())).thenReturn(pids);

        //The following call means that the 1st attempt to reset the events throws an exception, but the second attempt
        //is successful
        Mockito.doThrow(new ConcurrentModificationException())
               .doReturn(new Date())
               .when(enhancedFedora)
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               Matchers.any(byte[].class),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());

        DomsEventStorage<Batch> doms = new DomsEventStorage<>(enhancedFedora, PremisManipulatorFactory.TYPE,
                                                            DomsEventStorageFactory.EVENTS,
                                                            new BatchItemFactory(), 3,100);
        //Make the call
        int eventsRemoved = doms.triggerWorkflowRestartFromFirstFailure(new Batch("foo", 3));
        assertEquals(eventsRemoved, 6);
        //The captor is used to capture the modified datastream so it can be examined to see if it has been
        //correctly modfied
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

        //There should be two calls to each of these methods, once for each attempt
        Mockito.verify(enhancedFedora, Mockito.times(2))
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               captor.capture(),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());

        //The failed events should have been stripped from the xml
        String newXml = new String(captor.getValue());
        assertTrue(newXml.contains("e1"));
        assertTrue(newXml.contains("e2"));
        assertFalse(newXml.contains("e3"));
        assertFalse(newXml.contains("e4"));
        assertFalse(newXml.contains("e5"));
        assertFalse(newXml.contains("e6"));
        assertFalse(newXml.contains("e7"));
        assertFalse(newXml.contains("e8"));
    }

    /**
     * Tests the case where we remove all events after a given event.
     *
     * @throws Exception
     */
    @Test
    public void testTriggerWorkflowRestartFromNamedEvent() throws Exception {
        EnhancedFedora enhancedFedora = Mockito.mock(EnhancedFedora.class);
        ObjectProfile objectProfile = new ObjectProfile();
        objectProfile.setObjectLastModifiedDate(new Date());
        Mockito.when(enhancedFedora.getObjectProfile(Matchers.anyString(), Matchers.anyLong()))
               .thenReturn(objectProfile);
        PremisManipulator manipulator = getPremisManipulator();
        Mockito.when(enhancedFedora.getXMLDatastreamContents(Matchers.anyString(),
                                                                    Matchers.anyString(),
                                                                    Matchers.anyLong()))
               .thenReturn(manipulator.toXML());
        ArrayList<String> pids = new ArrayList<>();
        pids.add("uuid:thepid");
        Mockito.when(enhancedFedora.findObjectFromDCIdentifier(Matchers.anyString())).thenReturn(pids);

        //The following call means that the 1st attempt to reset the events throws an exception, but the second attempt
        //is successful
        Mockito.doThrow(new ConcurrentModificationException())
               .doReturn(new Date())
               .when(enhancedFedora)
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               Matchers.any(byte[].class),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());

        DomsEventStorage<Batch> doms = new DomsEventStorage<>(enhancedFedora, PremisManipulatorFactory.TYPE,
                                                            DomsEventStorageFactory.EVENTS,
                                                            new BatchItemFactory(), 3,100);
        //Make the call
        int eventsRemoved = doms.triggerWorkflowRestartFromFirstFailure(new Batch("foo", 3), "e5");
        assertEquals(eventsRemoved, 4);
        //The captor is used to capture the modified datastream so it can be examined to see if it has been
        //correctly modfied
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);

        //There should be two calls to each of these methods, once for each attempt
        Mockito.verify(enhancedFedora, Mockito.times(2))
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               captor.capture(),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());

        //The failed events should have been stripped from the xml
        String newXml = new String(captor.getValue());
        assertTrue(newXml.contains("e1"));
        assertTrue(newXml.contains("e2"));
        assertTrue(newXml.contains("e3"));
        assertTrue(newXml.contains("e4"));
        assertFalse(newXml.contains("e5"));
        assertFalse(newXml.contains("e6"));
        assertFalse(newXml.contains("e7"));
        assertFalse(newXml.contains("e8"));
    }

    /**
     * This tests that if the attempt to modify the EVENTS datastream keeps failing then the code will give up after
     * the specified number of attempts.
     *
     * @throws Exception
     */
    @Test
    public void testTriggerWorkflowRestartFails() throws Exception {
        EnhancedFedora enhancedFedora = Mockito.mock(EnhancedFedora.class);
        ObjectProfile objectProfile = new ObjectProfile();
        objectProfile.setObjectLastModifiedDate(new Date());
        Mockito.when(enhancedFedora.getObjectProfile(Matchers.anyString(), Matchers.anyLong()))
               .thenReturn(objectProfile);
        PremisManipulator manipulator = getPremisManipulator();
        Mockito.when(enhancedFedora.getXMLDatastreamContents(Matchers.anyString(),
                                                                    Matchers.anyString(),
                                                                    Matchers.anyLong()))
               .thenReturn(manipulator.toXML());
        ArrayList<String> pids = new ArrayList<>();
        pids.add("uuid:thepid");
        Mockito.when(enhancedFedora.findObjectFromDCIdentifier(Matchers.anyString())).thenReturn(pids);

        //This ensure that the call always throws the exception
        Mockito.doThrow(new ConcurrentModificationException())
               .when(enhancedFedora)
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               Matchers.any(byte[].class),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());
        final int MAX_ATTEMPTS = 10;

        DomsEventStorage<Batch> doms = new DomsEventStorage<>(enhancedFedora, PremisManipulatorFactory.TYPE,
                                                            DomsEventStorageFactory.EVENTS,
                                                            new BatchItemFactory(), MAX_ATTEMPTS,100);
        try {
            int eventsRemoved = doms.triggerWorkflowRestartFromFirstFailure(new Batch("foo", 3));
            fail("Should have thrown a " + CommunicationException.class.getSimpleName());
        } catch (CommunicationException e) {
            //expected
        }
        Mockito.verify(enhancedFedora, Mockito.times(MAX_ATTEMPTS))
               .modifyDatastreamByValue(Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.any(ChecksumType.class),
                                               Matchers.anyString(),
                                               Matchers.any(byte[].class),
                                               Matchers.anyListOf(String.class),
                                               Matchers.anyString(),
                                               Matchers.anyString(),
                                               Matchers.anyLong());
    }

    /**
     * Get a Premis object with some successful and some failed events.
     *
     * @throws JAXBException
     */
    private PremisManipulator getPremisManipulator() throws JAXBException {
        PremisManipulatorFactory<Batch> factory = new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE,
                                                                                        new BatchItemFactory());
        PremisManipulator manipulator = factory.createInitialPremisBlob(Batch.formatFullID(BATCH_ID,
                                                                                                  ROUND_TRIP_NUMBER));
        manipulator = manipulator.addEvent("me", new Date(100), "details here", "e1", true);
        manipulator = manipulator.addEvent("me", new Date(200), "details here", "e2", true);
        manipulator = manipulator.addEvent("me", new Date(300), "details here", "e3", false);
        manipulator = manipulator.addEvent("me", new Date(400), "details here", "e4", true);
        manipulator = manipulator.addEvent("me", new Date(500), "details here", "e5", true);
        manipulator = manipulator.addEvent("me", new Date(600), "details here", "e6", false);
        manipulator = manipulator.addEvent("me", new Date(700), "details here", "e7", true);
        manipulator = manipulator.addEvent("me", new Date(800), "details here", "e8", true);
        return manipulator;
    }
}
