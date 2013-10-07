package dk.statsbiblioteket.doms.iterator.common;

/**
 * This class represents an Event, which is the basic building block of the iterator. An event will happen at a given
 * path in the parsing of the tree structure. Get this path with the getPath method. An event will be of a type. Get
 * this with the getType method
 */
public abstract class ParsingEvent {

    protected final ParsingEventType type;
    protected final String localname;

    public ParsingEvent(String localname,
                        ParsingEventType type) {
        this.localname = localname;
        this.type = type;
    }

    /**
     * This represents the "name" of the the directory/file/object when the event was encountered.
     * @return the local name
     */
    public String getLocalname() {
        return localname;
    }

    /**
     * Get the type of event.
     *
     * @return the type
     * @see ParsingEventType
     */
    public ParsingEventType getType() {
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
