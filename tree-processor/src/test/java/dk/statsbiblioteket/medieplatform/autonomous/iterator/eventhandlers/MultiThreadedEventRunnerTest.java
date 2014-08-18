package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEventType;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** Test of the EventRunner class. */
public class MultiThreadedEventRunnerTest {

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

        List<TreeEventHandler> eventHandlers = Arrays.asList(treeEventHandlerMock);
        EventRunner batchStructureCheckerUT = new MultiThreadedEventRunner(treeIteratorMock, eventHandlers,null,MultiThreadedEventRunner.singleThreaded,
                Executors.newFixedThreadPool(2));
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
        when(treeIteratorMock.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(
                true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
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
        List<TreeEventHandler> eventHandlers = Arrays.asList(treeEventHandlerMock);
        EventRunner batchStructureCheckerUT = new MultiThreadedEventRunner(treeIteratorMock, eventHandlers, null,MultiThreadedEventRunner.singleThreaded,Executors.newFixedThreadPool(2));
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

    /**
     * Verifies that the BatchStructureChecker correctly passes the event for a simple batch with two leafs
     * to the attached event handlers.
     */
    @Test
    public void testMultithreadedInjection() throws Exception {
        // Setup fixture
        TreeIterator treeIteratorMock = mock(TreeIterator.class);
        TreeIterator date1IteratorMock = mock(TreeIterator.class);
        TreeIterator date2IteratorMock = mock(TreeIterator.class);
        when(treeIteratorMock.hasNext()).
                                                thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).//Begins
                thenReturn(true).thenReturn(true).//Ends
                thenReturn(false);
        when(date1IteratorMock.hasNext()).
                thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).                 //Attributes
                thenReturn(false);
        when(date2IteratorMock.hasNext()).
                                                 thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).                 //Attributes
                thenReturn(false);
        NodeBeginsParsingEvent batchNodeBegin = new NodeBeginsParsingEvent("BatchNode", null);
        NodeBeginsParsingEvent reelNodeBegin = new NodeBeginsParsingEvent("ReelNode", null);

        NodeBeginsParsingEvent dateNodeBegin = new NodeBeginsParsingEvent("DateNode", null);
        AttributeParsingEvent pageJp2Attribute = createAttributeParsingEventStub("pageJp2Attribute");
        AttributeParsingEvent pageXmlAttribute = createAttributeParsingEventStub("pageXmlAttribute");
        NodeEndParsingEvent dateNodeEnd = new NodeEndParsingEvent("DateNode", null);

        NodeBeginsParsingEvent date2NodeBegin = new NodeBeginsParsingEvent("DateNode2", null);
        AttributeParsingEvent page2Jp2Attribute = createAttributeParsingEventStub("page2Jp2Attribute");
        AttributeParsingEvent page2XmlAttribute = createAttributeParsingEventStub("page2XmlAttribute");
        NodeEndParsingEvent date2NodeEnd = new NodeEndParsingEvent("DateNode2", null);

        NodeEndParsingEvent reelNodeEnd = new NodeEndParsingEvent("ReelNode", null);
        NodeEndParsingEvent batchNodeEnd = new NodeEndParsingEvent("BatchNode", null);
        when(treeIteratorMock.skipToNextSibling()).thenReturn(date1IteratorMock).thenReturn(date2IteratorMock).thenReturn(null);

        when(treeIteratorMock.next()).
                                             thenReturn(batchNodeBegin).
                                             thenReturn(reelNodeBegin).
                                             thenReturn(dateNodeBegin).
                                             thenReturn(date2NodeBegin).
                                             thenReturn(reelNodeEnd).
                                             thenReturn(batchNodeEnd);
        when(date1IteratorMock.next()).

                                             thenReturn(dateNodeBegin).
                                             thenReturn(pageJp2Attribute).
                                             thenReturn(pageXmlAttribute).
                                             thenReturn(dateNodeEnd)
                                            ;
        when(date2IteratorMock.next()).
                                              thenReturn(date2NodeBegin).
                                              thenReturn(page2Jp2Attribute).
                                              thenReturn(page2XmlAttribute).
                                              thenReturn(date2NodeEnd);
        final AttributeParsingEvent injected = createAttributeParsingEventStub("injectedAttribute");
        final TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);
        //Perform test
        List<TreeEventHandler> eventHandlers = Arrays.asList(treeEventHandlerMock);
        EventRunner batchStructureCheckerUT = new MultiThreadedEventRunner(treeIteratorMock,
                eventHandlers,
                null,
                new MultiThreadedEventRunner.EventCondition() {
                    @Override
                    public boolean shouldFork(ParsingEvent nodeBeginsParsingEvent) {
                        return nodeBeginsParsingEvent.getName().startsWith("DateNode");
                    }

                    @Override
                    public boolean shouldJoin(ParsingEvent nodeEndParsingEvent) {
                        return nodeEndParsingEvent.getName().startsWith("ReelNode");
                    }
                },
                Executors.newFixedThreadPool(2));
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                EventRunner runner = (EventRunner) invocation.getArguments()[1];
                runner.pushEvent(injected);
                return null;
            }
        }).when(treeEventHandlerMock).handleAttribute(eq(pageJp2Attribute), isA(EventRunner.class));
        batchStructureCheckerUT.run();
        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin, batchStructureCheckerUT);
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin, batchStructureCheckerUT);

        verify(treeEventHandlerMock).handleNodeBegin(eq(dateNodeBegin), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleAttribute(eq(pageJp2Attribute), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleAttribute(eq(injected), isA(EventRunner.class));
      verify(treeEventHandlerMock).handleAttribute(eq(pageXmlAttribute), isA(EventRunner.class));
       verify(treeEventHandlerMock).handleNodeEnd(eq(dateNodeEnd), isA(EventRunner.class));



        verify(treeEventHandlerMock).handleNodeBegin(eq(date2NodeBegin), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleAttribute(eq(page2Jp2Attribute), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleAttribute(eq(injected), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleAttribute(eq(page2XmlAttribute), isA(EventRunner.class));
        verify(treeEventHandlerMock).handleNodeEnd(eq(date2NodeEnd), isA(EventRunner.class));

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
