package dk.statsbiblioteket.medieplatform.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** This is a sample component to serve as a guide to developers */
public class SampleComponent {


    private static Logger log = LoggerFactory.getLogger(SampleComponent.class);

    /**
     * The class must have a main method, so it can be started as a command line tool
     *
     * @param args the arguments.
     *
     * @throws Exception
     * @see #parseArgs(String[])
     */
    public static void main(String[] args)
            throws
            Exception {
        log.info("Starting with args {}", args);

        //Parse the args to a properties construct
        Properties properties = parseArgs(args);

        //make a new runnable component from the properties
        RunnableComponent component = new SampleRunnableComponent(properties);

        //Make a client for the lock framework, and start it
        CuratorFramework lockClient = CuratorFrameworkFactory
                .newClient(properties.getProperty("lockserver"), new ExponentialBackoffRetry(1000, 3));
        lockClient.start();

        //Make a batch event client to query and store events
        BatchEventClient eventClient = new BatchEventClientImpl(properties.getProperty("summa"),
                                                                properties.getProperty("domsUrl"),
                                                                properties.getProperty("domsUser"),
                                                                properties.getProperty("domsPass"),
                                                                properties.getProperty("pidGenerator"));


        //This is the number of batches that will be worked on in parallel per invocation
        int simultaneousProcesses = 1;
        //This is the timeout when attempting to lock SBOI
        long timeoutWaitingToLockSBOI = 5000l;
        //This is the timeout when attempting to lock a batch before working on it
        long timeoutWaitingToLockBatch = 2000l;
        //After this time, the worker thread will be terminated, even if not complete
        long maxRunTimeForWorker = 60 * 60 * 1000l;

        //Use all the above to make the autonomous component
        AutonomousComponent autonoumous = new AutonomousComponent(component,
                                                                  lockClient,
                                                                  eventClient,
                                                                  simultaneousProcesses,
                                                                  toEvents(properties
                                                                                   .getProperty
                                                                                           ("pastSuccessfulEvents")),
                                                                  toEvents(properties.getProperty("pastFailedEvents")),
                                                                  toEvents(properties.getProperty("futureEvents")),
                                                                  timeoutWaitingToLockSBOI,
                                                                  timeoutWaitingToLockBatch,
                                                                  maxRunTimeForWorker);
        //Start the component
        //This call will return when the work is done
        Map<String, Boolean> result = autonoumous.call();

        //Print the result. This is not nessessary, it is to help the user see whats going on
        for (Map.Entry<String, Boolean> stringBooleanEntry : result.entrySet()) {
            if (stringBooleanEntry.getValue()) {
                System.out.println("Worked on " + stringBooleanEntry.getKey() + " successfully");
            } else {
                System.out.println("Failed to process " + stringBooleanEntry.getKey());
            }

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
            result.add(eventSplit.trim());
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
     * @throws IOException if the properties file could not be read
     */
    private static Properties parseArgs(String[] args)
            throws
            IOException {
        Properties properties = new Properties(System.getProperties());
        for (int i = 0;
             i < args.length;
             i++) {
            String arg = args[i];
            if (arg.equals("-c")) {
                String configFile = args[i + 1];
                properties.load(new FileInputStream(configFile));
            }
        }
        return properties;
    }


}
