package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

/**
 * This class represents an Event, which is the basic building block of the iterator. An event will happen at a given
 * node in the parsing of the tree structure. Get the name of this node with the getName method. An event will be of a type. Get
 * this with the getType method
 */
public abstract class ParsingEvent {

    protected final ParsingEventType type;
    protected final String name;

    public ParsingEvent(String name,
                        ParsingEventType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * This represents the "name" of the the directory/file/object when the event was encountered.
     * @return the name
     */
    public String getName() {
        return name;
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
