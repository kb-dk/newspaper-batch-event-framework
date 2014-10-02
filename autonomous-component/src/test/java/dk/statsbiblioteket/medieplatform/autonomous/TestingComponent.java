package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

public class TestingComponent extends AbstractRunnableComponent {

    private ArrayList<Item> batches;

    protected TestingComponent(Properties properties) {
        super(properties);
    }


    @Override
    public String getEventID() {
        return "Data_Archived";
    }


    @Override
    public void doWorkOnItem(Item batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
    }

    public EventTrigger getEventTrigger() {
        return new EventTrigger() {
            @Override
            public Iterator<Item> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                                              Collection<String> pastFailedEvents,
                                                              Collection<String> futureEvents)
                    throws CommunicationException {
                return batches.iterator();
            }

            @Override
            public Iterator<Item> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                                              Collection<String> pastFailedEvents,
                                                              Collection<String> futureEvents,
                                                              Collection<Item> batches) throws
                                                                                                    CommunicationException {
                return batches.iterator();
            }
        };
    }

    public EventStorer getEventStorer() {
        return new EventStorer() {
            @Override
            public Date addEventToItem(Item item, String agent, Date timestamp,
                                        String details, String eventType, boolean outcome)
                    throws CommunicationException {
                return addEvent(item.getFullID(), timestamp, details, eventType, outcome);
            }

            private Date addEvent(String fullId, Date timestamp, String details, String eventType,
                                  boolean outcome) {
                for (Item batch : batches) {
                    if (batch.getFullID().equals(fullId)) {
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
            public int triggerWorkflowRestartFromFirstFailure(Item item, int maxTries,
                                                              long waitTime, String eventId)
                    throws CommunicationException, NotFoundException {
                return 0;
            }

            @Override
            public int triggerWorkflowRestartFromFirstFailure(Item item, int maxTries,
                                                              long waitTime)
                    throws CommunicationException, NotFoundException {
                return 0;
            }
        };
    }

    public void setBatches(ArrayList<Item> batches) {
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
