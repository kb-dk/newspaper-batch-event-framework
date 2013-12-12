package dk.statsbiblioteket.medieplatform.autonomous;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PremisManipulatorTest {


    public static final String BATCH_ID = "400022028241";
    public static final int ROUND_TRIP_NUMBER = 1;

    @Test
    public void testCreateInitialPremisBlob() throws Exception {

        PremisManipulatorFactory
                factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        PremisManipulator manipulator = factory.createInitialPremisBlob(BATCH_ID, ROUND_TRIP_NUMBER);
        String blobString = manipulator.toXML();
        StringReader test = new StringReader(blobString);
        Reader control = new InputStreamReader(getFile("objectOnlyBlob.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff diff = XMLUnit.compareXML(control, test);
        if ( ! diff.identical()){
            System.out.println(diff.toString());
        }
        Assert.assertTrue(diff.similar());
        Assert.assertTrue(diff.identical());
    }

    @Test
    public void testAddEvent() throws Exception {
        PremisManipulatorFactory factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        PremisManipulator manipulator = factory.createInitialPremisBlob(BATCH_ID, ROUND_TRIP_NUMBER);
        Date date = new Date(0);

        manipulator = manipulator.addEvent("batch_uploaded_trigger",date,"details here", "Data_Received",true);
        StringReader test = new StringReader(manipulator.toXML());
        Reader control = new InputStreamReader(getFile("eventAddedBlob.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        Diff diff = XMLUnit.compareXML(control, test);
        if ( ! diff.identical()){
            System.out.println(manipulator.toXML());
            System.out.println(diff.toString());
        }
        Assert.assertTrue(diff.similar());
        Assert.assertTrue(diff.identical());

    }

    private InputStream getFile(String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }

    @Test
    public void testGetAsBatch() throws Exception {
        PremisManipulatorFactory factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        PremisManipulator premisBlob = factory.createFromBlob(getFile("eventAddedBlob.xml"));
        Batch batch = premisBlob.toBatch();
        Assert.assertEquals(BATCH_ID,batch.getBatchID());
        List<Event> events = batch.getEventList();
        for (Event event : events) {
            Assert.assertTrue(event.isSuccess());
            Assert.assertEquals(event.getDetails(),"details here");
            Assert.assertEquals(event.getEventID(),"Data_Received");
        }

    }

    /**
     * Adds a bunch of events to a PREMIS blob. Remove all events after the first failure and check that they are
     * actually removed. Also check (somewhat redundantly) that the resultant blob can still be parsed as Premis.
     * Finally there is an idempotence test that a further call to remove failures has no effect.
     * @throws JAXBException
     */
    @Test
    public void testRemoveEventsAfterFailure() throws JAXBException {
        PremisManipulatorFactory factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        PremisManipulator manipulator = factory.createInitialPremisBlob(BATCH_ID, ROUND_TRIP_NUMBER);
        manipulator = manipulator.addEvent("me", new Date(100), "details here", "e1", true);
        manipulator = manipulator.addEvent("me", new Date(200), "details here", "e2", true);
        manipulator = manipulator.addEvent("me", new Date(300), "details here", "e3", false);
        manipulator = manipulator.addEvent("me", new Date(400), "details here", "e4", true);
        manipulator = manipulator.addEvent("me", new Date(500), "details here", "e5", true);
        manipulator = manipulator.addEvent("me", new Date(600), "details here", "e6", false);
        manipulator = manipulator.addEvent("me", new Date(700), "details here", "e7", true);
        manipulator = manipulator.addEvent("me", new Date(800), "details here", "e8", true);
        assertTrue(manipulator.toXML().contains("e7"));
        manipulator.removeEventsFromFailureOrEvent(null);
        String newXml = manipulator.toXML();
        assertTrue(newXml.contains("e1"));
        assertTrue(newXml.contains("e2"));
        assertFalse(newXml.contains("e3"));
        assertFalse(newXml.contains("e4"));
        assertFalse(newXml.contains("e5"));
        assertFalse(newXml.contains("e6"));
        assertFalse(newXml.contains("e7"));
        assertFalse(newXml.contains("e8"));
        factory.createFromBlob(new ByteArrayInputStream(newXml.getBytes()));
        manipulator.removeEventsFromFailureOrEvent(null);
        String evenNewerXml = manipulator.toXML();
        assertEquals(newXml, evenNewerXml);
    }

    /**
     * Adds a bunch of events to a PREMIS blob. Remove all events after the first failure and check that they are
     * actually removed. Also check (somewhat redundantly) that the resultant blob can still be parsed as Premis.
     * Finally there is an idempotence test that a further call to remove failures has no effect.
     * @throws JAXBException
     */
    @Test
    public void testRemoveEventsAfterNamedEvent() throws JAXBException {
        PremisManipulatorFactory factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        PremisManipulator manipulator = factory.createInitialPremisBlob(BATCH_ID, ROUND_TRIP_NUMBER);
        manipulator = manipulator.addEvent("me", new Date(100), "details here", "e1", true);
        manipulator = manipulator.addEvent("me", new Date(200), "details here", "e2", true);
        manipulator = manipulator.addEvent("me", new Date(300), "details here", "e3", false);
        manipulator = manipulator.addEvent("me", new Date(400), "details here", "e4", true);
        manipulator = manipulator.addEvent("me", new Date(500), "details here", "e5", true);
        manipulator = manipulator.addEvent("me", new Date(600), "details here", "e6", false);
        manipulator = manipulator.addEvent("me", new Date(700), "details here", "e7", true);
        manipulator = manipulator.addEvent("me", new Date(800), "details here", "e8", true);
        assertTrue(manipulator.toXML().contains("e7"));
        int eventsRemoved = manipulator.removeEventsFromFailureOrEvent("e5");
        assertEquals(eventsRemoved, 4);
        String newXml = manipulator.toXML();
        assertTrue(newXml.contains("e1"));
        assertTrue(newXml.contains("e2"));
        assertTrue(newXml.contains("e3"));
        assertTrue(newXml.contains("e4"));
        assertFalse(newXml.contains("e5"));
        assertFalse(newXml.contains("e6"));
        assertFalse(newXml.contains("e7"));
        assertFalse(newXml.contains("e8"));
        factory.createFromBlob(new ByteArrayInputStream(newXml.getBytes()));
        eventsRemoved = manipulator.removeEventsFromFailureOrEvent("e5");
        assertEquals(eventsRemoved, 0);
        String evenNewerXml = manipulator.toXML();
        assertEquals(newXml, evenNewerXml);
    }


}
