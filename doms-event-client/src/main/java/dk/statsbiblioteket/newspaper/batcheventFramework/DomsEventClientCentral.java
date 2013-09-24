package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DomsEventClientCentral<T> implements DomsEventClient<T> {


    //Extract factory with these properties. Perhaps constructor
    private static final String BATCH_TEMPLATE = "TODO";
    private static final String RUN_TEMPLATE = "TODO";
    private static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";
    private static final String EVENTS = "EVENTS";

    private final EnhancedFedora fedora;
    private IDFormatter<T> idFormatter;
    private PremisManipulatorFactory<T> premisFactory;

    public DomsEventClientCentral(EnhancedFedora fedora, IDFormatter<T> idFormatter, String type) {
        this.fedora = fedora;
        this.idFormatter = idFormatter;
        premisFactory = new PremisManipulatorFactory<>(idFormatter, type);
    }

    @Override
    public void addEventToBatch(T batchId,
                                int runNr,
                                String agent,
                                Date timestamp,
                                String details,
                                EventID eventType,
                                boolean outcome,
                                String outcomeDetails) throws CommunicationException {
        String runObject = createBatchRun(batchId, runNr);

        try {
            PremisManipulator<T> premisObject;
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(runObject, EVENTS, null);

                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = premisFactory.createInitialPremisBlob(batchId, runNr);
            }
            premisObject = premisObject.addEvent(agent, timestamp, details, eventType, outcome);
            try {
                fedora.modifyDatastreamByValue(runObject, EVENTS, premisObject.toXML(), "comments");
            } catch (BackendInvalidResourceException e1) {
                //But I just created the object, it must be there
                throw new CommunicationException(e1);
            }
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public String createBatchRun(T batchId, int runNr) throws CommunicationException {
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
                    batchObject = fedora.cloneTemplate(BATCH_TEMPLATE, Arrays.asList(idFormatter.formatBatchID(batchId)), "comment");
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
                runObject = fedora.cloneTemplate(RUN_TEMPLATE, Arrays.asList(id), "comment");
            } catch (ObjectIsWrongTypeException | BackendInvalidResourceException e) {
                throw new CommunicationException(e);
            }
            try {
                //set label
                fedora.modifyObjectLabel(runObject, id, "comment");


                //connect batch object to run object
                fedora.addRelation(batchObject,
                        toFedoraID(batchObject),
                        HAS_PART,
                        toFedoraID(runObject),
                        false,
                        "comment");

                //create the initial EVENTS datastream
                try {
                    String premisBlob = premisFactory.createInitialPremisBlob(batchId,runNr).toXML();
                    fedora.modifyDatastreamByValue(runObject, EVENTS, premisBlob, "comment");
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

    private String toFedoraID(String batchObject) {
        if (!batchObject.startsWith("info:fedora/")) {
            return "info:fedora/" + batchObject;
        }
        return batchObject;
    }
}
