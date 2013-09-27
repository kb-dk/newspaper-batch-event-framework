package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DomsEventClientCentral implements DomsEventClient {


    private final EnhancedFedora fedora;
    private final IDFormatter idFormatter;
    private final String batchTemplate;
    private final String roundTripTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory premisFactory;

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
                                EventID eventType,
                                boolean outcome) throws CommunicationException {
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
                fedora.modifyDatastreamByValue(roundTripObjectPid, eventsDatastream, premisObject.toXML(), "comments");
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public String createBatchRoundTrip(Long batchId, int roundTripNumber) throws CommunicationException {
        String id = idFormatter.formatFullID(batchId, roundTripNumber);
        try {
            //find the roundTrip Object
            List<String> founds = fedora.listObjectsWithThisLabel(id);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            //no roundTripObject, so sad

            //but alas, we can continue
            //find the batch object
            String batchObject;

            founds = fedora.listObjectsWithThisLabel(idFormatter.formatBatchID(batchId));
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                try {
                    batchObject = fedora.cloneTemplate(batchTemplate, Arrays.asList(idFormatter.formatBatchID(batchId)), "comment");
                } catch (ObjectIsWrongTypeException | BackendInvalidResourceException e) {
                    throw new CommunicationException(e);
                }
                try {
                    fedora.modifyObjectLabel(batchObject, idFormatter.formatBatchID(batchId), "comment2");
                } catch (BackendInvalidResourceException e) {
                    //no, I have just created it..... So I KNOW it's there
                    throw new CommunicationException(e);
                }
            }
            String roundTripObject;
            try {
                roundTripObject = fedora.cloneTemplate(roundTripTemplate, Arrays.asList(id), "comment");
            } catch (ObjectIsWrongTypeException | BackendInvalidResourceException e) {
                throw new CommunicationException(e);
            }
            try {
                //set label
                fedora.modifyObjectLabel(roundTripObject, id, "comment");


                //connect batch object to round trip object
                fedora.addRelation(batchObject,
                        toFedoraID(batchObject),
                        hasPart_relation,
                        toFedoraID(roundTripObject),
                        false,
                        "comment");

                //create the initial EVENTS datastream
                try {
                    String premisBlob = premisFactory.createInitialPremisBlob(batchId, roundTripNumber).toXML();
                    fedora.modifyDatastreamByValue(roundTripObject, eventsDatastream, premisBlob, "comment");
                } catch (JAXBException e) {
                    //how can this fail???
                    throw new RuntimeException(e);
                }
            } catch (BackendInvalidResourceException e) {
                //round trip object not found
                //no, I have just created it....
            }
            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public Batch getBatch(Long batchId, int roundTripNumber) throws CommunicationException{

        String roundTripID = null;
        try {
            roundTripID = getRoundTripID(batchId, roundTripNumber);
            String premisPreBlob = fedora.getXMLDatastreamContents(roundTripID, eventsDatastream, null);
            PremisManipulator premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            return premisObject.toBatch();
        } catch (BackendInvalidResourceException | BackendMethodFailedException | JAXBException | BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }


    }

    private String getRoundTripID(Long batchId, int roundTripNumber) throws CommunicationException, BackendInvalidResourceException {

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

    private String toFedoraID(String batchObject) {
        if (!batchObject.startsWith("info:fedora/")) {
            return "info:fedora/" + batchObject;
        }
        return batchObject;
    }
}
