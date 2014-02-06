package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This event represents the iterator encountering a node. It is given just as the iterator encounters the node, before
 * parsing of the node contents begin.
 */
public class NodeBeginsParsingEvent extends ParsingEvent {

    public NodeBeginsParsingEvent(String name) {
           super(name, ParsingEventType.NodeBegin, null);
       }

    public NodeBeginsParsingEvent(String name, String location) {
        super(name, ParsingEventType.NodeBegin, location);
    }


}
