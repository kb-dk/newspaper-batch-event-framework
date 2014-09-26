package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class TestingComponent implements RunnableComponent {

    private ArrayList<? extends Item> batches;

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
    public void doWorkOnItem(Item batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        doWorkOnItem(batch,resultCollector);
    }

    public EventTrigger getEventTrigger() {
        return new EventTrigger() {
            @Override
            public Iterator<? extends Item> getTriggeredBatches(Collection<String> pastSuccessfulEvents, Collection<String> pastFailedEvents,
                                                       Collection<String> futureEvents)
                    throws CommunicationException {
                return batches.iterator();
            }

            @Override
            public Iterator<? extends Item> getTriggeredBatches(Collection<String> pastSuccessfulEvents,
                                                                Collection<String> pastFailedEvents,
                                                                Collection<String> futureEvents,
                                                                Collection<? extends Item> batches) throws
                                                                                                    CommunicationException {
                return batches.iterator();
            }
        };
    }

    public EventStorer getEventStorer() {
        return new EventStorer() {
            @Override
            public Date addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp,
                                        String details, String eventType, boolean outcome)
                    throws CommunicationException {
                for (Item batch : batches) {
                    if (batch.getFullID().equals(Batch.formatFullID(batchId,roundTripNumber))) {
                        Event event = new Event();
                        event.setDate(timestamp);
                        event.setEventID(eventType);
                        event.setSuccess(outcome);
                        event.setDetails(details);
                        batch.getEventList().add(event);
                    }
                }
                return new Date();
            }

            @Override
            public Date addEventToItem(String itemID, String agent, Date timestamp, String details, String eventType,
                                       boolean outcome) throws CommunicationException {
                //TODO
                return null;
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

    public void setBatches(ArrayList<? extends Item> batches) {
        this.batches = batches;
    }

    public ArrayList<? extends Item> getBatches() {
        return batches;
    }

    public Item getBatch(String batchid, int roundtripnumber) {
        for (Item batch : batches) {
            if (batch.getFullID().equals(Batch.formatFullID(batchid,roundtripnumber))){
                return batch;
            }
        }
        return null;
    }
}
