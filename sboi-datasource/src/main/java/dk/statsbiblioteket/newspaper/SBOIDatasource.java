package dk.statsbiblioteket.newspaper;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotWorkingProperlyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SBOIDatasource implements DataSource {

    SBOIDatasourceConfiguration configuration;

    private BatchEventClient client = null;

    public SBOIDatasource(SBOIDatasourceConfiguration configuration) {
        this.configuration = configuration;
    }

    private synchronized BatchEventClient getClient(){
        if (client == null){
            client = new BatchEventClientImpl(configuration.getSummaLocation(),
                    configuration.getDomsUrl(),
                    configuration.getDomsUser(),
                    configuration.getDomsPass(),
                    configuration.getUrlToPidGen());
        }
        return client;
    }

    @Override
    public List<Batch> getBatches(boolean includeDetails, Map<String, String> filters) throws NotWorkingProperlyException {
        try {
            Iterator<Batch> batches = getClient().getBatches(Arrays.asList(EventID.Data_Received.name()), new ArrayList<String>(), new ArrayList<String>());
            return stripDetails(batches,includeDetails);
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException("Failed to communicate with SBOI",e);
        }

    }

    private List<Batch> stripDetails(Iterator<Batch> batches, boolean includeDetails) {
        ArrayList<Batch> result = new ArrayList<>();
        while (batches.hasNext()) {
            Batch next = batches.next();
            result.add(stripDetails(next,includeDetails));
        }
        return result;
    }

    private Batch stripDetails(Batch batch, boolean includeDetails) {
        if (!includeDetails){
            List<Event> events = batch.getEventList();
            for (Event event : events) {
                event.setDetails(null);
            }
        }
        return batch;
    }

    @Override
    public Batch getBatch(Long batchID, boolean includeDetails) throws NotFoundException, NotWorkingProperlyException {
        try {
            return stripDetails(getClient().getBatch(batchID), includeDetails);
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException(e);
        }

    }

    @Override
    public Event getBatchEvent(Long batchID, EventID eventID, boolean includeDetails) throws NotFoundException, NotWorkingProperlyException {
        Batch batch = getBatch(batchID, includeDetails);
        for (Event event : batch.getEventList()) {
            if (event.getEventID().equals(eventID)){
                return event;
            }
        }
        throw new NotFoundException("Event not found");
    }
}
