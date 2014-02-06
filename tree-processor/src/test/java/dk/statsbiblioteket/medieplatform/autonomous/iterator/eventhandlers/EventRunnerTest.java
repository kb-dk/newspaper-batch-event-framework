package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEventType;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** Test of the EventRunner class. */
public class EventRunnerTest {

    /**
     * Verifies that the BatchStructureChecker correctly passes the event for a simple batch with two leafs
     * to the attached event handlers.
     */
    @Test
    public void testStructureCalls() throws Exception {
        // Setup fixture
        TreeIterator treeIteratorMock = mock(TreeIterator.class);
        when(treeIteratorMock.hasNext()).
                thenReturn(true).thenReturn(true).thenReturn(true).//Begins
                thenReturn(true).thenReturn(true).                 //Attributes
                thenReturn(true).thenReturn(true).thenReturn(true).//Ends
                thenReturn(false);
        NodeBeginsParsingEvent batchNodeBegin = new NodeBeginsParsingEvent("BatchNode", null);
        NodeBeginsParsingEvent reelNodeBegin = new NodeBeginsParsingEvent("ReelNode", null);
        NodeBeginsParsingEvent dateNodeBegin = new NodeBeginsParsingEvent("DateNode", null);
        AttributeParsingEvent pageJp2Attribute = createAttributeParsingEventStub("pageJp2Attribute");
        AttributeParsingEvent pageXmlAttribute = createAttributeParsingEventStub("pageXmlAttribute");
        NodeEndParsingEvent dateNodeEnd = new NodeEndParsingEvent("DateNode", null);
        NodeEndParsingEvent reelNodeEnd = new NodeEndParsingEvent("ReelNode", null);
        NodeEndParsingEvent batchNodeEnd = new NodeEndParsingEvent("BatchNode", null);
        when(treeIteratorMock.next()).
                thenReturn(batchNodeBegin).
                thenReturn(reelNodeBegin).
                thenReturn(dateNodeBegin).
                thenReturn(pageJp2Attribute).
                thenReturn(pageXmlAttribute).
                thenReturn(dateNodeEnd).
                thenReturn(reelNodeEnd).
                thenReturn(batchNodeEnd);

        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);

        //Perform test
        EventRunner batchStructureCheckerUT = new EventRunner(treeIteratorMock);
        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandlerMock});
        batchStructureCheckerUT.runEvents(eventHandlers, null);

        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(dateNodeBegin);
        verify(treeEventHandlerMock).handleAttribute(pageJp2Attribute);
        verify(treeEventHandlerMock).handleAttribute(pageXmlAttribute);
        verify(treeEventHandlerMock).handleNodeEnd(dateNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(reelNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(batchNodeEnd);
        verify(treeEventHandlerMock).handleFinish();
        verifyNoMoreInteractions(treeEventHandlerMock);
    }

    /**
     * Verifies that the BatchStructureChecker correctly passes the event for a simple batch with two leafs
     * to the attached event handlers.
     */
    @Test
    public void testInjection() throws Exception {
        // Setup fixture
        TreeIterator treeIteratorMock = mock(TreeIterator.class);
        when(treeIteratorMock.hasNext()).
                thenReturn(true).thenReturn(true).thenReturn(true).//Begins
                thenReturn(true).thenReturn(true).thenReturn(true).                 //Attributes
                thenReturn(true).thenReturn(true).thenReturn(true).//Ends
                thenReturn(false);
        NodeBeginsParsingEvent batchNodeBegin = new NodeBeginsParsingEvent("BatchNode", null);
        NodeBeginsParsingEvent reelNodeBegin = new NodeBeginsParsingEvent("ReelNode", null);
        NodeBeginsParsingEvent dateNodeBegin = new NodeBeginsParsingEvent("DateNode", null);
        AttributeParsingEvent pageJp2Attribute = createAttributeParsingEventStub("pageJp2Attribute");
        AttributeParsingEvent pageXmlAttribute = createAttributeParsingEventStub("pageXmlAttribute");
        NodeEndParsingEvent dateNodeEnd = new NodeEndParsingEvent("DateNode", null);
        NodeEndParsingEvent reelNodeEnd = new NodeEndParsingEvent("ReelNode", null);
        NodeEndParsingEvent batchNodeEnd = new NodeEndParsingEvent("BatchNode", null);
        when(treeIteratorMock.next()).
                thenReturn(batchNodeBegin).
                thenReturn(reelNodeBegin).
                thenReturn(dateNodeBegin).
                thenReturn(pageJp2Attribute).

                thenReturn(pageXmlAttribute).
                thenReturn(dateNodeEnd).
                thenReturn(reelNodeEnd).
                thenReturn(batchNodeEnd);

        final AttributeParsingEvent injected = createAttributeParsingEventStub("injectedAttribute");

        final InjectingTreeEventHandler treeEventHandlerMock = mock(InjectingTreeEventHandler.class);

        doCallRealMethod().when(treeEventHandlerMock).pushInjectedEvent(injected);
        doCallRealMethod().when(treeEventHandlerMock).popInjectedEvent();

        doAnswer(
                new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {

                        InjectingTreeEventHandler that = (InjectingTreeEventHandler) invocation.getMock();
                        that.pushInjectedEvent(injected);
                        return null;
                    }
                }).when(treeEventHandlerMock).handleAttribute(pageJp2Attribute);

        //Perform test
        EventRunner batchStructureCheckerUT = new EventRunner(treeIteratorMock);
        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandlerMock});
        batchStructureCheckerUT.runEvents(eventHandlers, null);

        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin);
        verify(treeEventHandlerMock, times(9)).popInjectedEvent();
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(dateNodeBegin);
        verify(treeEventHandlerMock).handleAttribute(pageJp2Attribute);
        verify(treeEventHandlerMock).pushInjectedEvent(injected);
        verify(treeEventHandlerMock).handleAttribute(injected);
        verify(treeEventHandlerMock).handleAttribute(pageXmlAttribute);
        verify(treeEventHandlerMock).handleNodeEnd(dateNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(reelNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(batchNodeEnd);
        verify(treeEventHandlerMock).handleFinish();
        verifyNoMoreInteractions(treeEventHandlerMock);
    }

    /** @return Creates a attribute event and marks is as type 'Attribute'. */
    private AttributeParsingEvent createAttributeParsingEventStub(final String name) {
        AttributeParsingEvent event = mock(AttributeParsingEvent.class);
        when(event.getType()).thenReturn(ParsingEventType.Attribute);
        return event;
    }
}
