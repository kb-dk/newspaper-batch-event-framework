package dk.statsbiblioteket.newspaper.autonomous;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.autonomous.AutonomousComponent;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.IteratorForFileSystems;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SampleComponent
        implements RunnableComponent {


    private Properties properties;

    public SampleComponent(Properties properties) {
        //To change body of created methods use File | Settings | File Templates.
        this.properties = properties;
    }

    public static void main(String[] args)
            throws
            Exception {

        Properties properties = parseArgs(args);

        RunnableComponent component = new SampleComponent(properties);


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

    private static List<EventID> toEvents(String events) {
        String[] eventSplits = events.split(",");
        List<EventID> result = new ArrayList<>();
        for (String eventSplit : eventSplits) {
            try {
            result.add(EventID.valueOf(eventSplit.trim()));
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

    @Override
    public String getComponentName() {
        return "Sample_component";

    }

    @Override
    public String getComponentVersion() {
        return "0.1";
    }

    @Override
    public EventID getEventID() {
        return EventID.Data_Archived;
    }

    private TreeIterator createIterator(Properties properties,
                                        Batch batch) {
        boolean useFileSystem = Boolean.parseBoolean(properties.getProperty("useFileSystem", "true"));
        if (useFileSystem) {
            File scratchDir = new File(properties.getProperty("scratch"));
            File batchDir = new File(scratchDir, "B" + batch.getBatchID() + "-RT" + batch.getRoundTripNumber());
            return new IteratorForFileSystems(batchDir);

        }
        throw new UnsupportedOperationException("Presently only supported for filesystems, sorry");
    }

    @Override
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {
        TreeIterator iterator = createIterator(properties, batch);

        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        while (iterator.hasNext()) {
            ParsingEvent next = iterator.next();
            switch (next.getType()) {
                case NodeBegin: {
                    numberOfDirectories += 1;
                    break;
                }
                case NodeEnd: {
                    break;
                }
                case Attribute: {
                    numberOfFiles += 1;
                    break;
                }
            }

        }
        resultCollector.setSuccess(true);
        resultCollector.setTimestamp(new Date());
    }

}
