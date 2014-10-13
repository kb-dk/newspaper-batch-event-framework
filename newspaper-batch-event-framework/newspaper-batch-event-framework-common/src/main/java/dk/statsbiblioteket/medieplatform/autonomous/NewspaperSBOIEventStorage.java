package dk.statsbiblioteket.medieplatform.autonomous;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class NewspaperSBOIEventStorage extends SBOIEventIndex<Batch> implements EventAccessor<Batch> {

    private final DomsEventStorage<Batch> domsEventStorage;

    public NewspaperSBOIEventStorage(String summaLocation, PremisManipulatorFactory<Batch> premisManipulatorFactory,
                                     DomsEventStorage<Batch> domsEventStorage) throws MalformedURLException {
        super(summaLocation, premisManipulatorFactory, domsEventStorage);
        this.domsEventStorage = domsEventStorage;
    }

    @Override
    public Iterator<Batch> findItems(boolean details, List<String> pastSuccessfulEvents, List<String> pastFailedEvents,
                                 List<String> futureEvents) throws CommunicationException {

        return super.search(details, pastSuccessfulEvents, pastFailedEvents, futureEvents, null);
    }

    @Override
    public Batch getItem(String itemFullID) throws CommunicationException, NotFoundException {
        return domsEventStorage.getItemFromFullID(itemFullID);
    }

    @Override
    protected String getResultRestrictions(Collection<Batch> items) {
        //TODO apparently this does not work
        String itemsString;
        StringBuilder batchesString = new StringBuilder();
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
        itemsString = batchesString.toString();
        return itemsString;
    }
}
