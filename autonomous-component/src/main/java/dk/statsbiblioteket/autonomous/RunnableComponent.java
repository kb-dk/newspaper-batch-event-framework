package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

public interface RunnableComponent {

    public String getComponentName();

    public String getComponentVersion();

    public EventID getEventID();

    /**
     * This is the worker method for the component. Once a batch have been found that match the criteria and this batch
     * have been properly locked, this method is called.
     * The results of the work should be collected in the resultCollector. These will be added to the event system afterwards
     * @param batch the batch to work on
     * @param resultCollector the result collector
     * @throws Exception if something failed
     */
    public abstract void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception;

}
