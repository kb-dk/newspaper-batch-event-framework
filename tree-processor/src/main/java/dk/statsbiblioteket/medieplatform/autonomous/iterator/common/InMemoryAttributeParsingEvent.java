package dk.statsbiblioteket.medieplatform.autonomous.iterator.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an implementation of the AttributeParsingEvent. It is meant to be used for injected events from the
 * InjectingTreeEventHandler
 */
public class InMemoryAttributeParsingEvent extends AttributeParsingEvent {


    private final byte[] data;
    private final String checksum;

    /**
     * Constructur
     *
     * @param name     the event name
     * @param data     the data as a byte array
     * @param checksum the checksum for the data
     */
    public InMemoryAttributeParsingEvent(String name, byte[] data, String checksum) {
        super(name, null);
        this.data = data;
        this.checksum = checksum;
    }

    @Override
    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getChecksum() throws IOException {
        return checksum;
    }
}
