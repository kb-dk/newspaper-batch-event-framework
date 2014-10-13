package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Access to DOMS batch and event storage using the Central Webservice library to communicate with DOMS.
 * Implements the {@link dk.statsbiblioteket.medieplatform.autonomous.EventStorer} interface.
 */
public class NewspaperDomsEventStorage extends DomsEventStorage<Batch> {

    private static Logger log = LoggerFactory.getLogger(NewspaperDomsEventStorage.class);

    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;


    private final String createBatchRoundTripComment = "Creating batch round trip";
    private final String addEventToBatchComment = "Adding event to natch round trip";

    public NewspaperDomsEventStorage(EnhancedFedora fedora, String type, String batchTemplate, String roundTripTemplate,
                                     String hasPart_relation, String eventsDatastream, ItemFactory<Batch> itemFactory) throws JAXBException {
        super(fedora, type, eventsDatastream,
                     itemFactory);
        this.batchTemplate = batchTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
    }

    @Override
    public Date addEventToItem(Batch item, String agent, Date timestamp, String details, String eventType,
                               boolean outcome) throws CommunicationException {
        String itemID = item.getDomsID();
        if (itemID == null) {
            itemID = createBatchRoundTrip(item.getFullID());
            item.setDomsID(itemID);
        } else {
            throw new IllegalArgumentException("Trying to add an event to a non-existing item '" + item.toString() + "'");
        }
        return super.addEventToItem(item, agent, timestamp, details, eventType, outcome);
    }

    /**
     * Create a batch and round trip object, without adding any events
     *
     * @param fullItemID the full item id
     *
     * @return the pid of the doms object corresponding to the round trip
     * @throws dk.statsbiblioteket.medieplatform.autonomous.CommunicationException if communication with doms failed
     */
    public String createBatchRoundTrip(String fullItemID) throws CommunicationException {
        try {
            try {
                //find the roundTrip Object
                return getPidFromDCIdentifier(fullItemID);
            } catch (BackendInvalidResourceException e) {
                //no roundTripObject, so sad
                //but alas, we can continue
            }

            //find the batch object
            String batchObject;
            Batch.BatchRoundtripID fullIDSplits = new Batch.BatchRoundtripID(fullItemID);
            List<String> founds = fedora.findObjectFromDCIdentifier(fullIDSplits.batchDCIdentifier());
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                batchObject = fedora.cloneTemplate(
                        batchTemplate, Arrays.asList(fullIDSplits.batchDCIdentifier()), createBatchRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(fullIDSplits.roundTripDCIdentifier()), createBatchRoundTripComment);

            //connect batch object to round trip object
            fedora.addRelation(
                    batchObject,
                    toFedoraID(batchObject),
                    hasPart_relation,
                    toFedoraID(roundTripObject),
                    false,
                    createBatchRoundTripComment);

            //create the initial EVENTS datastream

            String premisBlob = premisFactory.createInitialPremisBlob(fullItemID).toXML();
            fedora.modifyDatastreamByValue(
                    roundTripObject, eventsDatastream, null,null,premisBlob.getBytes(), null, "text/xml",createBatchRoundTripComment,null);


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException | JAXBException e) {
            throw new CommunicationException(e);
        }


    }


    /**
     * Returns all Batch roundtrip objects for a given batchId, sorted in ascending order.
     * Returns null if the batchId is not known.
     * @param batchId the batchId.
     * @return the sorted list of roundtrip objects.
     */
    public List<Batch> getAllRoundTrips(String batchId) throws CommunicationException {
        Comparator<Batch> roundtripComparator = new Comparator<Batch>() {
            @Override
            public int compare(Batch o1, Batch o2) {
                return o1.getRoundTripNumber().compareTo(o2.getRoundTripNumber());
            }
        };
        try {
            List<String> founds = fedora.findObjectFromDCIdentifier(new Batch.BatchRoundtripID(batchId,0).batchDCIdentifier());
            if (founds == null || founds.size() == 0) {
                return null;
            }
            String batchObjectPid = founds.get(0);
            List<FedoraRelation> roundtripRelations = fedora.getNamedRelations(batchObjectPid, hasPart_relation, null);
            List<Batch> roundtrips = new ArrayList<>();
            for (FedoraRelation roundtripRelation: roundtripRelations) {
                try {
                    final Batch itemFromDomsID = getItemFromDomsID(roundtripRelation.getObject());
                    if (itemFromDomsID != null) {
                        roundtrips.add(itemFromDomsID);
                    }
                } catch (NotFoundException ignored) {

                }
            }
            Collections.sort(roundtrips, roundtripComparator);
            return roundtrips;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | BackendInvalidResourceException e) {
            throw new CommunicationException(e);
        }
    }





}
