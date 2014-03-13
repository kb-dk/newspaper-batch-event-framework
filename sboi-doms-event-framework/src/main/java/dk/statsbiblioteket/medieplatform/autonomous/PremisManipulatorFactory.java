package dk.statsbiblioteket.medieplatform.autonomous;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * This is the factory for creaing new PremisManipulators. Instances of this factory is thread safe, but the created
 * PremisManipulators are not.
 */
public class PremisManipulatorFactory {

    public final static String TYPE = "Newspaper_digitisation_project";
    private final IDFormatter format;
    private final String type;
    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;


    /**
     * Create a new factory for premis manipulators.
     *
     * @param format the formatter to convert IDs
     * @param type   the type to use in premis
     */
    public PremisManipulatorFactory(IDFormatter format, String type) throws JAXBException {
        this.format = format;
        this.type = type;
        JAXBContext context = JAXBContext.newInstance(dk.statsbiblioteket.autonomous.premis.ObjectFactory.class);
        unmarshaller = context.createUnmarshaller();
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

    }

    /**
     * Create a new premisManipulator from an inputstream of premis. We assume that the premis have a Object.
     *
     * @param blob the blob to read from
     *
     * @return a premis manipulator
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator createFromBlob(InputStream blob) throws JAXBException {
        return new PremisManipulator(blob, format, type, marshaller, unmarshaller);
    }

    /**
     * Create a new premisManipulator from the id's of a batch. The premis will be initialised with an Object with
     * the correct identifier.
     *
     * @param batchID         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return a premis manipulator.
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator createInitialPremisBlob(String batchID, int roundTripNumber) throws JAXBException {
        return new PremisManipulator(batchID, roundTripNumber, format, type, marshaller);
    }

}
