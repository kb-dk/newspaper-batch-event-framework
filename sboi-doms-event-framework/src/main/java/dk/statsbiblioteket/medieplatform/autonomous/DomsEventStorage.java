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
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Access to DOMS batch and event storage using the Central Webservice library to communicate with DOMS.
 * Implements the {@link EventStorer} interface.
 */
public class DomsEventStorage<T extends Item> implements EventStorer<T> {

    private static Logger log = LoggerFactory.getLogger(DomsEventStorage.class);

    private final EnhancedFedora fedora;
    private final NewspaperIDFormatter idFormatter;
    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory<T> premisFactory;
    private final String createBatchRoundTripComment = "Creating batch round trip";
    private final String addEventToBatchComment = "Adding event to natch round trip";

    DomsEventStorage(EnhancedFedora fedora, NewspaperIDFormatter idFormatter, String type, String batchTemplate,
                     String roundTripTemplate, String hasPart_relation, String eventsDatastream, ItemFactory<T> itemFactory) throws JAXBException {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        this.batchTemplate = batchTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory(type, itemFactory);
    }



    @Override
    public Date addEventToItem(T item, String agent, Date timestamp, String details, String eventType,
                               boolean outcome) throws CommunicationException {
        try {
            String itemID = item.getDomsID();
            if (itemID == null && item instanceof Batch){
                itemID = createBatchRoundTrip(item.getFullID());
            } else {
                throw new IllegalArgumentException("Trying to add an event to a non-existing item '" +item.toString()+"'");
            }
            PremisManipulator premisObject;
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(itemID, eventsDatastream, null);

                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = premisFactory.createInitialPremisBlob(itemID);
            }
            premisObject = premisObject.addEvent(agent, timestamp, details, eventType, outcome);
            try {
                return fedora.modifyDatastreamByValue(itemID,
                        eventsDatastream,
                        null,
                        null,
                        premisObject.toXML().getBytes(),
                        null,
                        "text/xml",
                        addEventToBatchComment,
                        null);
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Create a batch and round trip object, without adding any events
     *
     * @param fullItemID the full item id
     *
     * @return the pid of the doms object corresponding to the round trip
     * @throws CommunicationException if communication with doms failed
     */
    public String createBatchRoundTrip(String fullItemID) throws CommunicationException {
        try {
            try {
                //find the roundTrip Object
                return getRoundTripID(fullItemID);
            } catch (BackendInvalidResourceException e) {
                //no roundTripObject, so sad
                //but alas, we can continue
            }

            //find the batch object
            String batchObject;
            NewspaperIDFormatter.SplitID fullIDSplits = idFormatter.unformatFullID(fullItemID);
            List<String> founds = fedora.findObjectFromDCIdentifier(idFormatter.formatBatchID(fullIDSplits.getBatchID()));
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                batchObject = fedora.cloneTemplate(
                        batchTemplate, Arrays.asList(idFormatter.formatBatchID(fullIDSplits.getBatchID())), createBatchRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(idFormatter.formatFullID(fullItemID)), createBatchRoundTripComment);

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

    public T getItemFromFullID(String itemFullID) throws CommunicationException, NotFoundException {
        String roundTripID;
        try {
            roundTripID = getRoundTripID(itemFullID);
            return getItemFromDomsID(roundTripID);
        } catch (BackendInvalidResourceException e) {
            throw new NotFoundException(e);
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
            List<String> founds = fedora.findObjectFromDCIdentifier(idFormatter.formatBatchID(batchId));
            if (founds == null || founds.size() == 0) {
                return null;
            }
            String batchObjectPid = founds.get(0);
            List<FedoraRelation> roundtripRelations = fedora.getNamedRelations(batchObjectPid, hasPart_relation, null);
            List<Batch> roundtrips = new ArrayList<>();
            for (FedoraRelation roundtripRelation: roundtripRelations) {
                try {
                    final T itemFromDomsID = getItemFromDomsID(roundtripRelation.getObject());
                    if (itemFromDomsID instanceof Batch) {
                        Batch fromDomsID = (Batch) itemFromDomsID;
                        roundtrips.add(fromDomsID);
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

    /**
     * Retrieve a batch
     *
     * @param domsId the id of the round trip object in doms
     *
     * @return the batch
     * @throws NotFoundException      if the batch is not found
     * @throws CommunicationException if communication with doms failed
     */
    public T getItemFromDomsID(String domsId) throws CommunicationException, NotFoundException {
        try {
            String premisPreBlob = fedora.getXMLDatastreamContents(domsId, eventsDatastream, null);
            PremisManipulator<T> premisObject
                    = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            T item = premisObject.toItem();
            item.setDomsID(domsId);
            return item;
        } catch ( BackendMethodFailedException | JAXBException |
                BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        } catch (BackendInvalidResourceException e){
            throw new NotFoundException(e);
        }


    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(Item item, int maxAttempts,
                                                      long waitTime, String eventId) throws
                                                                                     CommunicationException,
                                                                                     NotFoundException {
        int attempts = 0;
        int eventsRemoved = -1;
        while ((eventsRemoved = attemptWorkflowRestart(item, eventId)) < 0) {
            attempts++;
            if (attempts == maxAttempts) {
                String msg = "Failed to trigger restart of item " + item.getFullID()+
                             " after " + maxAttempts + " attempts. Giving up.";
                log.error(msg);
                throw new CommunicationException(msg);
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                //no problem
            }
        }
        return eventsRemoved;
    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(Item item, int maxTries,
                                                      long waitTime) throws CommunicationException, NotFoundException {
        return triggerWorkflowRestartFromFirstFailure(item, maxTries, waitTime, null);
    }

    /**
     * This method carries out a single attempt to restart the workflow from where it first failed.
     *
     * @param eventId         the first event to remove or null if all events after the first failure are to be
     *                        removed.
     *
     * @return the number of events removed or -1 of there was a ConcurrentModificationException thrown.
     * @throws CommunicationException if there was a problem communicating with DOMS.
     */
    private int attemptWorkflowRestart(Item item, String eventId) throws
                                                                                            CommunicationException,
                                                                                            NotFoundException {
        String itemPid = item.getDomsID();
        if (itemPid == null) {
            try {
                itemPid = getRoundTripID(item.getFullID());
            } catch (BackendInvalidResourceException e) {
                throw new NotFoundException("Could not find DOMS object for " + item.getFullID(),
                                                   e);
            }
        }
        try {
            Date lastModifiedDate = fedora.getObjectProfile(itemPid, null).getObjectLastModifiedDate();
            String premisPreBlob = fedora.getXMLDatastreamContents(itemPid, eventsDatastream, null);
            PremisManipulator premisObject
                    = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            int eventsRemoved = premisObject.removeEventsFromFailureOrEvent(eventId);
            if (eventsRemoved > 0) {
                //backupEventsForBatch(batchId, roundTripNumber);
                try {
                    fedora.modifyDatastreamByValue(
                            itemPid,
                            eventsDatastream,
                            null,
                            null,
                            premisObject.toXML().getBytes("UTF-8"),
                            null,
                            "text/xml",
                            "Event list trimmed of all events after earliest failure",
                            lastModifiedDate.getTime());
                } catch (ConcurrentModificationException e) {
                    log.warn(
                            "Failed to trigger restart of batch round trip for " + item.getFullID() +
                            " on this attempt. Another process modified the object concurrently."
                            );
                    return -1;
                } catch (UnsupportedEncodingException e) {
                    throw new Error("UTF-8 not supported.", e);
                }
            }
            return eventsRemoved;
        } catch (BackendInvalidResourceException | JAXBException | BackendInvalidCredsException | BackendMethodFailedException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Retrieve the corresponding doms pid of the round trip object
     *
     * @return the doms round trip pid
     * @throws CommunicationException          failed to communicate
     * @throws BackendInvalidResourceException object not found
     */
    String getRoundTripID(String roundTripID) throws
                                                               CommunicationException,
                                                               BackendInvalidResourceException {

        try {
            //find the Round Trip object
            final String dcIdentifier = idFormatter.formatFullID(roundTripID);
            List<String> founds = fedora.findObjectFromDCIdentifier(dcIdentifier);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            throw new BackendInvalidResourceException("Round Trip object not found for dc identifier " + dcIdentifier);
        } catch (BackendMethodFailedException | BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Append "info:fedora/" to the fedora pid if needed
     *
     * @param fedoraPid the fedora pid
     *
     * @return duh
     */
    private String toFedoraID(String fedoraPid) {
        if (!fedoraPid.startsWith("info:fedora/")) {
            return "info:fedora/" + fedoraPid;
        }
        return fedoraPid;
    }
}
