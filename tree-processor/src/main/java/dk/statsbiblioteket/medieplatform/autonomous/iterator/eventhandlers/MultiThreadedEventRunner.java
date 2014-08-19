package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MultiThreadedEventRunner extends EventRunner {


    private final EventCondition forker;
    private final ExecutorService executor;
    private List<Future<?>> childTasks = new ArrayList<>();


    public MultiThreadedEventRunner(TreeIterator iterator, List<TreeEventHandler> eventHandlers,
                                    ResultCollector resultCollector, EventCondition forker, ExecutorService executor) {
        super(iterator, eventHandlers, resultCollector);
        this.forker = forker;
        this.executor = executor;
    }

    @Override
    protected void handleFinish(ParsingEvent current, TreeEventHandler handler) {
        super.handleFinish(current, handler);
    }

    @Override
    protected void handleNodeBegins(ParsingEvent current) {
        if (forker.shouldFork(current)) {
            //any further will spawn sub iterators
            //Skip to next sibling will branch of the iterator that began with this node begins
            //It will then return than iterator.
            //And the iterator where this was called will skip to the next node begins that was not this tree
            TreeIterator childIterator = iterator.skipToNextSibling();
            EventRunner childRunner = new EventRunner(childIterator,eventHandlers,resultCollector,true);
            Future<?> future = executor.submit(childRunner);
            childTasks.add(future);
        } else {
            super.handleNodeBegins(current);
        }

    }

    @Override
    protected void handleNodeEnd(ParsingEvent current) {

        if (forker.shouldJoin(current)) {
            for (Future<?> childTask : childTasks) {
                try {
                    childTask.get();
                } catch (InterruptedException | ExecutionException e) {
                    resultCollector.addFailure(current.getName(), EventRunner.EXCEPTION,
                            this.getClass().getSimpleName(), EventRunner.UNEXPECTED_ERROR + e.toString(),
                            Strings.getStackTrace(e));
                }
            }
        }
        super.handleNodeEnd(current);
    }

    public interface EventCondition{
        public boolean shouldFork(ParsingEvent nodeBeginsParsingEvent);

        public boolean shouldJoin(ParsingEvent nodeEndParsingEvent);
    }

    public static EventCondition singleThreaded = new EventCondition() {
        @Override
        public boolean shouldFork(ParsingEvent nodeBeginsParsingEvent) {
            return false;
        }

        @Override
        public boolean shouldJoin(ParsingEvent nodeEndParsingEvent) {
            return false;
        }
    };

}
