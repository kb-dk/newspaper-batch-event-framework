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
public class DomsEventStorage implements EventStorer {

    private static Logger log = LoggerFactory.getLogger(DomsEventStorage.class);

    private final EnhancedFedora fedora;
    private final IDFormatter idFormatter;
    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory premisFactory;
    private String createBatchRoundTripComment = "Creating batch round trip";
    private String addEventToBatchComment = "Adding event to natch round trip";

    DomsEventStorage(EnhancedFedora fedora, IDFormatter idFormatter, String type, String batchTemplate,
                     String roundTripTemplate, String hasPart_relation, String eventsDatastream) throws JAXBException {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        this.batchTemplate = batchTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory(idFormatter, type);
    }

    @Override
    public Date addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp, String details,
                                String eventType, boolean outcome) throws CommunicationException {
        String roundTripObjectPid = createBatchRoundTrip(batchId, roundTripNumber);

        try {
            PremisManipulator premisObject;
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(roundTripObjectPid, eventsDatastream, null);

                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = premisFactory.createInitialPremisBlob(batchId, roundTripNumber);
            }
            premisObject = premisObject.addEvent(agent, timestamp, details, eventType, outcome);
            try {
                return fedora.modifyDatastreamByValue(
                        roundTripObjectPid,
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

    @Override
    public Date addEventToItem(Item item, String agent, Date timestamp, String details, String eventType,
                               boolean outcome) throws CommunicationException {
        try {
            String itemID = item.getDomsID();
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
     * @param batchId         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return the pid of the doms object corresponding to the round trip
     * @throws CommunicationException if communication with doms failed
     */
    public String createBatchRoundTrip(String batchId, int roundTripNumber) throws CommunicationException {
        String id = idFormatter.formatFullID(batchId, roundTripNumber);
        try {
            try {
                //find the roundTrip Object
                return getRoundTripID(batchId, roundTripNumber);
            } catch (BackendInvalidResourceException e) {
                //no roundTripObject, so sad
                //but alas, we can continue
            }

            //find the batch object
            String batchObject;

            List<String> founds = fedora.findObjectFromDCIdentifier(idFormatter.formatBatchID(batchId));
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                batchObject = fedora.cloneTemplate(
                        batchTemplate, Arrays.asList(idFormatter.formatBatchID(batchId)), createBatchRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(id), createBatchRoundTripComment);

            //connect batch object to round trip object
            fedora.addRelation(
                    batchObject,
                    toFedoraID(batchObject),
                    hasPart_relation,
                    toFedoraID(roundTripObject),
                    false,
                    createBatchRoundTripComment);

            //create the initial EVENTS datastream

            String premisBlob = premisFactory.createInitialPremisBlob(batchId, roundTripNumber).toXML();
            fedora.modifyDatastreamByValue(
                    roundTripObject, eventsDatastream, null,null,premisBlob.getBytes(), null, "text/xml",createBatchRoundTripComment,null);


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException | JAXBException e) {
            throw new CommunicationException(e);
        }


    }

    public Batch getBatch(String batchId, Integer roundTripNumber) throws CommunicationException, NotFoundException {
        String roundTripID;
        if (roundTripNumber == null) {
            roundTripNumber = 0;
        }
        try {
            roundTripID = getRoundTripID(batchId, roundTripNumber);
            return getBatch(roundTripID);
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
                return o1.getRoundTripNumber() - o2.getRoundTripNumber();
            }
        };
        try {
            List<String> founds = fedora.findObjectFromDCIdentifier(idFormatter.formatBatchID(batchId));
            if (founds == null || founds.size() == 0) {
                return null;
            }
            String batchObjectPid = founds.get(0);
            List<FedoraRelation> roundtripRelations = fedora.getNamedRelations(batchObjectPid, hasPart_relation, null);
            List<Batch> roundtrips = new ArrayList<Batch>();
            for (FedoraRelation roundtripRelation: roundtripRelations) {
                roundtrips.add(getBatch(roundtripRelation.getObject()));
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
    public Batch getBatch(String domsId) throws CommunicationException {
        try {
            String premisPreBlob = fedora.getXMLDatastreamContents(domsId, eventsDatastream, null);
            PremisManipulator premisObject
                    = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            Batch batch = premisObject.toBatch();
            batch.setDomsID(domsId);
            return batch;
        } catch (BackendInvalidResourceException | BackendMethodFailedException | JAXBException |
                BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxAttempts,
                                                      long waitTime, String eventId) throws
                                                                                     CommunicationException,
                                                                                     NotFoundException {
        int attempts = 0;
        int eventsRemoved = -1;
        while ((eventsRemoved = attemptWorkflowRestart(batchId, roundTripNumber, eventId)) < 0) {
            attempts++;
            if (attempts == maxAttempts) {
                String msg = "Failed to trigger restart of batch round-trip " + getFullBatchId(
                        batchId, roundTripNumber) +
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
    public int triggerWorkflowRestartFromFirstFailure(String batchId, int roundTripNumber, int maxTries,
                                                      long waitTime) throws CommunicationException, NotFoundException {
        return triggerWorkflowRestartFromFirstFailure(batchId, roundTripNumber, maxTries, waitTime, null);
    }

    /**
     * This method carries out a single attempt to restart the workflow from where it first failed.
     *
     * @param batchId         the batchId.
     * @param roundTripNumber the round-trip number.
     * @param eventId         the first event to remove or null if all events after the first failure are to be
     *                        removed.
     *
     * @return the number of events removed or -1 of there was a ConcurrentModificationException thrown.
     * @throws CommunicationException if there was a problem communicating with DOMS.
     */
    private int attemptWorkflowRestart(String batchId, int roundTripNumber, String eventId) throws
                                                                                            CommunicationException,
                                                                                            NotFoundException {
        String roundTripObjectPid = null;
        try {
            roundTripObjectPid = getRoundTripID(batchId, roundTripNumber);
        } catch (BackendInvalidResourceException e) {
            throw new NotFoundException(
                    "Could not find DOMS object for " + getFullBatchId(batchId, roundTripNumber), e);
        }
        try {
            Date lastModifiedDate = fedora.getObjectProfile(roundTripObjectPid, null).getObjectLastModifiedDate();
            String premisPreBlob = fedora.getXMLDatastreamContents(roundTripObjectPid, eventsDatastream, null);
            PremisManipulator premisObject
                    = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            int eventsRemoved = premisObject.removeEventsFromFailureOrEvent(eventId);
            if (eventsRemoved > 0) {
                //backupEventsForBatch(batchId, roundTripNumber);
                try {
                    fedora.modifyDatastreamByValue(
                            roundTripObjectPid,
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
                            "Failed to trigger restart of batch round trip for " +
                            getFullBatchId(
                                    batchId,
                                    roundTripNumber) + " on this attempt. Another process modified the object concurrently."
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

    private String getFullBatchId(String batchId, int roundTripNumber) {
        return "B" + batchId + "-RT" + roundTripNumber;
    }

    /**
     * Retrieve the corresponding doms pid of the round trip object
     *
     * @return the doms round trip pid
     * @throws CommunicationException          failed to communicate
     * @throws BackendInvalidResourceException object not found
     */
    String getRoundTripID(String batchId, int roundTripNumber) throws
                                                               CommunicationException,
                                                               BackendInvalidResourceException {

        try {
            //find the Round Trip object
            final String dcIdentifier = idFormatter.formatFullID(batchId, roundTripNumber);
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
