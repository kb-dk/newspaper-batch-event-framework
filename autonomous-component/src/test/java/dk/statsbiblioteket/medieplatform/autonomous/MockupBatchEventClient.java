package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MockupBatchEventClient implements BatchEventClient {

    List<Batch> batches;



    public List<Batch> getBatches() {
        return batches;
    }

    public void setBatches(List<Batch> batches) {
        this.batches = batches;
    }

    @Override
    public void addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp, String details, String eventType, boolean outcome) throws CommunicationException {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchId) && batch.getRoundTripNumber() == roundTripNumber){
                Event event = new Event();
                event.setDate(timestamp);
                event.setEventID(eventType);
                event.setSuccess(outcome);
                event.setDetails(details);
                batch.getEventList().add(event);
            }
        }
    }

    @Override
    public String createBatchRoundTrip(String batchId, int roundTripNumber) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Batch getBatch(String batchId, Integer roundTripNumber) throws CommunicationException {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchId) && batch.getRoundTripNumber() == roundTripNumber){
                return batch;
            }
        }
        return null;
    }

    @Override
    public Iterator<Batch> search(String batchID,
                                  Integer roundTripNumber,
                                  List<String> pastSuccessfulEvents,
                                  List<String> pastFailedEvents,
                                  List<String> futureEvents)
            throws
            CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Batch getBatch(String domsID) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String backupEventsForBatch(String batchId, int roundTripNumber) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastEvents, List<String> pastEventsExclude, List<String> futureEvents) throws CommunicationException {
        return batches.iterator();
    }
}
