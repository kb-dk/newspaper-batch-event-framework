package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is the transforming iterator for filesystems. It allows one to iterate over a tree structure on the file system
 * but having it transformed inline to a format that is suitable to ingest into doms.
 *
 * The transformations are
 *
 * 1. All data files (ie. the ones matched by the dataFilePattern) will be made into special folders. The contents
 * of the datafile will reside in a virtual file called contents in that folder
 * 2. Prefix grouping. If a folder contains a number of files with a common prefix, these will be grouped into a
 * virtual
 * folder, named as the prefix. This only happens if there are more than one common prefix.
 * 2b. If only one of the groups contain no datafiles, this group will be cancelled, and the files will reside in the
 * real folder.
 *
 * There will be no virtual folders inside virtual folders.
 */
public class TransformingIteratorForFileSystems
        extends CommonTransformingIterator {


    protected List<DelegatingTreeIterator> virtualChildren;


    /**
     * Create the transforming Iterator for file systems
     *
     * @param id              The root folder
     * @param groupingPattern the grouping regular expression, ie. the char used as separator between prefix and
     *                        postfix.
     *                        Should be "\\."
     * @param dataFilePattern a regular expression that should match the names of all datafiles
     * @param checksumPostfix this is the postfix for the checksum files. Note, THIS IS NOT A PATTERN
     */
    public TransformingIteratorForFileSystems(File id,
                                              String groupingPattern,
                                              String dataFilePattern,
                                              String checksumPostfix) {
        this(id, id.getParentFile(), groupingPattern, dataFilePattern, checksumPostfix);
    }

    /**
     * Create the transforming Iterator for file systems
     *
     * @param id              The root folder
     * @param groupingChar    the grouping regular expression, ie. the char used as separator between prefix and
     *                        postfix.
     *                        Should be "\\."
     * @param dataFilePattern a regular expression that should match the names of all datafiles
     */
    protected TransformingIteratorForFileSystems(File id,
                                                 File prefix,
                                                 String groupingChar,
                                                 String dataFilePattern,
                                                 String checksumPostfix) {
        super(id, prefix, dataFilePattern, checksumPostfix, groupingChar);
        virtualChildren = new ArrayList<>();
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length + virtualChildren.size());
        for (File child : children) {
            result.add(new TransformingIteratorForFileSystems(child,
                                                              getBatchFolder(),
                                                              getGroupingChar(),
                                                              getDataFilePattern(),
                                                              getChecksumPostfix()));
        }
        for (DelegatingTreeIterator virtualChild : virtualChildren) {
            result.add(virtualChild);
        }
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> attributes = FileUtils.listFiles(id, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && !file.getName().endsWith(getChecksumPostfix());
            }
        }, null);


        //these are the attributes.
        Map<String, List<File>> groupedByPrefix = groupByPrefix(attributes);

        if (groupedByPrefix.size() == 1) {//only one group
            Collection<File> dataFiles = getDataFiles(attributes);
            for (File dataFile : dataFiles) {
                attributes.remove(dataFile);
                virtualChildren
                        .add(new DatafileIterator(dataFile, getBatchFolder(), getChecksumPostfix(), getGroupingChar()));
            }


         } else if (largestGroup(groupedByPrefix).size() <= 1) {//all groups are of size one, no common prefix
            //Make no virtual subfolders, just keep everything as it is
        } else {
            Pair<String, List<File>> noDataGroup = getUniqueNoDataFilesGroup(groupedByPrefix);
            if (noDataGroup != null) {
                attributes = noDataGroup.getRight();
            }
            for (String prefix : groupedByPrefix.keySet()) {
                if (noDataGroup != null && prefix.equals(noDataGroup.getLeft())) {
                    continue;
                }
                List<File> group = groupedByPrefix.get(prefix);

                virtualChildren.add(new VirtualIteratorForFileSystems(id,
                                                                      prefix,
                                                                      getBatchFolder(),
                                                                      getDataFilePattern(),
                                                                      group,
                                                                      getGroupingChar(),
                                                                      getChecksumPostfix()));
                attributes.removeAll(group);
            }
        }

        return attributes.iterator();
    }

    private List<File> largestGroup(Map<String, List<File>> groupedByPrefix) {
        List<File> largest = null;
        for (List<File> files : groupedByPrefix.values()) {
            if (largest == null || files.size() > largest.size()){
                largest = files;
            }
        }
        return largest;
    }

    /**
     * group a collection of files according to their prefix
     *
     * @param files the files to group
     *
     * @return a map of prefixes to lists of files
     * @see #getPrefix(java.io.File)
     */
    private Map<String, List<File>> groupByPrefix(Collection<File> files) {
        Map<String, List<File>> prefixToFile = new HashMap<>();
        for (File file : files) {
            String prefix = getPrefix(file);
            List<File> fileList = prefixToFile.get(prefix);
            if (fileList == null) {
                fileList = new ArrayList<>();
            }
            fileList.add(file);
            prefixToFile.put(prefix, fileList);
        }
        return prefixToFile;
    }


}
