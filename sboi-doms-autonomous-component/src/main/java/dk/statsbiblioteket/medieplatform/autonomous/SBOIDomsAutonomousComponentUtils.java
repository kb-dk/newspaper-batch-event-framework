package dk.statsbiblioteket.medieplatform.autonomous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SBOIDomsAutonomousComponentUtils extends AutonomousComponentUtils {
    private static Logger log = LoggerFactory.getLogger(SBOIDomsAutonomousComponentUtils.class);


    /**
     * Create an autonomous component from a runnable component and start it. Stuff is configured from the included
     * properties
     *
     * @param properties the properties to use
     * @param component  the runnable component to invoke
     *
     * @return the result of the invocation. A map from batch Full IDs to results. If the execution failed, a message
     * will be printed to the log and the result map will be empty
     *
     * lockserver: string: url to the zookeeper server
     * summa: string, url to the summa webservice
     * domsUrl: string, url to the fedora doms instance
     * domsUser: string; username when writing events to the doms batch objects
     * domsPass: string: password when writing events to the doms batch objects
     * pidGenerator: String: url to the pidgenerator service
     * maxThreads: Integer: The number of batches to work on concurrently. Default 1
     * maxRuntimeForWorkers: Long: The number of milliseconds to wait before forcebly killing worker threads.
     * Default one hour
     * pastSuccessfulEvents: String list, comma separated: The list of event IDs that the batch must have
     * experienced successfully in order to be eligble to be worked on by this component
     * pastFailedEvents: String list, comma separated: The list of event IDs that the batch must have
     * experienced without success in order to be eligble to be worked on by this component
     * futureEvents: String list, comma separated: The list of event IDs that the batch must NOT have
     * experienced in order to be eligble to be worked on by this component
     * @see dk.statsbiblioteket.medieplatform.autonomous.AutonomousComponentUtils#startAutonomousComponent(java.util.Properties, RunnableComponent, EventTrigger, EventStorer)
     */
    public static CallResult startAutonomousComponent(Properties properties, RunnableComponent component) {
        return startAutonomousComponent(properties, component, getEventTrigger(properties), getEventStorer(properties));
    }

    private static synchronized SBOIEventIndex getEventTrigger(Properties properties) {
        try {
            return new SBOIEventIndex(
                    properties.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL), new PremisManipulatorFactory(
                    new NewspaperIDFormatter(), PremisManipulatorFactory.TYPE), getEventStorer(properties)
            );
        } catch (Exception e) {
            log.error("Unable to initialize event trigger", e);
            throw new InitialisationException("Unable to initialize event trigger", e);
        }
    }

    private static synchronized DomsEventStorage getEventStorer(Properties properties) {
        DomsEventStorageFactory domsEventStorageFactory = new DomsEventStorageFactory();
        domsEventStorageFactory.setFedoraLocation(properties.getProperty(ConfigConstants.DOMS_URL));
        domsEventStorageFactory.setPidGeneratorLocation(properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
        domsEventStorageFactory.setUsername(properties.getProperty(ConfigConstants.DOMS_USERNAME));
        domsEventStorageFactory.setPassword(properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        try {
            return domsEventStorageFactory.createDomsEventStorage();
        } catch (Exception e) {
            log.error("Unable to initialize event storage", e);
            throw new InitialisationException("Unable to initialize event storage", e);
        }
    }

}
