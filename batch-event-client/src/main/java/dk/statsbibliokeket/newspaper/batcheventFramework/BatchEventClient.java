package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.autonomous.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.SBOIInterface;

/**
 * The interface for the batch event client. It is just a combination of the DomsEventClient and the SBOIInterface
 */
public interface BatchEventClient extends DomsEventClient,SBOIInterface{

}
