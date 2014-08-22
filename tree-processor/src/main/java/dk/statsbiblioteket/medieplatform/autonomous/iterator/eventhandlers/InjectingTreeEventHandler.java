package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InjectingTreeEventHandler extends DefaultTreeEventHandler{

    private static final ThreadLocal<Queue<AttributeParsingEvent>> eventQueue = new ThreadLocal<Queue<AttributeParsingEvent>>(){
        @Override
        protected Queue<AttributeParsingEvent> initialValue() {
            return new ConcurrentLinkedQueue<>();
        }
    };



    public final void pushEvent(AttributeParsingEvent event){
        this.eventQueue.get().add(event);
    }

    public final AttributeParsingEvent popEvent(){
        return this.eventQueue.get().poll();
    }
}
