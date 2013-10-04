package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a the Parsing Event of encountering a File
 */

public class FileAttributeParsingEvent extends AttributeParsingEvent {


    private File file;
    private File checksumFile;

    public FileAttributeParsingEvent(String localname,
                                     File file
                                     ) {
        super(localname);
        this.file = file;
        this.checksumFile = null;
    }

    public FileAttributeParsingEvent(String localname,
                                     File file,
                                     String checksumPostfix) {
        super(localname);
        this.file = file;
        this.checksumFile = new File(file.getAbsolutePath()+checksumPostfix);
    }


    @Override
    public InputStream getText()
            throws
            IOException {
        return new FileInputStream(file);
    }

    @Override
    public String getChecksum()
            throws
            IOException {
        if (checksumFile != null && checksumFile.canRead()){
            try (BufferedReader reader =  new BufferedReader(new FileReader(checksumFile))){
                String firstLine = reader.readLine();
                if (firstLine == null){
                    return null;
                }
                return firstWord(firstLine);
            }
        }
        return null;
    }

    private String firstWord(String firstLine) {
        firstLine = firstLine.trim();
        String[] splits = firstLine.split("\\s",2);
        return splits[0];
    }


}
