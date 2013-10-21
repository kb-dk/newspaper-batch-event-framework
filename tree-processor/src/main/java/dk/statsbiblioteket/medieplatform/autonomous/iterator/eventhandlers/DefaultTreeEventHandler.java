package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

/**
 * Abstract tree event handler, with no-op methods
 */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
    }

    @Override
    public void handleFinish() {
    }
}
