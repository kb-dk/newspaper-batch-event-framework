package dk.statsbiblioteket.doms.iterator.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents an attribute in the tree. An attribute is a node containing data directly, such as a
 * file system file or a fedora datastream.
 */
public abstract class AttributeEvent extends Event{


    public AttributeEvent(String localname, EventType type) {
        super(localname, type);
    }

    /**
     * Get the corresponding data
     * @return the data
     * @throws java.io.IOException
     */
    public abstract InputStream getText() throws IOException;

}
