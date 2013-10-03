package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TransformingIteratorForFileSystems extends CommonTransformingIterator {

    protected final String groupingChar;
    private String dataFilePattern;

    protected List<TreeIterator> virtualChildren;



    public TransformingIteratorForFileSystems(File id, String groupingChar, String dataFilePattern) {
        super(id, dataFilePattern);
        this.groupingChar = groupingChar;
        this.dataFilePattern = dataFilePattern;
        virtualChildren = new ArrayList<>();
    }



    @Override
    protected Iterator<TreeIterator> initializeChildrenIterator() {
        File[] children = id.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        ArrayList<TreeIterator> result = new ArrayList<>(children.length+virtualChildren.size());
        for (File child : children) {
            result.add(makeDelegate(id,child));
        }
        for (TreeIterator virtualChild : virtualChildren) {
            result.add(virtualChild);
        }
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        Collection<File> attributes = FileUtils.listFiles(id, FileFileFilter.FILE, null);
        //these are the attributes.
        Map<String,List<File>> groupedByPrefix = groupByPrefix(attributes);

        if (groupedByPrefix.size() == 1){//only one group
            Collection<File> dataFiles = getDataFiles(attributes);
            for (File dataFile : dataFiles) {
                attributes.remove(dataFile);
                virtualChildren.add(new DatafileIterator(dataFile));
            }


        } else {
            Map.Entry<String,List<File>> noDataGroup = getUniqueNoDataFilesGroup(groupedByPrefix);
            if (noDataGroup != null){
                attributes = noDataGroup.getValue();
            }
            for (String prefix : groupedByPrefix.keySet()) {
                if (noDataGroup != null && prefix.equals(noDataGroup.getKey())){
                    continue;
                }
                List<File> group = groupedByPrefix.get(prefix);
                virtualChildren.add(new VirtualIteratorForFileSystems(id,prefix,dataFilePattern,group));
            }
        }

        return attributes.iterator();
    }




    private Map<String,List<File>> groupByPrefix(Collection<File> attributes) {
        Map<String,List<File>> prefixToFile = new HashMap<>();
        for (File attribute : attributes) {
            String prefix = getPrefix(attribute);
            List<File> fileList = prefixToFile.get(prefix);
            if (fileList == null){
                fileList = new ArrayList<>();
            }
            fileList.add(attribute);
            prefixToFile.put(prefix,fileList);
        }
        return prefixToFile;
    }

    private String getPrefix(File attribute) {
        return attribute.getName().split(groupingChar)[0];
    }

    protected TreeIterator makeDelegate(File id, File childID) {
        return new TransformingIteratorForFileSystems(childID, groupingChar,dataFilePattern);
    }

    @Override
    protected AttributeEvent makeAttributeEvent(File id, File attributeID) {
        return new FileAttributeEvent(getIdOfAttribute(attributeID), attributeID);
    }

}
