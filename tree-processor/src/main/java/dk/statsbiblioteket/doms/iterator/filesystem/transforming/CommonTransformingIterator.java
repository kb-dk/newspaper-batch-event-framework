package dk.statsbiblioteket.doms.iterator.filesystem.transforming;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common super class for the transforming iterators.
 */
public abstract class CommonTransformingIterator extends AbstractIterator<File> {
    private String dataFilePattern;
    private String checksumPostfix;
    private final String groupingChar;


    protected CommonTransformingIterator(File id,
                                         String dataFilePattern,
                                         String checksumPostfix,
                                         String groupingChar) {
        super(id);
        this.dataFilePattern = dataFilePattern;
        this.checksumPostfix = checksumPostfix;
        this.groupingChar = groupingChar;
    }

    /**
     * Get the files that are identified as attributes in a collection of files
     * @param files the files to examine
     * @return the data files
     */
    protected Collection<File> getDataFiles(Collection<File> files) {
        Collection<File> datafiles = new ArrayList<>();
        for (File attribute : files) {
            if (attribute.getName().matches(dataFilePattern)){
                datafiles.add(attribute);
            }
        }
        return datafiles;
    }

    /**
     * Utility method, does the collection contain data files?
     * @param files the files to examine
     * @return true if a data file is found
     */
    protected boolean containsDatafiles(Collection<File> files) {
        return getDataFiles(files).size() > 0;
    }

    /**
     * Get the only group that contain no datafiles from a list grouping. If there is no unique group, return null
     * @param groupedByPrefix the map of groups
     * @return the only group without datafiles or null
     */
    protected Pair<String, List<File>> getUniqueNoDataFilesGroup(Map<String, List<File>> groupedByPrefix) {
        Pair<String, List<File>> uniqueGroup = null;
        for (Map.Entry<String, List<File>> group : groupedByPrefix.entrySet()) {
            if (!containsDatafiles(group.getValue())){
                if (uniqueGroup == null){
                    uniqueGroup = new Pair<>(group.getKey(),group.getValue());
                } else {
                    return null;
                }
            }
        }
        return uniqueGroup;
    }


    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent(attributeID.getName(), attributeID,checksumPostfix);
    }


    @Override
    protected String getIdOfNode() {
        return id.getName();
    }


    public String getChecksumPostfix() {
        return checksumPostfix;
    }

    public String getDataFilePattern() {
        return dataFilePattern;
    }

    /**
     * Get the prefix of a file
     * @param file the file
     * @return the prefix
     * @see #groupingChar
     */
    protected String getPrefix(File file) {
        return file.getName().split(groupingChar)[0];
    }

    public String getGroupingChar() {
        return groupingChar;
    }
}
