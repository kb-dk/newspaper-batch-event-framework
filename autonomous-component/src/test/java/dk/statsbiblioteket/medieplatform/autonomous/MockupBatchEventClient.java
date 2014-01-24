package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;

import java.util.ArrayList;
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
    public void addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp, String details,
                                String eventType, boolean outcome) throws CommunicationException {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchId) && batch.getRoundTripNumber() == roundTripNumber) {
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
        return null;
    }

    @Override
    public Batch getBatch(String batchId, Integer roundTripNumber) throws CommunicationException {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchId) && batch.getRoundTripNumber() == roundTripNumber) {
                return batch;
            }
        }
        return null;
    }


    @Override
    public Batch getBatch(String domsID) throws CommunicationException {
        return null;
    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries, long waitTime,
                                                      String eventId) throws CommunicationException {
        return 0;
    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries,
                                                      long waitTime) throws CommunicationException, NotFoundException {
        return 0;
    }


    @Override
    public Iterator<Batch> getBatches(boolean details, List<String> pastEvents, List<String> pastEventsExclude,
                                      List<String> futureEvents) throws CommunicationException {
        List<Batch> result = new ArrayList<>();
        for (Batch batch : batches) {
            boolean included = true;
            for (Event event : batch.getEventList()) {
                if (pastEvents != null && !pastEvents.contains(event.getEventID())) {
                    included = false;
                }
            }
            for (Event event : batch.getEventList()) {
                if (pastEventsExclude != null && pastEventsExclude.contains(event.getEventID())) {
                    included = false;
                }
            }
            for (Event event : batch.getEventList()) {
                if (futureEvents != null && futureEvents.contains(event.getEventID())) {
                    included = false;
                }
            }
            if (included) {
                result.add(batch);
            }
        }
        return result.iterator();
    }

    @Override
    public Iterator<Batch> getCheckedBatches(boolean details, List<String> pastSuccessfulEvents,
                                             List<String> pastFailedEvents, List<String> futureEvents) throws
                                                                                                       CommunicationException {
        return getBatches(details, pastSuccessfulEvents, pastFailedEvents, futureEvents);
    }

    @Override
    public Batch getBatch(String batchID, Integer roundTripNumber, boolean details) throws
                                                                                    CommunicationException,
                                                                                    NotFoundException {
        return getBatch(batchID, roundTripNumber);
    }
}
