package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This class represents an Event, which is the basic building block of the iterator. An event will happen at a given
 * node in the parsing of the tree structure. Get the name of this node with the getName method. An event will be of a
 * type. Get
 * this with the getType method
 */
public abstract class ParsingEvent {

    protected final ParsingEventType type;
    protected final String name;
    protected final String location;

    /**
     * Constructor for this class.
     * @param name The name of this event in the parse tree.
     * @param type The type of the event.
     * @param location A String specifying location information associated with this event (e.g. a
     *                 a fedora pid or filepath). The interpretation of this parameter is implementation
     *                 dependent. May be null.
     */
    public ParsingEvent(String name, ParsingEventType type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    /**
     * This represents the "name" of the the directory/file/object when the event was encountered.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the location String for this event. The interpretation of this event is implementation-specific and it
     * is not guaranteed to be non-null.
     * @return the location.
     */
    public String getLocation() {
        return location;
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
               ", name='" + name + '\'' +
               '}';
    }
}
