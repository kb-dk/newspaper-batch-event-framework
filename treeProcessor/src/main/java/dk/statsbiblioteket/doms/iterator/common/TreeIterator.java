package dk.statsbiblioteket.doms.iterator.common;

import java.util.Iterator;

/**
 * The iterator interface for the tree structure. Iterates over the tree, which will be given as a series of Events
 *
 * @see Event
 * @see EventType
 * @see AttributeEvent
 * @see NodeBeginsEvent
 * @see NodeBeginsEvent
 *
 *
 */
public interface TreeIterator extends Iterator<Event> {

    /**
     * Do:
     *  Extract the subtree originating from the current node as a separate TreeIterator.
     *  Skip to the next node begin event that is not in this subtree.
     * @return the subtree originating from the current node
     */
    public TreeIterator skipToNextSibling();


    /**
     * Reset this iterator/node, so that iteration from here will start fresh.
     */
    void reset();

    /**
     * Return the TreeIterator (if any) of the child-element over which this element is currently iterating, or null if
     * one if the delegate has not yet been initialised.
     * @return
     */
    TreeIterator getDelegate();

}
