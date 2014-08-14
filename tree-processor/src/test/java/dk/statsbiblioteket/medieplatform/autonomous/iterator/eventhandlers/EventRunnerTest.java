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
import static org.mockito.Mockito.mock;
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

        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandlerMock});
        EventRunner batchStructureCheckerUT = new EventRunner(treeIteratorMock, eventHandlers,null);
        batchStructureCheckerUT.run();

        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin,batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeBegin(dateNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleAttribute(pageJp2Attribute, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleAttribute(pageXmlAttribute, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(dateNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(reelNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(batchNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleFinish(batchStructureCheckerUT);
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
        when(treeIteratorMock.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
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

        final TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);



        //Perform test
        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandlerMock});
        EventRunner batchStructureCheckerUT = new EventRunner(treeIteratorMock, eventHandlers, null);
        doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        EventRunner runner = (EventRunner) invocation.getArguments()[1];
                        runner.pushEvent(injected);
                        return null;
                    }
                }).when(treeEventHandlerMock).handleAttribute(pageJp2Attribute, batchStructureCheckerUT);

        batchStructureCheckerUT.run();

        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeBegin(dateNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleAttribute(pageJp2Attribute, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleAttribute(injected, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleAttribute(pageXmlAttribute, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(dateNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(reelNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeEnd(batchNodeEnd, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleFinish(batchStructureCheckerUT);
        verifyNoMoreInteractions(treeEventHandlerMock);
    }

    /** @return Creates a attribute event and marks is as type 'Attribute'. */
    private AttributeParsingEvent createAttributeParsingEventStub(final String name) {
        AttributeParsingEvent event = mock(AttributeParsingEvent.class);
        when(event.getType()).thenReturn(ParsingEventType.Attribute);
        return event;
    }
}
