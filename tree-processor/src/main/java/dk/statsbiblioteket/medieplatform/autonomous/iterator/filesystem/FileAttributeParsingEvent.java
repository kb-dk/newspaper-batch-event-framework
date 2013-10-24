package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a the Parsing Event of encountering a File
 */

public class FileAttributeParsingEvent extends AttributeParsingEvent {


    private File file;
    private File checksumFile;

    public FileAttributeParsingEvent(String name,
                                     File file
                                     ) {
        super(name);
        this.file = file;
        this.checksumFile = null;
    }

    public FileAttributeParsingEvent(String name,
                                     File file,
                                     String checksumPostfix) {
        super(name);
        this.file = file;
        this.checksumFile = new File(file.getAbsolutePath()+checksumPostfix);
    }


    @Override
    public InputStream getData()
            throws
            IOException {
        return new FileInputStream(file);
    }

    @Override
    public String getChecksum()
            throws
            IOException {
        if (checksumFile != null){
            try (BufferedReader reader =  new BufferedReader(new FileReader(checksumFile))){
                String firstLine = reader.readLine();
                if (firstLine == null){
                    return "";
                }
                return firstWord(firstLine).trim().toLowerCase();
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    private String firstWord(String firstLine) {
        firstLine = firstLine.trim();
        String[] splits = firstLine.split("\\s",2);
        if (splits.length == 0){
            return "";
        }
        return splits[0];
    }


}
