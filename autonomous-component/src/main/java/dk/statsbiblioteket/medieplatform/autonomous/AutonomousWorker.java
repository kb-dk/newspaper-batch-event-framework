package dk.statsbiblioteket.medieplatform.autonomous;

import org.slf4j.Logger;

import dk.statsbiblioteket.util.Strings;

import java.util.Date;

/**
 * The purpose of this class is the decorate a runnable component with additional behaivour.
 * Unless the component specifically requests it (by settting presevable=false on the result collector) the
 * result will be written back to DOMS
 */
public class AutonomousWorker implements Runnable {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(AutonomousWorker.class);


    RunnableComponent component;
    private ResultCollector resultCollector;
    private Item item;
    private EventStorer eventStorer;
    private boolean pause = false;
    private boolean stop = false;

    public AutonomousWorker(RunnableComponent component, ResultCollector resultCollector, Item item,
                            EventStorer eventStorer) {
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
        final Date before = new Date();
        try {
            //do work
            resultCollector.setTimestamp(before);
            component.doWorkOnItem(item, resultCollector);
        } catch (Throwable e) {
            log.warn("Component threw exception", e);
            //the work failed
            resultCollector.addFailure(
                    item.getFullID(),
                    "exception",
                    component.getClass().getSimpleName(),
                    "Component threw exception: " + e.toString(),
                    Strings.getStackTrace(e));
        }
        final Date after = new Date();
        resultCollector.setDuration(after.getTime() - before.getTime());

        if (resultCollector.isPreservable()) {
            preserveResult(item, resultCollector);
        } else {
            log.info("The result collector is not marked as preservable, so it is not preserved in DOMS");
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
    private void preserveResult(Item item, ResultCollector result) {
        try {
            while (pause && !stop) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //just keep sleeping
                }
            }
            if (stop) {
                log.warn("The worker is stopped, so the result will not be preserved");
                return;
            }
            eventStorer.addEventToItem(item,
                    getComponentFormattedName(),
                    result.getTimestamp(),
                    result.toReport(),
                    component.getEventID(),
                    result.isSuccess());
        } catch (Throwable e) {
            log.error("Caught exception while attempting to preserve result for item", e);
            resultCollector.addFailure(
                    item.getFullID(),
                    "exception",
                    component.getClass().getSimpleName(),
                    "Autonomous component system threw exception: " + e.toString(),
                    Strings.getStackTrace(e));
        }
    }

    public Item getItem() {
        return item;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
