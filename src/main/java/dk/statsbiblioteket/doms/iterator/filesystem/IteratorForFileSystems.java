package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.Event;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class IteratorForFileSystems extends AbstractIterator<File> {


    private final File prefix;
    private final Collection<File> attributes;

    public IteratorForFileSystems(File dir, final File prefix) {
        super(dir,null,"");

        this.prefix = prefix;

        attributes = FileUtils.listFiles(dir, FileFileFilter.FILE, null);
        reset();

    }


    @Override
    protected Iterator<File> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        return Arrays.asList(children).iterator();
    }

    @Override
    protected AbstractIterator makeDelegate(File id, File childID) {
        return new IteratorForFileSystems(childID,prefix);
    }

    @Override
    protected Event makeAttributeEvent(File id, File attributeID) {
        return new FileAttributeEvent(getIdOfAttribute(attributeID),getPath(attributeID),attributeID);
    }

    @Override
    protected String getIdOfNode(File id) {
        return id.getName();
    }

    @Override
    protected String getPath(File id) {
        return id.getParentFile().getAbsolutePath().replaceFirst("^"+ Pattern.quote(prefix.getAbsolutePath()),"");
    }

    @Override
    protected String getIdOfAttribute(File attributeID) {
        return attributeID.getName();
    }

    @Override
    protected void reset() {
        super.reset();
        attributeIterator = attributes.iterator();
    }
}
