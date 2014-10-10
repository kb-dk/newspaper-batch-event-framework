package dk.statsbiblioteket.medieplatform.autonomous;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NewspaperSBOIEventStorage extends SBOIEventIndex<Batch> {

    public NewspaperSBOIEventStorage(String summaLocation, PremisManipulatorFactory<Batch> premisManipulatorFactory,
                                     DomsEventStorage<Batch> domsEventStorage) throws MalformedURLException {
        super(summaLocation, premisManipulatorFactory, domsEventStorage);
    }

    @Override
    protected String toQueryString(Collection<String> pastSuccessfulEvents, Collection<String> pastFailedEvents,
                                   Collection<String> futureEvents, Collection<Batch> items) {
        String base = spaced(RECORD_BASE);



        StringBuilder batchesString = new StringBuilder();

        if (items != null) {
            batchesString.append(" ( ");

            boolean first = true;
            for (Item item : items) {
                if (first) {
                    first = false;
                } else {
                    batchesString.append(" OR ");
                }
                batchesString.append(" ( ");
                Batch batch = (Batch) item;
                batchesString.append("+").append(BATCH_ID).append(":B").append(batch.getBatchID());
                if (batch.getRoundTripNumber() > 0) {
                    batchesString.append(" +").append(ROUND_TRIP_NO).append(":RT").append(batch.getRoundTripNumber());
                }
                batchesString.append(" ) ");
            }
            batchesString.append(" ) ");
        }

        List<String> events = new ArrayList<>();

        if (pastSuccessfulEvents != null) {
            for (String successfulPastEvent : pastSuccessfulEvents) {
                events.add(spaced("+" + SUCCESSEVENT + ":" + quoted(successfulPastEvent)));
            }
        }
        if (pastFailedEvents != null) {
            for (String failedPastEvent : pastFailedEvents) {
                events.add(spaced("+" + FAILEVENT + ":" + quoted(failedPastEvent)));
            }
        }
        if (futureEvents != null) {
            for (String futureEvent : futureEvents) {
                events.add(spaced("-" + SUCCESSEVENT + ":" + quoted(futureEvent)));
                events.add(spaced("-" + FAILEVENT + ":" + quoted(futureEvent)));
            }
        }

        return base + batchesString.toString() + anded(events);
    }

    private String anded(List<String> events) {
        StringBuilder result = new StringBuilder();
        for (String event : events) {
            result.append(" AND ").append(event);
        }
        return result.toString();
    }
}
