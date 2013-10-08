package dk.statsbiblioteket.autonomous;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

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
    public void addEventToBatch(Long batchId, int roundTripNumber, String agent, Date timestamp, String details, EventID eventType, boolean outcome) throws CommunicationException {
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
    public String createBatchRoundTrip(Long batchId, int roundTripNumber) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Batch getBatch(Long batchId, Integer roundTripNumber) throws CommunicationException {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchId) && batch.getRoundTripNumber() == roundTripNumber){
                return batch;
            }
        }
        return null;
    }

    @Override
    public Batch getBatch(String domsID) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Batch> getBatches(List<EventID> pastEvents, List<EventID> pastEventsExclude, List<EventID> futureEvents) throws CommunicationException {
        return batches.iterator();
    }
}
