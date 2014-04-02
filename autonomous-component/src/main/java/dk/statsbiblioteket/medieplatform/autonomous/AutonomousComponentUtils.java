package dk.statsbiblioteket.medieplatform.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AutonomousComponentUtils {

    private static Logger log = LoggerFactory.getLogger(AutonomousComponentUtils.class);

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
     *
     */

    public static CallResult startAutonomousComponent(Properties properties, RunnableComponent component,
                                                      EventTrigger eventTrigger, EventStorer eventStorer) {
        //Make a client for the lock framework, and start it
        CuratorFramework lockClient = CuratorFrameworkFactory.newClient(
                properties.getProperty(ConfigConstants.AUTONOMOUS_LOCKSERVER_URL), new ExponentialBackoffRetry(1000, 3));
        lockClient.start();

        //This is the number of batches that will be worked on in parallel per invocation
        int simultaneousProcesses = Integer.parseInt(
                properties.getProperty(
                        ConfigConstants.AUTONOMOUS_MAXTHREADS, "1")
                                                    );
        //This is the timeout when attempting to lock SBOI
        long timeoutWaitingToLockSBOI = 5000l;
        //This is the timeout when attempting to lock a batch before working on it
        long timeoutWaitingToLockBatch = 2000l;
        //After this time, the worker thread will be terminated, even if not complete
        long maxRunTimeForWorker = Long.parseLong(
                properties.getProperty(
                        ConfigConstants.AUTONOMOUS_MAX_RUNTIME, 60 * 60 * 1000l + "")
                                                 );

        String maxResultsProperty = properties.getProperty(ConfigConstants.MAX_RESULTS_COLLECTED);
        Integer maxResults = null;
        if (maxResultsProperty != null) {
            maxResults = Integer.parseInt(maxResultsProperty);
        }

        if (eventTrigger == null) {
            throw new IllegalArgumentException("eventTrigger null");
        }
        if (eventStorer == null) {
            throw new IllegalArgumentException("eventStorer null");
        }

        //Use all the above to make the autonomous component
        AutonomousComponent autonoumous = new AutonomousComponent(
                component,
                lockClient,
                simultaneousProcesses,
                toEvents(properties.getProperty(ConfigConstants.AUTONOMOUS_PAST_SUCCESSFUL_EVENTS)),
                toEvents(properties.getProperty(ConfigConstants.AUTONOMOUS_PAST_FAILED_EVENTS)),
                toEvents(properties.getProperty(ConfigConstants.AUTONOMOUS_FUTURE_EVENTS)),
                timeoutWaitingToLockSBOI,
                timeoutWaitingToLockBatch,
                maxRunTimeForWorker,
                maxResults,
                eventTrigger,
                eventStorer);


        try {//Start the component
            //This call will return when the work is done
            return autonoumous.call();
        } catch (CouldNotGetLockException e) {
            log.error("Could not get lock on SBOI", e);
            return new CallResult("Could not get lock on SBOI");
        } catch (LockingException e) {
            log.error("Failed to communicate with zookeeper", e);
            return new CallResult("Failed to communicate with zookeeper");
        } catch (CommunicationException e) {
            log.error("Commmunication exception when invoking backend services", e);
            return new CallResult("Commmunication exception when invoking backend services");
        } finally {
            lockClient.close();
        }

    }

    /**
     * Convert the events list from the properties file. It consist of a comma-separated list of event ids
     *
     * @param events the event list
     *
     * @return as a list
     */
    private static List<String> toEvents(String events) {
        String[] eventSplits = events.split(",");
        List<String> result = new ArrayList<>();
        for (String eventSplit : eventSplits) {
            if (!eventSplit.trim().isEmpty()) {
                result.add(eventSplit.trim());
            }
        }
        return result;
    }

    /**
     * Sample method to parse properties. This is probably not the best way to do this
     * It makes a new properties, with the system defaults. It then scan the args for a the string "-c". If found
     * it expects the next arg to be a path to a properties file.
     *
     * @param args the command line args
     *
     * @return as a properties
     * @throws java.io.IOException if the properties file could not be read
     */
    public static Properties parseArgs(String[] args) throws IOException {
        Properties properties = new Properties(System.getProperties());
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-c")) {
                String configFile = args[i + 1];
                properties.load(new FileInputStream(configFile));
            }
        }
        return properties;
    }
}
