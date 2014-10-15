package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This is a specialisation of the TreeIterator. Implementations function as a series of tree iterators. Each
 * iterator delegates the work downwards until it reaches one that does not have a delegate.
 */
public interface DelegatingTreeIterator extends TreeIterator {


    /** Reset this iterator/node, so that iteration from here will start fresh. */
    void reset();


    /**
     * Return the TreeIterator (if any) of the child-element over which this element is currently iterating, or null if
     * the delegate has not yet been initialised.
     *
     * @return
     */
    TreeIterator getDelegate();

}
