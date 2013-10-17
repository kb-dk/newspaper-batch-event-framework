package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.autonomous.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.Batch;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.util.Strings;

import java.util.Date;

/**
 * The purpose of this class is the decorate a runnable component with additional behaivour.
 * It ensures that the result of the execution is written back to DOMS as an event.
 *
 */
public class BatchWorker
        implements Runnable {

    RunnableComponent component;
    private ResultCollector resultCollector;
    private Batch batch;
    private DomsEventClient batchEventClient;

    private boolean pause = false;
    private boolean stop = false;

    public BatchWorker(RunnableComponent component,
                       ResultCollector resultCollector,
                       Batch batch,
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
        } catch (Exception e) {
            //the work failed
            resultCollector.addFailure(batch.getFullID(),
                                       "Component Failure",
                                       getComponentFormattedName(),
                                       "Component threw exception",
                                       e.toString());
        }
        preserveResult(batch, resultCollector);
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
    private void preserveResult(Batch batch,
                                ResultCollector result) {
        try {
            while (pause && !stop){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //just keep sleeping
                }
            }
            if (stop){
                return;
            }
            batchEventClient.addEventToBatch(batch.getBatchID(),
                                             batch.getRoundTripNumber(),
                                             getComponentFormattedName(),
                                             result.getTimestamp(),
                                             result.toReport(),
                                             component.getEventID(),
                                             result.isSuccess());
        } catch (CommunicationException e) {
            resultCollector.addFailure("Autonomous Component System",
                                       e.getClass().getName(),
                                       component.getComponentName(),
                                       e.getMessage(),
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
