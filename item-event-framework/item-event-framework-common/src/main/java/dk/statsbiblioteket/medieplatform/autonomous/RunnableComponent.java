package dk.statsbiblioteket.medieplatform.autonomous;

import org.slieb.throwables.FunctionWithThrowable;

/** This is the interface a component implementer must use.
 * Implement either doWorkOnItem or apply, your choice, but just one of them
 * */
public interface RunnableComponent<T extends Item> extends FunctionWithThrowable<T, ResultCollector, Exception> {
    /**
     * Get the name of the component. Used as part of the event/failure message, and for locking batches
     * to components
     *
     * @return the component name
     */
    default String getComponentName() {
        return getClass().getSimpleName();
    }

    /**
     * Get the version of the component. Used as part of the event/failure message.
     *
     * @return the component version
     */
    default String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    /**
     * The EventID that the work done by this component can be identified as
     *
     * @return the event id
     */
    String getEventID();


    default void doWorkOnItem(T item, ResultCollector resultCollector) throws Exception {
        ResultCollector result = apply(item);
        result.mergeInto(resultCollector);
    }

    @Override
    /**
     * This is the worker method for the component. Once a item have been found that match the criteria and this item
     * have been properly locked, this method is called.
     * The results of the work should be collected in the resultCollector. These will be added to the event system
     * afterwards
     *
     * @param item            the item to work on
     * @return the result collector
     * @throws Exception if something failed
     */
    default ResultCollector applyWithThrowable(T item) throws Exception{
        ResultCollector resultCollector = new ResultCollector(getComponentName(), getComponentVersion(), 1000);
        doWorkOnItem(item,resultCollector);
        return resultCollector;
    }

}
