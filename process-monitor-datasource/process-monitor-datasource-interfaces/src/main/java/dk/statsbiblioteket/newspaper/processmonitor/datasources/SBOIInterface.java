package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Iterator;
import java.util.List;

public interface SBOIInterface {


    public Iterator<Batch> getBatches(List<String> pastEvents,
                                      List<String> pastEventsExclude,
                                      List<String> futureEvents) throws CommunicationException;

    public Batch getBatch(Long batchID) throws CommunicationException, NotFoundException;
}
