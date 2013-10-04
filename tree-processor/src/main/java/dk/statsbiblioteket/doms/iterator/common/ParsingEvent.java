package dk.statsbiblioteket.doms.iterator.common;

/**
 * This class represents an Event, which is the basic building block of the iterator. An event will happen at a given
 * path in the parsing of the tree structure. Get this path with the getPath method. An event will be of a type. Get
 * this with the getType method
 */
public abstract class ParsingEvent {

    protected final EventType type;
    protected final String localname;

    public ParsingEvent(String localname,
                        EventType type) {
        this.localname = localname;
        this.type = type;
    }

    /**
     * This represents the "location" of the parser when the event was encountered. It will be a series of identifiers
     * separated by File.separator
     *
     * @return the path
     */
    public String getPath() {
        return localname;
    }

    /**
     * Get the type of event.
     *
     * @return the type
     * @see EventType
     */
    public EventType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ParsingEvent{" +
               "type=" + type +
               ", localname='" + localname + '\'' +
               '}';
    }
}
