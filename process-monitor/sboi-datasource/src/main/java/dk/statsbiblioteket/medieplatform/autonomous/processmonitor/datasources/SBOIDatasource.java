package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.BatchItemFactory;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.EventAccessor;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperDomsEventStorageFactory;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperSBOIEventStorage;
import dk.statsbiblioteket.medieplatform.autonomous.NotFoundException;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Datasource implemented to SBOI */
public class SBOIDatasource implements DataSource {

    private SBOIDatasourceConfiguration configuration;
    private EventAccessor<Batch> eventAccessor = null;
    private BatchItemFactory itemFactory;

    public SBOIDatasource(SBOIDatasourceConfiguration configuration) {
        this.configuration = configuration;
        itemFactory = new BatchItemFactory();
    }

    private synchronized EventAccessor<Batch> getEventExplorer() {
        try {
            if (eventAccessor == null) {
                eventAccessor = new NewspaperSBOIEventStorage(
                        configuration.getSummaLocation(),
                        new PremisManipulatorFactory<>(PremisManipulatorFactory.TYPE,itemFactory),
                        getDomsEventStorage(),configuration.getSboiPageSize());
            }
            return eventAccessor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NewspaperDomsEventStorage getDomsEventStorage() {
        NewspaperDomsEventStorageFactory factory = new NewspaperDomsEventStorageFactory();

        factory.setFedoraLocation(configuration.getDomsLocation());
        factory.setUsername(configuration.getDomsUser());
        factory.setPassword(configuration.getDomsPassword());
        factory.setItemFactory(itemFactory);
        NewspaperDomsEventStorage domsEventStorage;
        try {
            domsEventStorage = factory.createDomsEventStorage();
        } catch (JAXBException | MalformedURLException | PIDGeneratorException e) {
            throw new RuntimeException(e);
        }

        return domsEventStorage;

    }

    @Override
    public List<Batch> getBatches(boolean includeDetails, Map<String, String> filters) throws
                                                                                       NotWorkingProperlyException {
        try {
            Iterator<Batch> batches = getEventExplorer().findItems(includeDetails,
                                                                                   Arrays.asList("Data_Received"), new ArrayList<String>());
            return iteratorToBatchList(batches);
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException("Failed to communicate with SBOI", e);
        }

    }

    private List<Batch> iteratorToBatchList(Iterator<Batch> batches) {
        ArrayList<Batch> result = new ArrayList<>();
        while (batches.hasNext()) {
            Batch item = batches.next();
            result.add(item);
        }
        return result;
    }

    /**
     * Strip details if required from the batched
     *
     * @param batches        the batches to strip
     * @param includeDetails true if details should be stripped
     *
     * @return the batches as a list
     */
    private List<Batch> stripDetails(Iterator<Batch> batches, boolean includeDetails) {
        ArrayList<Batch> result = new ArrayList<>();
        while (batches.hasNext()) {
            Batch batch = batches.next();
            result.add(stripDetails(batch, includeDetails));
        }
        return result;
    }

    /**
     * Strip details on a single batch
     *
     * @param batch          the batch
     * @param includeDetails true if details should be stripped
     *
     * @return the batch
     */
    private Batch stripDetails(Batch batch, boolean includeDetails) {
        if (!includeDetails) {
            List<Event> events = batch.getEventList();
            for (Event event : events) {
                event.setDetails(null);
            }
        }
        return batch;
    }

    @Override
    public Batch getBatch(String batchID, Integer roundTripNumber, boolean includeDetails) throws
                                                                                           NotFoundException,
                                                                                           NotWorkingProperlyException {
        try {
            final NewspaperDomsEventStorage domsEventStorage = getDomsEventStorage();
            if (roundTripNumber == null){
                List<Batch> roundTrips = domsEventStorage.getAllRoundTrips(batchID);
                return roundTrips.get(roundTrips.size()-1);
            }
            return domsEventStorage.getItemFromFullID(Batch.formatFullID(batchID, roundTripNumber));
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException(e);
        }

    }

    @Override
    public Event getBatchEvent(String batchID, Integer roundTripNumber, String eventID, boolean includeDetails) throws
                                                                                                                NotFoundException,
                                                                                                                NotWorkingProperlyException {
        Batch batch = getBatch(batchID, roundTripNumber, includeDetails);
        for (Event event : batch.getEventList()) {
            if (event.getEventID().equals(eventID)) {
                return event;
            }
        }
        throw new NotFoundException("Event not found");
    }
}
