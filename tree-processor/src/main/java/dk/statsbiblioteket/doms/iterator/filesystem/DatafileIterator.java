package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class DatafileIterator extends AbstractIterator<File> {


    public DatafileIterator(File dataFile) {
        super(dataFile);
    }

    @Override
    protected Iterator<TreeIterator> initializeChildrenIterator() {
        ArrayList<TreeIterator> result = new ArrayList<TreeIterator>();
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        return Arrays.asList(id).iterator();
    }

    @Override
    protected AttributeEvent makeAttributeEvent(File id, File attributeID) {
        return new FileAttributeEvent("contents",attributeID);
    }

    @Override
    protected String getIdOfNode() {
        return id.getName().replaceAll("^[^.]*\\.","");
    }

    @Override
    protected String getIdOfAttribute(File attributeID) {
        return attributeID.getName();
    }

}
