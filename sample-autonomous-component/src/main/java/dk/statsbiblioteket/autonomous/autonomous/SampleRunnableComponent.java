package dk.statsbiblioteket.autonomous.autonomous;

import dk.statsbiblioteket.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.Batch;

import java.util.Date;
import java.util.Properties;

public class SampleRunnableComponent extends AbstractRunnableComponent {

    private Properties properties;

    public SampleRunnableComponent(Properties properties) {
        //To change body of created methods use File | Settings | File Templates.
        this.properties = properties;
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
    public String getEventID() {
        return "Data_Archived";
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
        resultCollector.setTimestamp(new Date());
    }
}
