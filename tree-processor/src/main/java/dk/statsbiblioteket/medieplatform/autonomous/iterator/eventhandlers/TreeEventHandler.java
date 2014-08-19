package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

/** Interface for tree event handlers */
public interface TreeEventHandler {
    /**
     * Signifies that a node with nested elements will be processed.
     *
     * @param event Contains information on the node.
     */
    public void handleNodeBegin(NodeBeginsParsingEvent event, EventRunner runner);

    /**
     * Signifies that a node end has been reached.
     *
     * @param event Contains information on the node.
     */
    public void handleNodeEnd(NodeEndParsingEvent event, EventRunner runner);

    /**
     * Signifies that a leaf has been reached.
     *
     * @param event Contains information on leaf.
     */
    public void handleAttribute(AttributeParsingEvent event, EventRunner runner);

    /**
     * Signifies that the parsing of the batch has been is finished, and any crosscutting batch
     * analysis should done.
     */
    public void handleFinish(EventRunner runner);
}
