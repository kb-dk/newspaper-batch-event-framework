package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.util.Strings;
import org.slf4j.Logger;

import java.util.Date;

/**
 * The purpose of this class is the decorate a runnable component with additional behaivour.
 * Unless the component specifically requests it (by settting presevable=false on the result collector) the
 * result will be written back to DOMS
 */
public class BatchWorker implements Runnable {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchWorker.class);


    RunnableComponent component;
    private ResultCollector resultCollector;
    private Batch batch;
    private DomsEventClient batchEventClient;
    private boolean pause = false;
    private boolean stop = false;

    public BatchWorker(RunnableComponent component, ResultCollector resultCollector, Batch batch,
                       DomsEventClient batchEventClient) {
        this.component = component;
        this.resultCollector = resultCollector;
        this.batch = batch;
        this.batchEventClient = batchEventClient;
    }

    private String getComponentFormattedName() {
        return component.getComponentName() + "-" + component.getComponentVersion();
    }

    @Override
    public void run() {

        try {
            //do work
            resultCollector.setTimestamp(new Date());
            component.doWorkOnBatch(batch, resultCollector);
        } catch (Throwable e) {
            log.warn("Component threw exception", e);
            //the work failed
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    component.getClass().getSimpleName(),
                    "Component threw exception: " + e.toString(),
                    Strings.getStackTrace(e));
        }
        if (resultCollector.isPreservable()) {
            preserveResult(batch, resultCollector);
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
     * @param batch  the batch worked on
     * @param result the result of the work
     */
    private void preserveResult(Batch batch, ResultCollector result) {
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
            batchEventClient.addEventToBatch(
                    batch.getBatchID(),
                    batch.getRoundTripNumber(),
                    getComponentFormattedName(),
                    result.getTimestamp(),
                    result.toReport(),
                    component.getEventID(),
                    result.isSuccess());
        } catch (Throwable e) {
            log.error("Caught exception while attempting to preserve result for batch", e);
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    component.getClass().getSimpleName(),
                    "Autonomous component system threw exception: " + e.toString(),
                    Strings.getStackTrace(e));
        }
    }

    public Batch getBatch() {
        return batch;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
