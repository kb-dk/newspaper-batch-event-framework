package dk.statsbiblioteket.autonomous.iterator.filesystem.transforming;

import dk.statsbiblioteket.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.autonomous.iterator.common.DelegatingTreeIterator;
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

public class TransformingIteratorForFileSystems extends CommonTransformingIterator {


    protected List<DelegatingTreeIterator> virtualChildren;


    /**
     * Create the transforming Iterator for file systems
     * @param id The root folder
     * @param groupingChar the grouping regular expression, ie. the char used as separator between prefix and postfix.
     *                     Should be "\\."
     * @param dataFilePattern a regular expression that should match the names of all datafiles
     */
    public TransformingIteratorForFileSystems(File id, String groupingChar, String dataFilePattern, String checksumPostfix) {
        super(id, dataFilePattern, checksumPostfix, groupingChar);
        virtualChildren = new ArrayList<>();
    }



    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>(children.length+virtualChildren.size());
        for (File child : children) {
            result.add(new TransformingIteratorForFileSystems(child, getGroupingChar(), getDataFilePattern(),getChecksumPostfix()));
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
        Map<String,List<File>> groupedByPrefix = groupByPrefix(attributes);

        if (groupedByPrefix.size() == 1){//only one group
            Collection<File> dataFiles = getDataFiles(attributes);
            for (File dataFile : dataFiles) {
                attributes.remove(dataFile);
                virtualChildren.add(new DatafileIterator(dataFile,getChecksumPostfix(),getGroupingChar()));
            }


        } else {
            Pair<String,List<File>> noDataGroup = getUniqueNoDataFilesGroup(groupedByPrefix);
            if (noDataGroup != null){
                attributes = noDataGroup.getRight();
            }
            for (String prefix : groupedByPrefix.keySet()) {
                if (noDataGroup != null && prefix.equals(noDataGroup.getLeft())){
                    continue;
                }
                List<File> group = groupedByPrefix.get(prefix);
                virtualChildren.add(new VirtualIteratorForFileSystems(id,prefix,getDataFilePattern(),group,getGroupingChar(),getChecksumPostfix()));
            }
        }

        return attributes.iterator();
    }


    /**
     * group a collection of files according to their prefix
     * @param files the files to group
     * @return a map of prefixes to lists of files
     * @see #getPrefix(java.io.File)
     */
    private Map<String,List<File>> groupByPrefix(Collection<File> files) {
        Map<String,List<File>> prefixToFile = new HashMap<>();
        for (File file : files) {
            String prefix = getPrefix(file);
            List<File> fileList = prefixToFile.get(prefix);
            if (fileList == null){
                fileList = new ArrayList<>();
            }
            fileList.add(file);
            prefixToFile.put(prefix,fileList);
        }
        return prefixToFile;
    }



}
