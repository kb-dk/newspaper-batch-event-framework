package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

/** Implementation of the DomsEventClient, using the Central Webservice library to communicate with DOMS */
public class DomsEventClientCentral implements DomsEventClient {

    private static Logger log = LoggerFactory.getLogger(DomsEventClientCentral.class);

    private final EnhancedFedora fedora;
    private final IDFormatter idFormatter;
    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory premisFactory;
    private String createBatchRoundTripComment = "TODO"; //TODO
    private String addEventToBatchComment = "TODO";//TODO

    DomsEventClientCentral(EnhancedFedora fedora, IDFormatter idFormatter, String type, String batchTemplate,
                           String roundTripTemplate, String hasPart_relation, String eventsDatastream) throws
                                                                                                       JAXBException {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        this.batchTemplate = batchTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory(idFormatter, type);
    }

    @Override
    public void addEventToBatch(String batchId, int roundTripNumber, String agent, Date timestamp, String details,
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
                fedora.modifyDatastreamByValue(
                        roundTripObjectPid, eventsDatastream, premisObject.toXML(), null, addEventToBatchComment);
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
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
                    roundTripObject, eventsDatastream, premisBlob, null, createBatchRoundTripComment);


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException | JAXBException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public Batch getBatch(String batchId, Integer roundTripNumber) throws CommunicationException {
        String roundTripID;
        try {
            roundTripID = getRoundTripID(batchId, roundTripNumber);
            return getBatch(roundTripID);
        } catch (BackendInvalidResourceException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
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

    /**
     * This method creates a timestamped backup of the EVENTS datastream for this round-trip.
     *
     * @param batchId         the batchId.
     * @param roundTripNumber the round-trip number.
     *
     * @return the name of the new datastream.
     * @throws CommunicationException if there was a problem communicating with DOMS.
     */
    String backupEventsForBatch(String batchId, int roundTripNumber) throws CommunicationException, NotFoundException {
        String roundTripObjectPid = null;
        try {
            roundTripObjectPid = getRoundTripID(batchId, roundTripNumber);
        } catch (BackendInvalidResourceException e) {
            throw new NotFoundException("Did not find " + getFullBatchId(batchId, roundTripNumber), e);
        }
        try {
            try {
                String backupDatastream = eventsDatastream + "_" + new Date().getTime();
                String eventXml = fedora.getXMLDatastreamContents(roundTripObjectPid, eventsDatastream, null);
                fedora.modifyDatastreamByValue(roundTripObjectPid, backupDatastream, eventXml, null, "Premis backup");
                return backupDatastream;
            } catch (BackendInvalidResourceException e) {
                return null;
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException e) {
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
                backupEventsForBatch(batchId, roundTripNumber);
                try {
                    fedora.modifyDatastreamByValue(
                            roundTripObjectPid,
                            eventsDatastream,
                            null,
                            null,
                            premisObject.toXML().getBytes("UTF-8"),
                            null,
                            "Event list trimmed of all events after earliest failure",
                            lastModifiedDate.getTime());
                } catch (ConcurrentModificationException e) {
                    log.warn(
                            "Failed to trigger restart of batch round trip for " +
                            getFullBatchId(
                                    batchId,
                                    roundTripNumber) + " on this attempt. Another process modified the object concurrently.");
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
     * @throws CommunicationException failed to communicate
     * @throws BackendInvalidResourceException
     *                                object not found
     */
    private String getRoundTripID(String batchId, int roundTripNumber) throws
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
