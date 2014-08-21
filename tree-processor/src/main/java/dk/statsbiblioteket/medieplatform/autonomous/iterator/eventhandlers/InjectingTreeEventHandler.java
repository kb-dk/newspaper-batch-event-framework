package dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InjectingTreeEventHandler extends DefaultTreeEventHandler{

    private static final ThreadLocal<Queue<ParsingEvent>> eventQueue = new ThreadLocal<Queue<ParsingEvent>>(){
        @Override
        protected Queue<ParsingEvent> initialValue() {
            return new ConcurrentLinkedQueue<>();
        }
    };



    public final void pushEvent(ParsingEvent event){
        this.eventQueue.get().add(event);
    }

    public final ParsingEvent popEvent(){
        return this.eventQueue.get().poll();
    }
}
