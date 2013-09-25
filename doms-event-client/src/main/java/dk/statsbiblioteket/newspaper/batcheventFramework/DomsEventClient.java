package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.util.Date;

public interface DomsEventClient {

    void addEventToBatch(Long batchId, int runNr,
                         String agent,
                         Date timestamp,
                         String details,
                         EventID eventType,
                         boolean outcome,
                         String outcomeDetails) throws CommunicationException;

    String createBatchRun(Long batchId, int runNr) throws CommunicationException;

}
