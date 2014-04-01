package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class TestingComponent implements RunnableComponent {

    private ArrayList<Batch> batches;

    @Override
    public String getComponentName() {
        return "TestingComponent";

    }

    @Override
    public String getComponentVersion() {
        return "0.1-SNAPSHOT";
    }

    @Override
    public String getEventID() {
        return "Data_Archived";
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
    }

    public EventTrigger getEventTrigger() {
        return new EventTrigger() {
            @Override
            public Iterator<Batch> getTriggeredBatches(List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                                       List<String> futureEvents, Batch... batches1)
                    throws CommunicationException {
                return batches.iterator();
            }
        };
    }

    public EventStorer getEventStorer() {
        return new EventStorer() {
            @Override
            public void addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp,
                                        String details, String eventType, boolean outcome)
                    throws CommunicationException {
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
            public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries,
                                                              long waitTime, String eventId)
                    throws CommunicationException, NotFoundException {
                return 0;
            }

            @Override
            public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries,
                                                              long waitTime)
                    throws CommunicationException, NotFoundException {
                return 0;
            }
        };
    }

    public void setBatches(ArrayList<Batch> batches) {
        this.batches = batches;
    }

    public ArrayList<Batch> getBatches() {
        return batches;
    }

    public Batch getBatch(String batchid, int roundtripnumber) {
        for (Batch batch : batches) {
            if (batch.getBatchID().equals(batchid) && batch.getRoundTripNumber().equals(roundtripnumber)) {
                return batch;
            }
        }
        return null;
    }
}
