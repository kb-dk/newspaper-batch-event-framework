package dk.statsbiblioteket.newspaper.batcheventFramework;

import javax.xml.bind.JAXBException;
import java.io.InputStream;

public class PremisManipulatorFactory{

    public final static String TYPE="Newspaper_digitisation_project";

    private IDFormatter format;

    private String type;

    /**
     * Create a new factory for premis manipulators.
     * @param format the formatter to convert IDs
     * @param type the type to use in premis
     */
    public PremisManipulatorFactory(IDFormatter format, String type) {
        this.format = format;
        this.type = type;
    }

    /**
     * Create a new premisManipulator from an inputstream of premis. We assume that the premis have a Object.
     * @param blob the blob to read from
     * @return a premis manipulator
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator createFromBlob(InputStream blob) throws JAXBException {
        return new PremisManipulator(blob,format,type);
    }




    /**
     * Create a new premisManipulator from the id's of a batch. The premis will be initialised with an Object with
     * the correct identifier.
     * @param batchID the batch id
     * @param roundTripNumber the round trip number
     * @return a premis manipulator.
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator createInitialPremisBlob(Long batchID, int roundTripNumber) throws JAXBException {
        return new PremisManipulator(batchID,roundTripNumber,format,type);
    }

}
