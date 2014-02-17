package dk.statsbibliokeket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEventType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestEventHelper {

    public static NodeBeginsParsingEvent createBatchBeginEvent(int sequenceNumber) {
        return new NodeBeginsParsingEvent("B40002202824" + sequenceNumber + "RT1");
    }

    public static NodeBeginsParsingEvent createReelBeginEvent(int sequenceNumber) {
        return new NodeBeginsParsingEvent("B400022028241" + sequenceNumber);
    }

    public static NodeBeginsParsingEvent createUdgaveBeginEvent(int sequenceNumber) {
        return new NodeBeginsParsingEvent("1850-10-18-0" + sequenceNumber);
    }

    /**
     * @return Creates a attribute event and marks is as type 'Attribute'.
     */
    public static AttributeParsingEvent createAttributeParsingEventStub(final String name) {
        AttributeParsingEvent event = mock(AttributeParsingEvent.class);
        when(event.getType()).thenReturn(ParsingEventType.Attribute);
        return event;
    }
}
