package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.util.Strings;

import java.util.Date;

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

    private String getComponentFormattetName() {
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
            resultCollector.setSuccess(false);
            resultCollector.addFailure(AutonomousComponent.getBatchFormattetID(batch),
                                       "Component Failure",
                                       getComponentFormattetName(),
                                       "Component threw exception",
                                       e.getMessage());
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
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if (stop){
                return;
            }
            batchEventClient.addEventToBatch(batch.getBatchID(),
                                             batch.getRoundTripNumber(),
                                             getComponentFormattetName(),
                                             result.getTimestamp(),
                                             result.toReport(),
                                             component.getEventID(),
                                             result.isSuccess());
        } catch (CommunicationException e) {
            resultCollector.setSuccess(false);
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
