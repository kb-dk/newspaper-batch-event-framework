package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.util.Date;

public interface DomsEventClient<T> {

    void addEventToBatch(T batchId, int runNr,
                         String agent,
                         Date timestamp,
                         String details,
                         EventID eventType,
                         boolean outcome,
                         String outcomeDetails) throws CommunicationException;

    String createBatchRun(T batchId, int runNr) throws CommunicationException;

}
