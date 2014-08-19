package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.util.Strings;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventRunner implements Runnable {
    public static final String EXCEPTION = "exception";
    public static final String UNEXPECTED_ERROR = "Unexpected error: ";
    protected TreeIterator iterator;
    protected final List<TreeEventHandler> eventHandlers;
    protected final ResultCollector resultCollector;

    private boolean spawn = false;


    private Queue<ParsingEvent> pushedEvents = new ConcurrentLinkedQueue<>();


    /**
     * Initialise the EventRunner with a tree iterator.
     *
     * @param iterator The tree iterator to run events on.
     */
    public EventRunner(TreeIterator iterator, List<TreeEventHandler> eventHandlers, ResultCollector resultCollector) {
        this.iterator = iterator;
        this.eventHandlers = eventHandlers;
        this.resultCollector = resultCollector;
    }

    /**
     * Initialise the EventRunner with a tree iterator.
     *
     * @param iterator The tree iterator to run events on.
     */
    protected EventRunner(TreeIterator iterator, List<TreeEventHandler> eventHandlers, ResultCollector resultCollector, boolean spawn) {
        this(iterator,eventHandlers,resultCollector);
        this.spawn = spawn;
    }


    /**
     * Trigger all the given event handlers on all events of the iterator.
    *
     * @throws java.io.IOException
     */
    public void run()  {
        ParsingEvent current = null;
        while (!pushedEvents.isEmpty() || iterator.hasNext()) {
            current = popInjectedEvent();
            if (current == null) {
                current = iterator.next();
            }
            switch (current.getType()) {
                case NodeBegin: {
                    handleNodeBegins(current);
                    break;
                }
                case NodeEnd: {
                    handleNodeEnd(current);
                    break;
                }
                case Attribute: {
                    handleAttribute(current);
                    break;
                }
            }
        }
        if (!spawn) {
            handleFinish();
        }
    }

    public ParsingEvent popInjectedEvent() {
        ParsingEvent current;
        current = pushedEvents.poll();
        return current;
    }

    public void handleFinish() {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleFinish(this);
            } catch (Exception e) {
                resultCollector.addFailure("General Batch failure",
                        EXCEPTION,
                        handler.getClass().getSimpleName(),
                        UNEXPECTED_ERROR + e.toString(),
                        Strings.getStackTrace(e));
            }
        }
    }

    public void handleAttribute(ParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleAttribute((AttributeParsingEvent) current,this);
            } catch (Exception e) {
                resultCollector.addFailure(current.getName(),
                        EXCEPTION,
                        handler.getClass().getSimpleName(),
                        UNEXPECTED_ERROR + e.toString(),
                        Strings.getStackTrace(e));
            }
        }
    }

    public void handleNodeEnd(ParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleNodeEnd((NodeEndParsingEvent) current, this);
            } catch (Exception e) {
                resultCollector.addFailure(current.getName(),
                        EXCEPTION,
                        handler.getClass().getSimpleName(),
                        UNEXPECTED_ERROR + e.toString(),
                        Strings.getStackTrace(e));
            }
        }
    }

    public void handleNodeBegins(ParsingEvent current) {
        for (TreeEventHandler handler : eventHandlers) {
            try {
                handler.handleNodeBegin((NodeBeginsParsingEvent) current,this);
            } catch (Exception e) {
                resultCollector.addFailure(current.getName(),
                        EXCEPTION,
                        handler.getClass().getSimpleName(),
                        UNEXPECTED_ERROR + e.toString(),
                        Strings.getStackTrace(e));
            }
        }
    }

    public void pushEvent(ParsingEvent parsingEvent) {
        pushedEvents.add(parsingEvent);
    }
}
