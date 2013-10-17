package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.AbstractIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.AbstractIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Common super class for the transforming iterators. */
public abstract class CommonTransformingIterator
        extends AbstractIterator<File> {
    private final String groupingChar;
    private final File batchFolder;
    private String dataFilePattern;
    private String checksumPostfix;


    protected CommonTransformingIterator(File id,
                                         File batchFolder,
                                         String dataFilePattern,
                                         String checksumPostfix,
                                         String groupingChar) {
        super(id);
        this.batchFolder = batchFolder;
        this.dataFilePattern = dataFilePattern;
        this.checksumPostfix = checksumPostfix;
        this.groupingChar = groupingChar;
    }

    /**
     * Get the files that are identified as attributes in a collection of files
     *
     * @param files the files to examine
     *
     * @return the data files
     */
    protected Collection<File> getDataFiles(Collection<File> files) {
        Collection<File> datafiles = new ArrayList<>();
        for (File attribute : files) {
            if (attribute.getName().matches(dataFilePattern)) {
                datafiles.add(attribute);
            }
        }
        return datafiles;
    }

    /**
     * Utility method, does the collection contain data files?
     *
     * @param files the files to examine
     *
     * @return true if a data file is found
     */
    protected boolean containsDatafiles(Collection<File> files) {
        return getDataFiles(files).size() > 0;
    }

    /**
     * Get the only group that contain no datafiles from a list grouping. If there is no unique group, return null
     *
     * @param groupedByPrefix the map of groups
     *
     * @return the only group without datafiles or null
     */
    protected Pair<String, List<File>> getUniqueNoDataFilesGroup(Map<String, List<File>> groupedByPrefix) {
        Pair<String, List<File>> uniqueGroup = null;
        for (Map.Entry<String, List<File>> group : groupedByPrefix.entrySet()) {
            if (!containsDatafiles(group.getValue())) {
                if (uniqueGroup == null) {
                    uniqueGroup = new Pair<>(group.getKey(), group.getValue());
                } else {
                    return null;
                }
            }
        }
        return uniqueGroup;
    }

    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID,
                                                       File attributeID) {
        return new FileAttributeParsingEvent(toPathID(attributeID), attributeID, checksumPostfix);
    }

    @Override
    protected String getIdOfNode() {
        return toPathID(id);
    }

    public String getChecksumPostfix() {
        return checksumPostfix;
    }

    public String getDataFilePattern() {
        return dataFilePattern;
    }

    /**
     * Get the batchFolder of a file
     *
     * @param file the file
     *
     * @return the batchFolder
     * @see #groupingChar
     */
    protected String getPrefix(File file) {
        return file.getName().split(groupingChar)[0];
    }

    public String getGroupingChar() {
        return groupingChar;
    }

    public File getBatchFolder() {
        return batchFolder;
    }

    public String toPathID(File id) {
        return id.getAbsolutePath().replaceFirst(Pattern.quote(getBatchFolder().getAbsolutePath()), "");
    }
}
