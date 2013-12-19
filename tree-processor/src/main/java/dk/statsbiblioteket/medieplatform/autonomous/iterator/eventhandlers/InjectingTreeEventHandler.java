package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;

import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Injecting Tree Event Handler. An implementation of the TreeEventHandler. It maintains a stack of injected events
 * each time the EventRunner polls for a new event, it will check this list before the tree iterator. This way,
 * events can be injected (but only as the next event, not at arbitrary locations in the tree)
 */
public abstract class InjectingTreeEventHandler implements TreeEventHandler {

    private LinkedList<ParsingEvent> parsingEventLinkedList = new LinkedList<>();

    /**
     * Get the top element from the stack
     *
     * @return a parsing event
     * @throws NoSuchElementException if the stack was empty
     */
    public ParsingEvent popInjectedEvent() throws NoSuchElementException {
        return getParsingEventLinkedList().pop();
    }

    /**
     * push an injected parsing event onto the stack
     *
     * @param parsingEvent the event to push
     */
    public void pushInjectedEvent(ParsingEvent parsingEvent) {
        getParsingEventLinkedList().push(parsingEvent);
    }

    private synchronized LinkedList<ParsingEvent> getParsingEventLinkedList() {
        if (parsingEventLinkedList == null) {
            parsingEventLinkedList = new LinkedList<>();
        }
        return parsingEventLinkedList;
    }
}
