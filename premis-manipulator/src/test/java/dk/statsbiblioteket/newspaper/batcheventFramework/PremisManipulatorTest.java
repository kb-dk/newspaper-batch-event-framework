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


    public static final String BATCH_ID = "B400022028241-RT1";

    @Test
    public void testCreateInitialPremisBlob() throws Exception {
        PremisManipulator manipulator = PremisManipulator.createInitialPremisBlob(BATCH_ID);
        String blobString = manipulator.toString();
        StringReader test = new StringReader(blobString);
        Reader control = new InputStreamReader(getFile("objectOnlyBlob.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(control, test);
        if ( ! diff.identical()){
            System.out.println(diff.toString());
        }
        Assert.assertTrue(diff.similar());
        Assert.assertTrue(diff.identical());
    }

    @Test
    public void testAddEvent() throws Exception {
        PremisManipulator manipulator = PremisManipulator.createInitialPremisBlob(BATCH_ID);
        //2006-07-16T19:20:30+01:00
        Date date = new Date(0);

        manipulator = manipulator.addEvent("batch_uploaded_trigger",date,"details here", EventID.Data_Received,true);
        StringReader test = new StringReader(manipulator.toString());
        Reader control = new InputStreamReader(getFile("eventAddedBlob.xml"));
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(control, test);
        if ( ! diff.identical()){
            System.out.println(manipulator.toString());
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
        PremisManipulator premisBlob = PremisManipulator.createFromBlob(getFile("eventAddedBlob.xml"));
        Batch batch = premisBlob.asBatch();
        Assert.assertEquals("B"+batch.getBatchID()+"-RT"+batch.getRunNr(),BATCH_ID);
        List<Event> events = batch.getEventList();
        for (Event event : events) {
            Assert.assertTrue(event.isSuccess());
            Assert.assertEquals(event.getDetails(),"details here");
            Assert.assertEquals(event.getEventID(),EventID.Data_Received);
        }

    }
}
