package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.DelegatingTreeIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Iterator for parsing a tree structure backed by a file system. Each iterator represents a node. A node corresponds
 * to a directory.
 */
public class IteratorForFileSystems extends AbstractIterator<File> {


    /**
     * Construct an iterator rooted at a given directory
     * @param dir the directory at which to root the iterator.
     */
    public IteratorForFileSystems(File dir) {
        super(dir);
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        //The id attribute is the id of this node, ie. the File corresponding to the directory
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length);
        for (File child : children) {
            result.add(new IteratorForFileSystems(child));
        }
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> attributes = FileUtils.listFiles(id, FileFileFilter.FILE, null);
        return attributes.iterator();
    }

    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent(attributeID.getName(), attributeID);
    }

    /**
     * The name of the directory is used as the Id of the node.
     * @return the name of the directory.
     */
    @Override
    protected String getIdOfNode() {
        return id.getName();
    }


}
