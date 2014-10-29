package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.util.Pair;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class TransformingIteratorForFileSystems extends CommonTransformingIterator {


    private final List<String> ignoredFiles;
    protected List<DelegatingTreeIterator> virtualChildren;


    /**
     * Create the transforming Iterator for file systems
     *
     * @param id              The root folder
     * @param groupingPattern The grouping regular expression, ie. the char used as separator between prefix and
     *                        postfix.
     *                        Should be "\\."
     * @param dataFilePattern a regular expression that should match the names of all datafiles
     * @param checksumPostfix this is the postfix for the checksum files. Note, THIS IS NOT A PATTERN
     * @param ignoredFiles    Files to ignore during transformation.
     */
    public TransformingIteratorForFileSystems(File id, String groupingPattern, String dataFilePattern,
                                              String checksumPostfix, List<String> ignoredFiles) {
        this(id, id.getParentFile(), groupingPattern, dataFilePattern, checksumPostfix, ignoredFiles);
    }

    /**
     * Create the transforming Iterator for file systems
     *
     * @param id              The root folder
     * @param prefix          The prefix folder
     * @param groupingPattern The grouping regular expression, ie. the char used as separator between prefix and
     *                        postfix.
     *                        Should be "\\."
     * @param dataFilePattern a regular expression that should match the names of all datafiles
     * @param checksumPostfix this is the postfix for the checksum files. Note, THIS IS NOT A PATTERN
     * @param ignoredFiles    Files to ignore during transformation.
     */
    protected TransformingIteratorForFileSystems(File id, File prefix, String groupingPattern, String dataFilePattern,
                                                 String checksumPostfix, List<String> ignoredFiles) {
        super(id, prefix, dataFilePattern, checksumPostfix, groupingPattern);
        virtualChildren = new ArrayList<>();
        this.ignoredFiles = ignoredFiles;
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length + virtualChildren.size());
        for (File child : children) {
            result.add(
                    new TransformingIteratorForFileSystems(
                            child, getBatchFolder(), getGroupingChar(), getDataFilePattern(), getChecksumPostfix(),
                            Arrays.asList("transfer_complete", "transfer_acknowledged")));
        }
        for (DelegatingTreeIterator virtualChild : virtualChildren) {
            result.add(virtualChild);
        }
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() throws IOException {
        if (!(id.isDirectory() && id.canRead())) {
            throw new IOException("Failed to read directory '" + id.getAbsolutePath() + "'");
        }
        Collection<File> attributes = FileUtils.listFiles(
                id, new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                boolean isFile = file.isFile();
                boolean isNotChecksum = !file.getName().endsWith(getChecksumPostfix());
                boolean isNotIgnored = !ignoredFiles.contains(file.getName());
                return isFile && isNotChecksum && isNotIgnored;
            }
        }, null);

        //If there is any datafiles, we group by prefix. If there are no datafiles, we expect the structure to be flat
        if (containsDatafiles(attributes)) {
            Map<String, List<File>> groupedByPrefix = groupByPrefix(attributes);
            Pair<String, List<File>> noDataGroup = getShortestNoDataFilesGroup(groupedByPrefix);
            if (noDataGroup != null) {
                attributes = noDataGroup.getRight();
            }
            for (String prefix : groupedByPrefix.keySet()) {
                if (noDataGroup != null && prefix.equals(noDataGroup.getLeft())) {
                    continue;
                }
                List<File> group = groupedByPrefix.get(prefix);

                virtualChildren.add(
                        new VirtualIteratorForFileSystems(
                                id,
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
