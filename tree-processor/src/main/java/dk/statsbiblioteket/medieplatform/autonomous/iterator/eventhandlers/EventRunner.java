package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

        List<InjectingTreeEventHandler> injectingTreeEventHandlers = getInjectingTreeEventHandlers(eventHandlers);

        while (iterator.hasNext()) {
            ParsingEvent current;

            current = getInjectedParsingEvent(injectingTreeEventHandlers);
            if (current == null){
                current = iterator.next();
            }

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

    /**
     * Iterate through the given injectingTreeEventHandlers. If any of them have an injected event, pop it and return.
     * @param injectingTreeEventHandlers the injecting event handlers
     * @return an event or null
     */
    private ParsingEvent getInjectedParsingEvent(List<InjectingTreeEventHandler> injectingTreeEventHandlers) {
        ParsingEvent current;
        for (InjectingTreeEventHandler injectingTreeEventHandler : injectingTreeEventHandlers) {
            try {
                current = injectingTreeEventHandler.popInjectedEvent();
                if (current != null){
                    return current;
                }
            } catch (NoSuchElementException e){
                continue;
            }
        }
        return null;
    }

    /**
     * Filter out the injecting event handlers from the list of event handlers
     * @param eventHandlers all the event handlers
     * @return the injecting event handlers
     */
    private List<InjectingTreeEventHandler> getInjectingTreeEventHandlers(List<TreeEventHandler> eventHandlers) {
        List<InjectingTreeEventHandler> injectingTreeEventHandlers = new ArrayList<>(); for (TreeEventHandler eventHandler : eventHandlers) {
            if (eventHandler instanceof InjectingTreeEventHandler) {
                InjectingTreeEventHandler injectingTreeEventHandler = (InjectingTreeEventHandler) eventHandler;
                injectingTreeEventHandlers.add(injectingTreeEventHandler);
            }
        }
        return injectingTreeEventHandlers;
    }
}
