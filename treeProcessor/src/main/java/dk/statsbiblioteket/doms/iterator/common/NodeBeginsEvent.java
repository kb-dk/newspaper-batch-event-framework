package dk.statsbiblioteket.doms.iterator.common;

/**
 * This event represents the iterator encountering a node. It is given just as the iterator encounters the node, before
 * parsing of the node contents begin.
 */
public class NodeBeginsEvent extends Event {


    public NodeBeginsEvent(String localname) {
        super(localname, EventType.NodeBegin);
    }


}
