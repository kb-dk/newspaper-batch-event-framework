package dk.statsbiblioteket.doms.iterator.filesystem.transforming;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.FileAttributeParsingEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class represents the virtual folder that will be created for all datafiles. Each datafile will be created
 * as a folder containing one file, named content, which represents the content of the datafile
 */
public class DatafileIterator extends CommonTransformingIterator {


    public DatafileIterator(File dataFile) {
        super(dataFile,null);
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        //returns empty iterator, datafiles will have no children
        ArrayList<DelegatingTreeIterator> result = new ArrayList<>();
        return result.iterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        return Arrays.asList(id).iterator();
    }

    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent("contents",attributeID);
    }


}
