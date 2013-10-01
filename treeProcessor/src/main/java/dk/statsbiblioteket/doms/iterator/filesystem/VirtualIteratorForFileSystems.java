package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.common.TreeIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class VirtualIteratorForFileSystems extends CommonTransformingIterator{
    protected String prefix;
    private List<File> group;
    private List<TreeIterator> virtualChildren;


    public VirtualIteratorForFileSystems(File id, String prefix, String dataFilePattern, List<File> group) {
        super(new File(id,prefix),dataFilePattern);
        this.prefix = prefix;
        this.group = group;
        virtualChildren = new ArrayList<>();
    }

    @Override
    protected Iterator<TreeIterator> initializeChildrenIterator() {
        return virtualChildren.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> datafiles = getDataFiles(group);
        for (File dataFile : datafiles) {
            group.remove(dataFile);
            virtualChildren.add(new DatafileIterator(dataFile));
        }
        return group.iterator();
    }

    @Override
    protected String getIdOfAttribute(File attributeID) {
        return attributeID.getName().replace(prefix,"").replaceAll("^\\.","");
    }


}
