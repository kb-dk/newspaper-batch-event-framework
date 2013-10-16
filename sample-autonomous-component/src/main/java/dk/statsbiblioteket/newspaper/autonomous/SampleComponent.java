package dk.statsbiblioteket.newspaper.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.autonomous.AutonomousComponent;
import dk.statsbiblioteket.autonomous.RunnableComponent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SampleComponent {



    public static void main(String[] args)
            throws
            Exception {

        Properties properties = parseArgs(args);

        RunnableComponent component = new SampleRunnableComponent(properties);


        CuratorFramework lockClient = CuratorFrameworkFactory.newClient(properties.getProperty("lockserver"),
                                                                        new ExponentialBackoffRetry(1000, 3));
        lockClient.start();
        BatchEventClient eventClient = createEventClient(properties);
        AutonomousComponent autonoumous = new AutonomousComponent(component, properties, lockClient, eventClient, 1,
                                                                  toEvents(properties.getProperty("pastevents")),
                                                                  toEvents(properties.getProperty("pasteventsExclude")),
                                                                  toEvents(properties.getProperty("futureEvents")));
        Map<String, Boolean> result = autonoumous.call();
        //TODO what to do with the result?
    }

    private static List<String> toEvents(String events) {
        String[] eventSplits = events.split(",");
        List<String> result = new ArrayList<>();
        for (String eventSplit : eventSplits) {
            try {
            result.add(String.valueOf(eventSplit.trim()));
            } catch (IllegalArgumentException e){
                //TODO log this
            }
        }
        return result;
    }

    private static BatchEventClient createEventClient(Properties properties) {
        return new BatchEventClientImpl(properties.getProperty("summa"), properties.getProperty("domsUrl"),
                                        properties.getProperty("domsUser"), properties.getProperty("domsPass"),
                                        properties.getProperty("pidGenerator"));
    }

    private static Properties parseArgs(String[] args)
            throws
            IOException {
        Properties properties = new Properties();
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
