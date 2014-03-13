package dk.statsbiblioteket.medieplatform.autonomous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.util.Date;
import java.util.Properties;

public class SampleRunnableComponent extends TreeProcessorAbstractRunnableComponent {

    private static Logger log = LoggerFactory.getLogger(SampleRunnableComponent.class);


    /**
     * Constructor matching super. Super requires a properties to be able to initialise the tree iterator, if needed.
     * If you do not need the tree iterator, ignore properties.
     *
     * You can use properties for your own stuff as well
     *
     * @param properties properties
     *
     * @see #getProperties()
     */
    public SampleRunnableComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getEventID() {
        //This is the event ID that correspond to the work done by this component. It will be added to the list of
        //events a batch have experienced when the work is completed (along with information about success or failure)
        return "Batch_Sampled";
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        //This is the working method of the component

        //IT REALLY MUST BE THREAD SAFE. Multiple threads can invoke this concurrently. Do not use instance variables!

        //Create a tree iterator for the batch. It will be created based on the properties that is handled in the constructor
        TreeIterator iterator = createIterator(batch);

        //The work of this component is just to count the number of files and directories
        int numberOfFiles = 0;
        int numberOfDirectories = 0;
        while (iterator.hasNext()) {
            ParsingEvent next = iterator.next();
            switch (next.getType()) {
                case NodeBegin: {
                    //This is the event when we enter a new "directory"
                    //After this event, there will come a series of AttributeEvents, corresponding to the files in the folder
                    //Then there will come a NodeBegin event if there is any subfolders, and the process repeats
                    numberOfDirectories += 1;
                    break;
                }
                case NodeEnd: {
                    //This is the event when we have handled all files and subfolders in a folder. This event is
                    //thrown just before we leave the folder
                    break;
                }
                case Attribute: {
                    //This represents a file in the tree
                    numberOfFiles += 1;

                    //Cast the event to a attributeEvent
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;

                    //Check that the checksum is readable.
                    String checksum = attributeEvent.getChecksum();
                    if (checksum == null) {
                        //If there is no checksum, report a failure.
                        resultCollector.addFailure(
                                attributeEvent.getName(),
                                "filestructure",
                                getClass().getSimpleName(),
                                "Missing checksum");
                    }
                    break;
                }
            }

        }
        if (numberOfFiles < 5) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "filestructure",
                    getClass().getSimpleName(),
                    "There are to few files in the batch");
        }
        //And finally set the timestamp of the execution.
        resultCollector.setTimestamp(new Date());
    }
}
