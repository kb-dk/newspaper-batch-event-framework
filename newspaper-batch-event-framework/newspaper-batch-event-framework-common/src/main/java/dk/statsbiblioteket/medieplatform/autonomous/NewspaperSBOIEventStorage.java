package dk.statsbiblioteket.medieplatform.autonomous;

import java.net.MalformedURLException;
import java.util.Collection;

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
                batchesString.append(BATCH_ID).append(":B").append(batch.getBatchID());
                if (batch.getRoundTripNumber() > 0) {
                    batchesString.append(" ");
                    batchesString.append(ROUND_TRIP_NO).append(":RT").append(batch.getRoundTripNumber());
                }
                batchesString.append(" ) ");
            }
            batchesString.append(" ) ");
        }

        StringBuilder events = new StringBuilder();
        if (pastSuccessfulEvents != null) {
            for (String successfulPastEvent : pastSuccessfulEvents) {
                events.append(spaced("+" + SUCCESSEVENT + ":" + quoted(successfulPastEvent)));
            }
        }
        if (pastFailedEvents != null) {
            for (String failedPastEvent : pastFailedEvents) {
                events.append(spaced("+" + FAILEVENT + ":" + quoted(failedPastEvent)));
            }
        }
        if (futureEvents != null) {
            for (String futureEvent : futureEvents) {
                events.append(spaced("-" + SUCCESSEVENT + ":" + quoted(futureEvent)));
                events.append(spaced("-" + FAILEVENT + ":" + quoted(futureEvent)));
            }
        }
        return base + batchesString.toString() + events.toString();
    }
}
