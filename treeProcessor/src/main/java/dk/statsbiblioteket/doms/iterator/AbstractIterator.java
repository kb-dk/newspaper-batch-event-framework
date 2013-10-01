package dk.statsbiblioteket.doms.iterator;

import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The abstract iterator, meant as a common superclass for implementations for different backends.
 * The system is designed as a series of Iterators. Each iterator represents a non-leaf node in the tree.
 * It has an ID of type T and iterators for attributes and children.
 * When iterating, first the attributes are encountered. When all attributes are used, iteration through the children
 * begin. A delegate Iterator is initialised with the id of the first child. When this iterator runs out, the delegate
 * is initialised to the second child.
 * As such, the abstract iterator will often be a long chain of iterators. The "Current" iterator will always be the
 * iterator with no delegate.
 *
 * @param <T> The type of identifier used for the nodes.
 */
public abstract class AbstractIterator<T> implements TreeIterator {


    private Iterator<TreeIterator> childrenIterator;
    private Iterator<T> attributeIterator;
    protected final T id;

    private TreeIterator delegate = null;
    private boolean done = false;
    private boolean begun = false;

    protected AbstractIterator(T id) {
        this.id = id;
    }

    @Override
    public final boolean hasNext() {
        // We are done with this node and all subnodes.
        return !done;

    }



    @Override
    public final Event next() {
        if (!hasNext()) {//general catch-all, we know there is something to come from this iterator
            throw new NoSuchElementException("The iterator is out of objects");
        }
        //So now we must figure out what kind of event is next

        //We have not sent the "NodeBeginEvent" yet, so get that done before anything else
        if (!begun) {
            //!begun implied delegate==null
            Event event = new NodeBeginsEvent(getIdOfNode());
            begun = true;
            return event;
        }
        //After the NodeBeginsEvent, iterate the AttributeEvents
        if (getAttributeIterator().hasNext()) {
            //begun==true implies attributeIterator!=null
            T attributeID = getAttributeIterator().next();
            return makeAttributeEvent(id, attributeID);
        }


        //We are now finished iterating the attributes, and we check if we have a delegate
        //If we have one, forward the request to the delegate
        if (delegate != null) {
            //delegate != null implied attributeIterator is empty
            if (delegate.hasNext()) {//forward the request the delegate
                return delegate.next();
            } else {
                //The delegate is out of objects, so ditch it
                delegate = null;
            }
        }
        //If we got to here delegate==null, ie. we either have never had a delegate or we just exhausted the one we had
        if (getChildrenIterator().hasNext()) {
            delegate = getChildrenIterator().next();
            return delegate.next();
        } else {
            //Okay, so we have exhausted the children iterator also.
            if (done) {
                //And we have given a "NodeEndEvent"
                throw new NoSuchElementException("Iterator for id" + id + " exhausted");
            } else {
                //We have not given the "NodeEndEvent" so return this.
                done = true;
                return new NodeEndEvent(getIdOfNode());
            }
        }
    }

    /**
     * Get the children iterator, initilise if if needed. As initialising the children iterator can be a somewhat expensive
     * task, do not do it before requested
     *
     * @return the children iterator
     */
    protected synchronized Iterator<TreeIterator> getChildrenIterator() {
        if (childrenIterator == null) {
            childrenIterator = initializeChildrenIterator();
        }
        return childrenIterator;
    }

    protected abstract Iterator<TreeIterator> initializeChildrenIterator();


    protected synchronized Iterator<T> getAttributeIterator(){
        if (attributeIterator == null){
            attributeIterator = initilizeAttributeIterator();
        }
        return attributeIterator;
    }
    protected abstract Iterator<T> initilizeAttributeIterator();

    protected abstract AttributeEvent makeAttributeEvent(T id, T attributeID);

    /**
     * Convert a attributeID to a human readable string
     *
     * @param attributeID the attribute id
     * @return the readable version
     */
    protected String getIdOfAttribute(T attributeID) {
        return attributeID.toString();
    }

    protected String getIdOfNode() {
        return id.toString();
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }


    public TreeIterator skipToNextSibling() {


        if (delegate == null) {
            //we have no delegate, so the current node is the root node of the tree
            //So this is a not very useful function.
            return this;
        }
        //Okay, so we are not the root node, as we have a delegate.

        //If our delegate also have a delegate, we know that we are not the current node either
        if (delegate.getDelegate() != null) {
            //, so proceed downwards
            return delegate.skipToNextSibling();
        }

        //Okay, so we know that we have a delegate, and this delegate does not have a delegate. We are in
        //the correct location

        //Save our delegate
        TreeIterator oldDelegate = delegate;
        //disconnect it. If the delegate is null, the children iterator will be next'ed to get a next child as
        //delegate on the following next operation
        delegate = null;
        //reset the disconnected delegate, as we are probably somewhat inside it
        oldDelegate.reset();
        //return it
        return oldDelegate;


    }

    /**
     * Reset this iterator/node, so that iteration from here will start fresh.
     */
    public void reset() {
        delegate = null;
        done = false;
        begun = false;
        childrenIterator = null;
        attributeIterator = null;
    }

    public final TreeIterator getDelegate() {
        return delegate;
    }


}
