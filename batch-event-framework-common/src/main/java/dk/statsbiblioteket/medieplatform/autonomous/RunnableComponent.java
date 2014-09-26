package dk.statsbiblioteket.medieplatform.autonomous;

/** This is the interface a component implementer must use. */
public interface RunnableComponent {
    /**
     * Get the name of the component. Used as part of the event/failure message, and for locking batches
     * to components
     *
     * @return the component name
     */
    String getComponentName();

    /**
     * Get the version of the component. Used as part of the event/failure message.
     *
     * @return the component version
     */
    String getComponentVersion();

    /**
     * The EventID that the work done by this component can be identified as
     *
     * @return the event id
     */
    String getEventID();

    /**
     * This is the worker method for the component. Once a item have been found that match the criteria and this item
     * have been properly locked, this method is called.
     * The results of the work should be collected in the resultCollector. These will be added to the event system
     * afterwards
     *
     * @param item           the item to work on
     * @param resultCollector the result collector
     *
     * @throws Exception if something failed
     */
    void doWorkOnItem(Item item, ResultCollector resultCollector) throws Exception;

    /**
     * This is the worker method for the component. Once a item have been found that match the criteria and this item
     * have been properly locked, this method is called.
     * The results of the work should be collected in the resultCollector. These will be added to the event system
     * afterwards
     *
     * @param batch           the batch to work on
     * @param resultCollector the result collector
     *
     * @throws Exception if something failed
     */
    void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception;
}
