package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public abstract class TCKTestSuite {


    @Deprecated
    public abstract boolean isRunNrInBatchID();

    public abstract DataSource getDataSource();

    public abstract String getValidBatchID();

    public abstract String getInvalidBatchID();

    public abstract String getValidAndSucessfullEventIDForValidBatch();

    public abstract String getInvalidEventIDForValidBatch();


    @Test(groups = "integrationTest")
    public void testGetBatches() throws NotWorkingProperlyException {
        List<Batch> batches = getDataSource().getBatches(false, null);
        assertTrue(batches.size() > 0, "The datasource have no content");
        boolean validHaveBeenFound = false;
        boolean anEventHaveBeenSeen = false;
        for (Batch batch : batches) {
            List<Event> eventList = batch.getEventList();
            assertNotNull(eventList, "The event list cannot be null");
            if (eventList.size() > 0) {
                anEventHaveBeenSeen = true;
            }
            if (batch.getBatchID().equals(getValidBatchID())) {
                validHaveBeenFound = true;
                boolean goodEventFound = false;
                for (Event event : eventList) {
                    if (event.getEventID().equals(getValidAndSucessfullEventIDForValidBatch())) {
                        assertTrue(event.isSuccess(), "The successful event must be successful");
                        goodEventFound = true;
                    }
                }
                assertTrue(goodEventFound, "The good event was not found for the valid batch");
            }
            for (Event event : eventList) {
                // Assert.assertNull(event.getDetails(),"We requested no details, so that must be null");

            }
        }
        if (!validHaveBeenFound) {
            fail("Failed to find the valid ID among all the batches");
        }
        if (!anEventHaveBeenSeen) {
            fail("None of the batches have any events. Quite boring, right?");
        }
    }

    @Test(groups = "integrationTest")
    public void testGetInvalidBatch() throws NotWorkingProperlyException {
        try {
            Batch batch = getDataSource().getBatch(getInvalidBatchID(), false);
            assertNotNull(batch, "Do not return null");
            fail("The invalid batch was found");
        } catch (NotFoundException e) {
            //expected
        }
    }

    @Test(groups = "integrationTest")
    public void testGetValidBatch() throws NotWorkingProperlyException {
        Batch validBatch = null;
        try {

            validBatch = getDataSource().getBatch(getValidBatchID(), true);
            assertNotNull(validBatch, "Do not return null");
        } catch (NotFoundException e) {
            fail("The valid batch was not found", e);
        }
        assertEquals(validBatch.getBatchID(), getValidBatchID(), "The batch have a wrong ID");
        List<Event> eventList = validBatch.getEventList();
        assertTrue(eventList.size() > 0, "The valid batch must have at least one event");
        for (Event event : eventList) {
            // Assert.assertNotNull(event.getDetails(), "We requested details, so that must be not null");
            if (event.getEventID().equals(getValidAndSucessfullEventIDForValidBatch())) {
                assertTrue(event.isSuccess(), "The event must be successful");
            }
        }
    }

    @Test(groups = "integrationTest")
    public void testGetEvent() throws NotWorkingProperlyException {

        Event event = null;
        try {
            event = getDataSource().getBatchEvent(getValidBatchID(), getValidAndSucessfullEventIDForValidBatch(), true);
            assertNotNull(event, "Do not return null");
        } catch (NotFoundException e) {
            fail("The valid batch event was not found", e);
        }
        assertEquals(event.getEventID(), getValidAndSucessfullEventIDForValidBatch(), "The event have a wrong ID");
        //   Assert.assertNotNull(event.getDetails(), "We requested details, so that must be not null");
        assertTrue(event.isSuccess(), "The event must be successful");


    }


    @Test(groups = "integrationTest")
    public void testGetInvalidEvent() throws NotWorkingProperlyException {

        Event event = null;
        try {
            event = getDataSource().getBatchEvent(getValidBatchID(), getInvalidEventIDForValidBatch(), true);
            assertNotNull(event, "Do not return null");
            fail("The invalid event was found");
        } catch (NotFoundException e) {

        }


    }

}
