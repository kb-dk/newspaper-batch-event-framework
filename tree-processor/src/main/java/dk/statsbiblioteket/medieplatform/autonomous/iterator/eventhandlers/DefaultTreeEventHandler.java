package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

/** Abstract tree event handler, with no-op methods */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {



    public void handleFinish() {
    }

    public void handleNodeBegin(NodeBeginsParsingEvent event) {
    }

    public void handleNodeEnd(NodeEndParsingEvent event) {
    }

    public void handleAttribute(AttributeParsingEvent event) {
    }
}
