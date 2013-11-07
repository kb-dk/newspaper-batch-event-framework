package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.InMemoryAttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import junit.framework.Assert;
import org.testng.annotations.Test;

public class InjectingTreeEventHandlerTest {

    @Test
    public void testInjectingTreeHandler(){
        InjectingTreeEventHandler injectingTreeEventHandler = new InjectingTreeEventHandler() {

            @Override
            public void handleNodeBegin(NodeBeginsParsingEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void handleNodeEnd(NodeEndParsingEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void handleAttribute(AttributeParsingEvent event) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void handleFinish() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        InMemoryAttributeParsingEvent parsingEventBefore = new InMemoryAttributeParsingEvent("testEvent",new byte[]{5,5,5},"checksum");

        injectingTreeEventHandler.pushInjectedEvent(parsingEventBefore);
        injectingTreeEventHandler.pushInjectedEvent(parsingEventBefore);

        ParsingEvent event1 = injectingTreeEventHandler.popInjectedEvent();
        Assert.assertEquals(parsingEventBefore,event1);


        ParsingEvent event2 = injectingTreeEventHandler.popInjectedEvent();
        Assert.assertEquals(parsingEventBefore,event2);


    }
}
