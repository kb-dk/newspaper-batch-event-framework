package dk.statsbiblioteket.medieplatform.autonomous;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

public class TestingComponent extends AbstractRunnableComponent<Batch> {

    private ArrayList<Batch> items;

    protected TestingComponent(Properties properties) {
        super(properties);
    }


    @Override
    public String getEventID() {
        return "Data_Archived";
    }


    @Override
    public void doWorkOnItem(Batch batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
    }

    public EventTrigger<Batch> getEventTrigger() {
        return new EventTrigger<Batch>() {
            @Override
            public Iterator<Batch> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                                              Collection<String> pastFailedEvents,
                                                              Collection<String> futureEvents)
                    throws CommunicationException {
                return items.iterator();
            }

            @Override
            public Iterator<Batch> getTriggeredItems(Collection<String> pastSuccessfulEvents,
                                                              Collection<String> pastFailedEvents,
                                                              Collection<String> futureEvents,
                                                              Collection<Batch> itemCollection) throws
                                                                                                    CommunicationException {
                return itemCollection.iterator();
            }
        };
    }

    public EventStorer<Batch> getEventStorer() {
        return new EventStorer<Batch>() {
            @Override
            public Date addEventToItem(Batch item, String agent, Date timestamp,
                                        String details, String eventType, boolean outcome)
                    throws CommunicationException {
                return addEvent(item.getFullID(), timestamp, details, eventType, outcome);
            }

            private Date addEvent(String fullId, Date timestamp, String details, String eventType,
                                  boolean outcome) {
                for (Batch item : items) {
                    if (item.getFullID().equals(fullId)) {
                        Event event = new Event();
                        event.setDate(timestamp);
                        event.setEventID(eventType);
                        event.setSuccess(outcome);
                        event.setDetails(details);
                        item.getEventList().add(event);
                    }
                }
                return new Date();
            }


            @Override
            public int triggerWorkflowRestartFromFirstFailure(Batch item, int maxTries,
                                                              long waitTime, String eventId)
                    throws CommunicationException, NotFoundException {
                return 0;
            }

            @Override
            public int triggerWorkflowRestartFromFirstFailure(Batch item, int maxTries,
                                                              long waitTime)
                    throws CommunicationException, NotFoundException {
                return 0;
            }
        };
    }

    public void setItems(ArrayList<Batch> items) {
        this.items = items;
    }

    public ArrayList<Batch> getItems() {
        return items;
    }

    public Batch getItem(String itemFullID) {
        for (Batch batch : items) {
            if (batch.getFullID().equals(itemFullID)){
                return batch;
            }
        }
        return null;
    }
}
