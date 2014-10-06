package dk.statsbiblioteket.medieplatform.autonomous;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;

/**
 * This is the factory for creaing new PremisManipulators. Instances of this factory is thread safe, but the created
 * PremisManipulators are not.
 */
public class PremisManipulatorFactory<T extends Item> {

    public final static String TYPE = "Newspaper_digitisation_project";
    private final String type;
    private final ItemFactory<T> itemFactory;
    private final JAXBContext context;


    /**
     * Create a new factory for premis manipulators.
     *
     * @param type   the type to use in premis
     */
    public PremisManipulatorFactory(String type, ItemFactory<T> itemFactory) throws JAXBException {
        this.type = type;
        this.itemFactory = itemFactory;
        context = JAXBContext.newInstance(dk.statsbiblioteket.autonomous.premis.ObjectFactory.class);
    }


    /**
     * Create a new premisManipulator from an inputstream of premis. We assume that the premis have a Object.
     *
     * @param blob the blob to read from
     *
     * @return a premis manipulator
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator<T> createFromBlob(InputStream blob) throws JAXBException {
        return new PremisManipulator<>(blob, type, context, itemFactory);
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
    public PremisManipulator<T> createInitialPremisBlob(String batchID, int roundTripNumber) throws JAXBException {
        return new PremisManipulator<>(new Batch(batchID,roundTripNumber).getFullID(), type, context, itemFactory);
    }

    /**
     * Create a new premisManipulator from the id's of a batch. The premis will be initialised with an Object with
     * the correct identifier.
     *
     * @param itemId         the item id
     *
     * @return a premis manipulator.
     * @throws JAXBException if the parsing failed
     */
    public PremisManipulator<T> createInitialPremisBlob(String itemId) throws JAXBException {
        return new PremisManipulator<>(itemId, type, context, itemFactory);
    }
}
