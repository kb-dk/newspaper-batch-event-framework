package dk.statsbiblioteket.autonomous.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.Batch;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.CommunicationException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Implementation of the DomsEventClient, using the Central Webservice library to communicate with DOMS
 */
public class DomsEventClientCentral
        implements DomsEventClient {


    private final EnhancedFedora fedora;
    private final IDFormatter idFormatter;
    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory premisFactory;
    private String createBatchRoundTripComment = "TODO"; //TODO
    private String addEventToBatchComment = "TODO";//TODO

    DomsEventClientCentral(EnhancedFedora fedora,
                           IDFormatter idFormatter,
                           String type,
                           String batchTemplate,
                           String roundTripTemplate,
                           String hasPart_relation,
                           String eventsDatastream) {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        this.batchTemplate = batchTemplate;
        this.roundTripTemplate = roundTripTemplate;
        this.hasPart_relation = hasPart_relation;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory(idFormatter, type);
    }

    @Override
    public void addEventToBatch(Long batchId,
                                int roundTripNumber,
                                String agent,
                                Date timestamp,
                                String details,
                                String eventType,
                                boolean outcome)
            throws
            CommunicationException {
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
                fedora.modifyDatastreamByValue(roundTripObjectPid,
                                               eventsDatastream,
                                               premisObject.toXML(),
                                               null,
                                               addEventToBatchComment);
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public String createBatchRoundTrip(Long batchId,
                                       int roundTripNumber)
            throws
            CommunicationException {
        String id = idFormatter.formatFullID(batchId, roundTripNumber);
        try {
            try {
                //find the roundTrip Object
                return getRoundTripID(batchId, roundTripNumber);
            } catch (BackendInvalidResourceException e) {
                //no roundTripObject, so sad
            }
            //but alas, we can continue
            //find the batch object
            String batchObject;

            List<String> founds = fedora.listObjectsWithThisLabel(idFormatter.formatBatchID(batchId));
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                batchObject = fedora.cloneTemplate(batchTemplate,
                                                   Arrays.asList(idFormatter.formatBatchID(batchId)),
                                                   createBatchRoundTripComment);
                fedora.modifyObjectLabel(batchObject, idFormatter.formatBatchID(batchId), createBatchRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(id), createBatchRoundTripComment);


            //set label
            fedora.modifyObjectLabel(roundTripObject, id, createBatchRoundTripComment);


            //connect batch object to round trip object
            fedora.addRelation(batchObject,
                               toFedoraID(batchObject),
                               hasPart_relation,
                               toFedoraID(roundTripObject),
                               false,
                               createBatchRoundTripComment);

            //create the initial EVENTS datastream

            String premisBlob = premisFactory.createInitialPremisBlob(batchId, roundTripNumber).toXML();
            fedora.modifyDatastreamByValue(roundTripObject,
                                           eventsDatastream,
                                           premisBlob,
                                           null,
                                           createBatchRoundTripComment);


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException | JAXBException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public Batch getBatch(Long batchId,
                          Integer roundTripNumber)
            throws
            CommunicationException {
        String roundTripID;
        try {
            roundTripID = getRoundTripID(batchId, roundTripNumber);
            return getBatch(roundTripID);
        } catch (BackendInvalidResourceException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public Batch getBatch(String domsId)
            throws
            CommunicationException {
        try {
            String premisPreBlob = fedora.getXMLDatastreamContents(domsId, eventsDatastream, null);
            PremisManipulator premisObject =
                    premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            return premisObject.toBatch();
        } catch (BackendInvalidResourceException | BackendMethodFailedException | JAXBException |
                BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }


    }

    /**
     * Retrieve the corresponding doms pid of the round trip object
     *
     * @param batchId         the batch id
     * @param roundTripNumber the round trip number
     *
     * @return the doms round trip pid
     * @throws CommunicationException failed to communicate
     * @throws BackendInvalidResourceException
     *                                object not found
     */
    private String getRoundTripID(Long batchId,
                                  int roundTripNumber)
            throws
            CommunicationException,
            BackendInvalidResourceException {
        String id = idFormatter.formatFullID(batchId, roundTripNumber);
        try {
            //find the Round Trip object
            List<String> founds = fedora.listObjectsWithThisLabel(id);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            throw new BackendInvalidResourceException("Round Trip object not found");
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
