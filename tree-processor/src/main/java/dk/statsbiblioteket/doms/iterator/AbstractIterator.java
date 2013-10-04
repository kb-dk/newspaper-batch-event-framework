package dk.statsbiblioteket.doms.iterator;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
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
public abstract class AbstractIterator<T> implements DelegatingTreeIterator {


    private Iterator<DelegatingTreeIterator> childrenIterator;
    private Iterator<T> attributeIterator;
    protected final T id;

    private DelegatingTreeIterator delegate = null;
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


    /**
     * This iterator iterates over the current node. It lists first all attributes of the current node and then begins
     * iterating over the child nodes. It has the side effect that once it is finished iterating over attributes, it
     * will create the first delegate (ie first child node to be iterated over). The delegate is set back to null when the
     * last child has been read. Therefore calls to this method will have the side effect of changing subsequent calls to
     * getDelegate() from null to non-null, and back to null again at various points in the iteration cycle.
     * @return the next ParsingEvent
     * @throws NoSuchElementException if we are already finished iterating this object.
     */
    @Override
    public final ParsingEvent next() {
        if (!hasNext()) {//general catch-all, we know there is something to come from this iterator
            throw new NoSuchElementException("The iterator is out of objects");
        }
        //So now we must figure out what kind of event is next

        //We have not sent the "NodeBeginEvent" yet, so get that done before anything else
        if (!begun) {
            //!begun implied delegate==null
            ParsingEvent parsingEvent = new NodeBeginsParsingEvent(getIdOfNode());
            begun = true;
            return parsingEvent;
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
                return new NodeEndParsingEvent(getIdOfNode());
            }
        }

    }

    /**
     * Get the children iterator, initilised lazily.
     *
     * @return the children iterator
     */
    protected synchronized Iterator<DelegatingTreeIterator> getChildrenIterator() {
        if (childrenIterator == null) {
            childrenIterator = initializeChildrenIterator();
        }
        return childrenIterator;
    }

    /**
     * This is a factory method which creates an iterator over all the children of this element. Typically it will
     * just call a constructor defined in an implementation of this class for each child and return an iterator of the
     * resulting objects.
     * @return the children iterator
     */
    protected abstract Iterator<DelegatingTreeIterator> initializeChildrenIterator();


    /**
     * Get the iterator over attributes of the current element, initializing it needed.
     * @return the attribute iterator
     */
    protected synchronized Iterator<T> getAttributeIterator(){
        if (attributeIterator == null){
            attributeIterator = initilizeAttributeIterator();
        }
        return attributeIterator;
    }


    /**
     * This is a factory method which creates an iterator over all attributes of this element, for example all files
     * in a directory.
     * @return
     */
    protected abstract Iterator<T> initilizeAttributeIterator();

    /**
     * Returns an instance of a concrete subclass of AttributeEvent appropriate for this attribute. There could be
     * different kinds of attribute in a given element and these could be identified and given different behaviours.
     * @param nodeID the identifier of the node that the attribute resides in
     * @param attributeID the identifier of the attribute.
     * @return an AttributeParsingEvent
     */
    protected abstract AttributeParsingEvent makeAttributeEvent(T nodeID, T attributeID);

    protected String getIdOfNode() {
        return id.toString();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }


    @Override
    /**
     * This method recursively follows the chain of delegates down from the node it was called on to the node currently
     * being processed. It identifies the node currently being processed as being that node which has a delegate, but
     * whose delegate has no delegate.
     */
    public TreeIterator skipToNextSibling() {

        if (delegate == null) {
            //we have no delegate. The only way this can happen is if this method have been called on the
            //root node. We would never recurse to a node without a delegate.
            //Return this, as there is not very much else to do
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
        DelegatingTreeIterator oldDelegate = delegate;
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
    @Override
    public void reset() {
        delegate = null;
        done = false;
        begun = false;
        childrenIterator = null;
        attributeIterator = null;
    }

    @Override
    public final TreeIterator getDelegate() {
        return delegate;
    }


}
