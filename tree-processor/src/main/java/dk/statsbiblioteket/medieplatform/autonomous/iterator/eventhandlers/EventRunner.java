package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.io.IOException;
import java.util.List;

/**
 * For an iterator, triggers all event handlers on a given event.
 *
 * @author jrg
 */
public class EventRunner {
    private TreeIterator iterator;

    /**
     * Initialise the EventRunner with a tree iterator.
     *
     * @param iterator The tree iterator to run events on.
     */
    public EventRunner(TreeIterator iterator) {
        this.iterator = iterator;
    }

    /**
     * Trigger all the given event handlers on all events of the iterator.
     * @param eventHandlers List of event handlers to trigger.
     * @throws IOException
     */
    public void runEvents(List<TreeEventHandler> eventHandlers)
            throws IOException {
        while (iterator.hasNext()) {
            ParsingEvent current = iterator.next();

            switch (current.getType()){
                case NodeBegin: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleNodeBegin((NodeBeginsParsingEvent)current);
                    }
                    break;
                }
                case NodeEnd: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleNodeEnd((NodeEndParsingEvent) current);
                    }
                    break;
                }
                case Attribute: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleAttribute((AttributeParsingEvent) current);
                    }
                    break;
                }
            }
        }

        for (TreeEventHandler handler : eventHandlers) {
            handler.handleFinish();
        }
    }
}
