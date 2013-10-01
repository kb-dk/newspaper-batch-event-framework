package dk.statsbiblioteket.doms.iterator.common;

import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeBeginsEvent extends Event {


    public NodeBeginsEvent(String localname, String path) {
        super(localname, path, EventType.NodeBegin);
    }

    @Override
    public InputStream getText() {
        throw new UnsupportedOperationException("Nodes do not have text");
    }

}
