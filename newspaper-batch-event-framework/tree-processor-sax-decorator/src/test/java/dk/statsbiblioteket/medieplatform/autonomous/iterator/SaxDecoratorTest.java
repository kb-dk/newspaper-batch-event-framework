package dk.statsbiblioteket.medieplatform.autonomous.iterator;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEventType;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import org.testng.annotations.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by abr on 12-04-16.
 */
public class SaxDecoratorTest {

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

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();


        SaxDecorator treeEventHandler = new SaxDecorator();


        DefaultHandler handler = new DefaultHandler(){
            @Override
            public void startDocument() throws SAXException {
                System.out.println("StartDocument");
                super.startDocument();
            }

            @Override
            public void endDocument() throws SAXException {
                System.out.println("EndDocument");
                super.endDocument();
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                System.out.println("StartElement uri="+uri+" localname="+localName+" qName="+qName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    System.out.println("  "+name+"="+value);
                }
                super.startElement(uri, localName, qName, attributes);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                System.out.println("EndElement uri="+uri+" localname="+localName+" qName="+qName);
                super.endElement(uri, localName, qName);
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                System.out.println("Characters '"+new String(ch,start,length).trim()+"'");
                super.characters(ch, start, length);
            }
        };

        //Perform test

        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandler});
        EventRunner batchStructureCheckerUT = new EventRunner(treeIteratorMock, eventHandlers, null);
        batchStructureCheckerUT.run();


        saxParser.parse(treeEventHandler, handler);



    }

    /** @return Creates a attribute event and marks is as type 'Attribute'. */
    private AttributeParsingEvent createAttributeParsingEventStub(final String name) {
        AttributeParsingEvent event = mock(AttributeParsingEvent.class);
        when(event.getName()).thenReturn(name);
        when(event.getType()).thenReturn(ParsingEventType.Attribute);
        return event;
    }
}