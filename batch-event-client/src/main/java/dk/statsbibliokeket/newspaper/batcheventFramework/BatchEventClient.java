package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClient;

/**
 * The interface for the batch event client. It is just a combination of the DomsEventClient and the SBOIInterface
 */
public interface BatchEventClient extends DomsEventClient,SBOIInterface{

}
