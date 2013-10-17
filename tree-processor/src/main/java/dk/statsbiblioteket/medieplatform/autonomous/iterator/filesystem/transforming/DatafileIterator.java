package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DelegatingTreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * This class represents the virtual folder that will be created for all datafiles. Each datafile will be created
 * as a folder containing one file, named content, which represents the content of the datafile
 */
public class DatafileIterator extends CommonTransformingIterator {


    public DatafileIterator(File dataFile,
                            File batchFolder,
                            String checksumPostfix,
                            String groupingChar) {
        super(dataFile,batchFolder,null,checksumPostfix, groupingChar);
    }

    @Override
    protected Iterator<DelegatingTreeIterator> initializeChildrenIterator() {
        //returns empty iterator, datafiles will have no children
        return Collections.emptyIterator();
    }

    @Override
    protected Iterator<File> initilizeAttributeIterator() {
        return Arrays.asList(id).iterator();
    }

    @Override
    protected AttributeParsingEvent makeAttributeEvent(File nodeID, File attributeID) {
        return new FileAttributeParsingEvent(toPathID(new File(attributeID,"contents")),attributeID,getChecksumPostfix());

    }


}
