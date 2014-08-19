package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

/** Abstract tree event handler, with no-op methods */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {

    @Override
    public final void handleNodeBegin(NodeBeginsParsingEvent event, EventRunner runner) {
        handleNodeBegin(event);
    }

    @Override
    public final void handleNodeEnd(NodeEndParsingEvent event, EventRunner runner) {
        handleNodeEnd(event);
    }

    @Override
    public final void handleAttribute(AttributeParsingEvent event, EventRunner runner) {
        handleAttribute(event);
    }

    @Override
    public final void handleFinish(EventRunner runner) {
        handleFinish();
    }


    public void handleFinish() {
    }

    public void handleNodeBegin(NodeBeginsParsingEvent event) {
    }

    public void handleNodeEnd(NodeEndParsingEvent event) {
    }

    public void handleAttribute(AttributeParsingEvent event) {
    }
}
