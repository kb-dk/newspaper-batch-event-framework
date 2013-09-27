package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

public class PremisManipulatorTest {


    public static final Long BATCH_ID = 400022028241l;
    public static final int ROUND_TRIP_NUMBER = 1;

    @Test
    public void testCreateInitialPremisBlob() throws Exception {

        PremisManipulatorFactory factory = new PremisManipulatorFactory(new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
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

        manipulator = manipulator.addEvent("batch_uploaded_trigger",date,"details here", EventID.Data_Received,true);
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
            Assert.assertEquals(event.getEventID(),EventID.Data_Received);
        }

    }
}
