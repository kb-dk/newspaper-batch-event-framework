package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a the Parsing Event of encountering a File
 */

public class FileAttributeParsingEvent extends AttributeParsingEvent {


    private File file;

    public FileAttributeParsingEvent(String localname,
                                     File file) {
        super(localname);
        this.file = file;
    }

    @Override
    public InputStream getText() throws IOException {
        return new FileInputStream(file);
    }


}
