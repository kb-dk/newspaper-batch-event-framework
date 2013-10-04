package dk.statsbiblioteket.doms.iterator.filesystem.transforming;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.FileAttributeParsingEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class VirtualIteratorForFileSystems extends CommonTransformingIterator {
    protected String prefix;
    private List<File> group;
    private List<DelegatingTreeIterator> virtualChildren;


    public VirtualIteratorForFileSystems(File id,
                                         String prefix,
                                         String dataFilePattern,
                                         List<File> group,
                                         String groupingChar,
                                         String checksumPostfix) {
        super(new File(id,prefix),dataFilePattern,checksumPostfix,groupingChar);
        this.prefix = prefix;
        this.group = group;
        virtualChildren = new ArrayList<>();
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        return virtualChildren.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> datafiles = getDataFiles(group);
        for (File dataFile : datafiles) {
            group.remove(dataFile);
            virtualChildren.add(new DatafileIterator(dataFile,getChecksumPostfix(),getGroupingChar()));
        }
        return group.iterator();
    }


    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent(getIdOfAttribute(attributeID), attributeID,getChecksumPostfix());
    }


    protected String getIdOfAttribute(File attributeID) {
        //cut the prefix from the name, including the grouping char
        return attributeID.getName().replace(prefix,"").replaceFirst(getGroupingChar(), "");
    }


}
