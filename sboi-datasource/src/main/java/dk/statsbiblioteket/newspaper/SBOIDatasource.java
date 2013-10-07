package dk.statsbiblioteket.newspaper;

import dk.statsbibliokeket.newspaper.batcheventFramework.SBOIClientImpl;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotWorkingProperlyException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.SBOIInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SBOIDatasource implements DataSource {

    SBOIDatasourceConfiguration configuration;

    private SBOIInterface client = null;

    public SBOIDatasource(SBOIDatasourceConfiguration configuration) {
        this.configuration = configuration;
    }

    private synchronized SBOIInterface getClient(){
        if (client == null){
            client = new SBOIClientImpl(configuration.getSummaLocation());
        }
        return client;
    }

    @Override
    public List<Batch> getBatches(boolean includeDetails, Map<String, String> filters) throws NotWorkingProperlyException {
        try {
            Iterator<Batch> batches = getClient().getBatches(Arrays.asList(EventID.Data_Received.name()), new ArrayList<String>(), new ArrayList<String>());
            List<Batch> results = new ArrayList<>();
            while (batches.hasNext()) {
                Batch next = batches.next();
                Batch better = getClient().getBatch(next.getBatchID(), next.getRoundTripNumber());
                results.add(better);
            }
            return stripDetails(results.iterator(),includeDetails);
        } catch (CommunicationException | NotFoundException e) {
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
    public Batch getBatch(Long batchID, Integer roundTripNumber, boolean includeDetails) throws NotFoundException, NotWorkingProperlyException {
        try {
            return stripDetails(getClient().getBatch(batchID,roundTripNumber), includeDetails);
        } catch (CommunicationException e) {
            throw new NotWorkingProperlyException(e);
        }

    }

    @Override
    public Event getBatchEvent(Long batchID, Integer roundTripNumber, EventID eventID, boolean includeDetails) throws NotFoundException, NotWorkingProperlyException {
        Batch batch = getBatch(batchID,roundTripNumber, includeDetails);
        for (Event event : batch.getEventList()) {
            if (event.getEventID().equals(eventID)){
                return event;
            }
        }
        throw new NotFoundException("Event not found");
    }
}
