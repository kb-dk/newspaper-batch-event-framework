package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Iterator;
import java.util.List;

public interface SBOIInterface {


    public Iterator<Batch> getBatches(List<EventID> pastSuccessfulEvents,
                                      List<EventID> pastFailedEvents,
                                      List<EventID> futureEvents) throws CommunicationException;

    public Batch getBatch(Long batchID, Integer roundTripNumber) throws CommunicationException, NotFoundException;
}
