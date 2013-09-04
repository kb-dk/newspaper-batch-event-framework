package dk.statsbiblioteket.doms.iterator;

import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndEvent;
import dk.statsbiblioteket.doms.iterator.common.SBIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class  AbstractIterator<T> implements SBIterator{



    //information about this node
    protected Iterator<T> childrenIterator;
    protected Iterator<T> attributeIterator;
    protected final T id;
    private String parents;
    private String label;


    //The iterator stuff
    private AbstractIterator delegate = null;
    private boolean done = false;
    private boolean begun = false;

    protected AbstractIterator(T id, String parents, String label) {
        this.id = id;
        this.parents = parents;
        this.label = label;
    }

    public boolean hasNext() {
        if (delegate != null) {
            return true;
        }

        if (!begun){
            return true;
        }
        if (attributeIterator == null){
            return !done;
        }
        //first we iterate through the datastreams, then the children
        if (attributeIterator.hasNext()) {
            return true;
        } else{
            if (childrenIterator == null){
                childrenIterator = initializeChildrenIterator();
            }
            if (childrenIterator.hasNext()) {
                return true;
            } else {
                return !done;
            }
        }

    }

    protected abstract Iterator<T> initializeChildrenIterator();

    public Event next() {
        if (!begun) {
            Event event = new NodeBeginsEvent(getIdOfNode(id), getPath(id));
            begun = true;
            return event;
        }

        if (delegate != null) {
            if (delegate.hasNext()) {
                return delegate.next();
            } else {
                delegate = null;
            }
        }
        if (attributeIterator == null){
            return endOrDone();
        }
        if (attributeIterator.hasNext()) {
            T attributeID = attributeIterator.next();
            return makeAttributeEvent(id,attributeID);
        }
        if (childrenIterator.hasNext()) {
            T childID = childrenIterator.next();
            delegate = makeDelegate(id,childID);

            return delegate.next();
        } else {
            return endOrDone();
        }
    }

    private Event endOrDone() {
        if (done) {
            throw new NoSuchElementException("Iterator for id"+id+" exhausted");
        } else {
            done = true;
            return new NodeEndEvent(getIdOfNode(id), getPath(id));
        }
    }

    protected abstract AbstractIterator makeDelegate(T id, T childID);

    protected abstract Event makeAttributeEvent(T id, T attributeID);

    protected String getIdOfAttribute(T attributeID) {
        return attributeID.toString();
    }

    protected String getPath(T id) {
        return "";
    }

    protected String getIdOfNode(T id) {
        return id.toString();
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }
    public void skipToEnd() {
        if (delegate != null) {
            delegate.skipToEnd();
        } else {
            attributeIterator = null;
            childrenIterator = null;
        }
    }
}
