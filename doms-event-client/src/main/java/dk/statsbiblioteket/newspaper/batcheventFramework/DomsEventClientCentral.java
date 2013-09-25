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
    private final String runTemplate;
    private final String hasPart_relation;
    private final String eventsDatastream;
    private final PremisManipulatorFactory premisFactory;

    DomsEventClientCentral(EnhancedFedora fedora,
                           IDFormatter idFormatter,
                           String type,
                           String batchTemplate,
                           String runTemplate,
                           String hasPart_relation,
                           String eventsDatastream) {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        this.batchTemplate = batchTemplate;
        this.runTemplate = runTemplate;
        this.hasPart_relation = hasPart_relation;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory(idFormatter, type);
    }

    @Override
    public void addEventToBatch(Long batchId,
                                int runNr,
                                String agent,
                                Date timestamp,
                                String details,
                                EventID eventType,
                                boolean outcome) throws CommunicationException {
        String runObject = createBatchRun(batchId, runNr);

        try {
            PremisManipulator premisObject;
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(runObject, eventsDatastream, null);

                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = premisFactory.createInitialPremisBlob(batchId, runNr);
            }
            premisObject = premisObject.addEvent(agent, timestamp, details, eventType, outcome);
            try {
                fedora.modifyDatastreamByValue(runObject, eventsDatastream, premisObject.toXML(), "comments");
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public String createBatchRun(Long batchId, int runNr) throws CommunicationException {
        String id = idFormatter.formatFullID(batchId, runNr);
        try {
            //find the run object
            List<String> founds = fedora.listObjectsWithThisLabel(id);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            //no run object, so sad

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
            String runObject;
            try {
                runObject = fedora.cloneTemplate(runTemplate, Arrays.asList(id), "comment");
            } catch (ObjectIsWrongTypeException | BackendInvalidResourceException e) {
                throw new CommunicationException(e);
            }
            try {
                //set label
                fedora.modifyObjectLabel(runObject, id, "comment");


                //connect batch object to run object
                fedora.addRelation(batchObject,
                        toFedoraID(batchObject),
                        hasPart_relation,
                        toFedoraID(runObject),
                        false,
                        "comment");

                //create the initial EVENTS datastream
                try {
                    String premisBlob = premisFactory.createInitialPremisBlob(batchId,runNr).toXML();
                    fedora.modifyDatastreamByValue(runObject, eventsDatastream, premisBlob, "comment");
                } catch (JAXBException e) {
                    //how can this fail???
                    throw new RuntimeException(e);
                }
            } catch (BackendInvalidResourceException e) {
                //run object not found
                //no, I have just created it....
            }
            return runObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException e) {
            throw new CommunicationException(e);
        }


    }

    @Override
    public Batch getBatch(Long batchId, int runNr) throws CommunicationException{

        String runID = null;
        try {
            runID = getRunId(batchId, runNr);
            String premisPreBlob = fedora.getXMLDatastreamContents(runID, eventsDatastream, null);
            PremisManipulator premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            return premisObject.toBatch();
        } catch (BackendInvalidResourceException | BackendMethodFailedException | JAXBException | BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }


    }

    private String getRunId(Long batchId, int runNr) throws CommunicationException, BackendInvalidResourceException {

        String id = idFormatter.formatFullID(batchId, runNr);
        try {
            //find the run object
            List<String> founds = fedora.listObjectsWithThisLabel(id);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            throw new BackendInvalidResourceException("run object not found");
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
