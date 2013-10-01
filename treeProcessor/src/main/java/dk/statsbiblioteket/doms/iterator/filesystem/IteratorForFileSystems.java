package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class IteratorForFileSystems extends AbstractIterator<File> {


    private final File prefix;

    public IteratorForFileSystems(File dir, final File prefix) {
        super(dir);
        this.prefix = prefix;
    }


    @Override
    protected Iterator<TreeIterator> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<TreeIterator> result = new ArrayList<>(children.length);
        for (File child : children) {
            result.add(makeDelegate(id,child));
        }
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> attributes = FileUtils.listFiles(id, FileFileFilter.FILE, null);
        return attributes.iterator();
    }

    private AbstractIterator makeDelegate(File id, File childID) {
        return new IteratorForFileSystems(childID,prefix);
    }

    @Override
    protected AttributeEvent makeAttributeEvent(File id, File attributeID) {
        return new FileAttributeEvent(getIdOfAttribute(attributeID), attributeID);
    }

    @Override
    protected String getIdOfNode() {
        return id.getName();
    }

    @Override
    protected String getIdOfAttribute(File attributeID) {
        return attributeID.getName();
    }


}
