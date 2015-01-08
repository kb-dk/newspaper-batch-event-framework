package dk.statsbiblioteket.medieplatform.autonomous;

import org.slf4j.Logger;

import dk.statsbiblioteket.util.Strings;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * The purpose of this class is the decorate a runnable component with additional behaivour.
 * Unless the component specifically requests it (by settting presevable=false on the result collector) the
 * result will be written back to DOMS
 */
public class AutonomousWorker<T extends Item> implements Runnable {

    private static Logger log = LoggerFactory.getLogger(AutonomousWorker.class);


    private RunnableComponent<T> component;
    private ResultCollector resultCollector;
    private T item;
    private EventStorer<T> eventStorer;
    private boolean pause = false;
    private boolean stop = false;

    public AutonomousWorker(RunnableComponent<T> component, ResultCollector resultCollector, T item,
                            EventStorer<T> eventStorer) {
        this.component = component;
        this.resultCollector = resultCollector;
        this.item = item;
        this.eventStorer = eventStorer;
    }

    private String getComponentFormattedName() {
        return component.getComponentName() + "-" + component.getComponentVersion();
    }

    @Override
    public void run() {

        try {
            try {
                try {
                    //do work
                    resultCollector.setTimestamp(new Date());
                    component.doWorkOnItem(item, resultCollector);
                } catch (Throwable e) {
                    log.warn("Component threw exception", e);
                    //the work failed
                    resultCollector.addFailure(item.getFullID(),
                                                      "exception",
                                                      component.getClass().getSimpleName(),
                                                      "Component threw exception: " + e.toString(),
                                                      Strings.getStackTrace(e));
                }
            } finally {
                resultCollector.setDuration(new Date().getTime() - resultCollector.getTimestamp().getTime());
                if (resultCollector.isPreservable()) {
                    try {
                        preserveResult(item, resultCollector);
                    } catch (Throwable t) {
                        resultCollector.addFailure(item.getFullID(),
                                                          "exception",
                                                          component.getClass().getSimpleName(),
                                                          "Caught exception '" + t.toString() + "'while attempting to preserve result for item " + item.getFullID(),
                                                          Strings.getStackTrace(t));
                    }
                } else {
                    log.info("The result collector is not marked as preservable, so it is not preserved in DOMS, but embedded here instead: {}",
                                    resultCollector.toReport());
                }
            }
        } finally {
            if (!resultCollector.isSuccess()) {
                log.error("Failed for item {}. The report was {}", item.getFullID(), resultCollector.toReport());
            }
        }
    }

    public ResultCollector getResultCollector() {
        return resultCollector;
    }

    /**
     * This method stores the event back into DOMS, so it should be visible to the SBOI soonish
     *
     * @param item  the item worked on
     * @param result the result of the work
     */
    private void preserveResult(T item, ResultCollector result) throws CommunicationException {

        while (pause && !stop) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                //just keep sleeping
            }
        }
        if (stop) {
            log.warn("The worker is stopped, so the result will not be preserved. The result was '{}'",
                            result.toReport());
            return;
        }
        eventStorer.addEventToItem(item,
                                          getComponentFormattedName(),
                                          result.getTimestamp(),
                                          result.toReport(),
                                          component.getEventID(),
                                          result.isSuccess());
    }

    public T getItem() {
        return item;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
