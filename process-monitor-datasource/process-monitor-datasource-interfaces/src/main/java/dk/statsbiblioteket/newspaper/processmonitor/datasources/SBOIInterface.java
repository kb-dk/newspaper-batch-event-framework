package dk.statsbiblioteket.newspaper.processmonitor.datasources;

import java.util.Iterator;
import java.util.List;

public interface SBOIInterface {


    public Iterator<Batch> getBatches(List<String> pastEvents,
                                      List<String> pastEventsExclude,
                                      List<String> futureEvents) throws CommunicationException;

/*    public void registerEvent(Long batchID, Integer runNr, EventID eventID, String agent, Date timestamp, String details, boolean success);


    public Batch getBatch(Long batchID, Integer runNr);*/
}
