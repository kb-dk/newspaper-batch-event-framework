package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.batcheventFramework.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.util.Date;
import java.util.List;

public interface BatchEventClient {


    List<Batch> query(List<EventID> successfulPastEvents, List<EventID> failedPastEvents, List<EventID> futureEvents) throws CommunicationException;

    void addEvent(Batch batch, String premisAgent, Date timestamp, String details, EventID eventID, boolean outcome) throws CommunicationException;
}
