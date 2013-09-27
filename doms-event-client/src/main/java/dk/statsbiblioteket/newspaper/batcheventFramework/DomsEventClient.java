package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.util.Date;

public interface DomsEventClient {

    void addEventToBatch(Long batchId, int roundTripNumber,
                         String agent,
                         Date timestamp,
                         String details,
                         EventID eventType,
                         boolean outcome) throws CommunicationException;

    String createBatchRoundTrip(Long batchId, int roundTripNumber) throws CommunicationException;


    Batch getBatch(Long batchId, int roundTripNumber) throws CommunicationException;
}
