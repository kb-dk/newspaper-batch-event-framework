package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;

/**
 * This is the interface a component implementer must use.
 */
public interface RunnableComponent {

    /**
     * Get the name of the component. Used as part of the event/failure message, and for locking batches
     * to components
     * @return the component name
     */
    public String getComponentName();

    /**
     * Get the version of the component. Used as part of the event/failure message.
     * @return the component version
     */
    public String getComponentVersion();

    /**
     * The EventID that the work done by this component can be identified as
     * @return the event id
     */
    public String getEventID();

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
