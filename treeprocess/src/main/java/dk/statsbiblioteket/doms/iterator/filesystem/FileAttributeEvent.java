package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.EventType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileAttributeEvent extends Event {


    private File file;

    public FileAttributeEvent(String localname, String path, File file) {
        super(localname, path, EventType.Attribute);
        this.file = file;
    }

    @Override
    public InputStream getText() throws IOException {
        return new FileInputStream(file);
    }


}
