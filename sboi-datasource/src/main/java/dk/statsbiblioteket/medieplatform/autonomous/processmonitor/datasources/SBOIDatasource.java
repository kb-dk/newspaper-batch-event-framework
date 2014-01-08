package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;

import dk.statsbibliokeket.newspaper.batcheventFramework.SBOIClientImpl;
import dk.statsbibliokeket.newspaper.batcheventFramework.SBOIInterface;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClient;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClientFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperIDFormatter;
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
    private SBOIInterface client = null;

    public SBOIDatasource(SBOIDatasourceConfiguration configuration) {
        this.configuration = configuration;
    }

    private synchronized SBOIInterface getClient() {
        try {
            if (client == null) {
                client = new SBOIClientImpl(
                        configuration.getSummaLocation(),
                        new PremisManipulatorFactory(new NewspaperIDFormatter(), PremisManipulatorFactory.TYPE),
                        getDomsEventClient());
            }
            return client;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DomsEventClient getDomsEventClient() {
        DomsEventClientFactory factory = new DomsEventClientFactory();

        factory.setFedoraLocation(configuration.getDomsLocation());
        factory.setUsername(configuration.getDomsUser());
        factory.setPassword(configuration.getDomsPassword());
        DomsEventClient domsEventClient;
        try {
            domsEventClient = factory.createDomsEventClient();
        } catch (JAXBException | MalformedURLException | PIDGeneratorException e) {
            throw new RuntimeException(e);
        }

        return domsEventClient;

    }

    @Override
    public List<Batch> getBatches(boolean includeDetails, Map<String, String> filters) throws
                                                                                       NotWorkingProperlyException {
        try {
            Iterator<Batch> batches = getClient().getBatches(
                    includeDetails, Arrays.asList("Data_Received"), new ArrayList<String>(), new ArrayList<String>());
            return iteratorToList(batches);
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException("Failed to communicate with SBOI", e);
        }

    }

    private List<Batch> iteratorToList(Iterator<Batch> batches) {
        ArrayList<Batch> result = new ArrayList<>();
        while (batches.hasNext()) {
            Batch next = batches.next();
            result.add(next);
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
            Batch next = batches.next();
            result.add(stripDetails(next, includeDetails));
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
            return stripDetails(getClient().getBatch(batchID, roundTripNumber), includeDetails);
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
