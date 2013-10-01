package dk.statsbiblioteket.doms.iterator.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Event {

    protected final EventType type;
    protected final String path;
    protected final String localname;




    public Event(String localname, String path, EventType type) {
        this.localname = localname;
        this.path = path;
        this.type = type;
    }

    public String getLocalname() {
        return localname;
    }

    public String getPath() {
        return path;
    }

    public EventType getType() {
        return type;
    }

    public abstract InputStream getText() throws IOException;

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", path='" + path +"/"+ localname+'\'' +
                "} ";
    }
}
