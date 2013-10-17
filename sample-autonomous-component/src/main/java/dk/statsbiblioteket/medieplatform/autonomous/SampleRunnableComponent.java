package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.util.Date;
import java.util.Properties;

public class SampleRunnableComponent extends AbstractRunnableComponent {


    public SampleRunnableComponent(Properties properties) {
        super(properties);
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
        TreeIterator iterator = createIterator(batch);

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
