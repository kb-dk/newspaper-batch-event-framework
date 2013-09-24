package dk.statsbiblioteket.newspaper.batcheventFramework;

import javax.xml.bind.JAXBException;
import java.io.InputStream;

public class PremisManipulatorFactory <T> {

    public final static String TYPE="Newspaper_digitisation_project";

    private IDFormatter<T> format;

    private String type;

    public PremisManipulatorFactory() {
        this.format = format;
        this.type = TYPE;
    }


    public PremisManipulatorFactory(IDFormatter<T> format, String type) {
        this.format = format;
        this.type = type;
    }

    public PremisManipulator<T> createFromBlob(InputStream blob) throws JAXBException {
        PremisManipulator that = new PremisManipulator(blob,format,type);
        return that;
    }


    //Here we assume that an object exists
    public PremisManipulator<T> createInitialPremisBlob(T BatchID, int runNr) throws JAXBException {
        PremisManipulator that = new PremisManipulator(BatchID,runNr,format,type);
        return that;
    }

}
