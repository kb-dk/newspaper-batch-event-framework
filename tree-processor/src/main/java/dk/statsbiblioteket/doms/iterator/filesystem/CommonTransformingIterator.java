package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class CommonTransformingIterator extends AbstractIterator<File> {
    private String dataFilePattern;

    protected CommonTransformingIterator(File id, String dataFilePattern) {
        super(id);
        this.dataFilePattern = dataFilePattern;
    }

    protected Collection<File> getDataFiles(Collection<File> attributes) {
        Collection<File> datafiles = new ArrayList<>();
        for (File attribute : attributes) {
            if (attribute.getName().matches(dataFilePattern)){
                datafiles.add(attribute);
            }
        }
        return datafiles;
    }

    protected boolean containsDatafiles(Collection<File> attributes) {
        return getDataFiles(attributes).size() > 0;
    }

    protected Map.Entry<String, List<File>> getUniqueNoDataFilesGroup(Map<String, List<File>> groupedByPrefix) {
        Map.Entry<String, List<File>> uniqueGroup = null;
        for (Map.Entry<String, List<File>> group : groupedByPrefix.entrySet()) {
            if (!containsDatafiles(group.getValue())){
                if (uniqueGroup == null){
                    uniqueGroup = group;
                } else {
                    return null;
                }
            }
        }
        return uniqueGroup;
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
