package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This event represents the iterator leaving a node. It is given when the iterator is finished processing all attributes
 * and subtrees from the current node, just before leaving it.
 */
public class NodeEndParsingEvent extends ParsingEvent {


    public NodeEndParsingEvent(String name) {
        super(name, ParsingEventType.NodeEnd);
    }


}
