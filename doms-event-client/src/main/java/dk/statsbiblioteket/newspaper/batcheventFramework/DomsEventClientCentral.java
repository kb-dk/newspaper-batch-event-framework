package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.CentralWebserviceService;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.central.Relation;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class DomsEventClientCentral implements DomsEventClient {

    private static final QName CENTRAL_WEBSERVICE_SERVICE = new QName(
            "http://central.doms.statsbiblioteket.dk/",
            "CentralWebserviceService");
    private static final String BATCH_TEMPLATE = "";
    private static final String RUN_TEMPLATE = "";
    private static final String HAS_PART = "";
    private static final String EVENTS = "";

    private final CentralWebservice domsAPI;

    public DomsEventClientCentral(String userName, String password, URL domsWSAPIEndpoint) {
        domsAPI = new CentralWebserviceService(domsWSAPIEndpoint,
                CENTRAL_WEBSERVICE_SERVICE).getCentralWebservicePort();

        Map<String, Object> domsAPILogin = ((BindingProvider) domsAPI)
                .getRequestContext();
        domsAPILogin.put(BindingProvider.USERNAME_PROPERTY, userName);
        domsAPILogin.put(BindingProvider.PASSWORD_PROPERTY, password);
    }

    @Override
    public void addEventToBatch(String batchId,
                                int runNr,
                                String agent,
                                Date timestamp,
                                String details,
                                EventID eventType,
                                boolean outcome,
                                String outcomeDetails) throws CommunicationException {
        String runObject = createBatchRun(batchId, runNr);

        try {
            PremisManipulator premisObject;
            try {
                String premisPreBlob = null;
                premisPreBlob = domsAPI.getDatastreamContents(runObject, EVENTS);
                premisObject = PremisManipulator.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (InvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = PremisManipulator.createInitialPremisBlob(toFullID(batchId, runNr));
            }
            premisObject = premisObject.addEvent(agent, timestamp, details, eventType, outcome);
            domsAPI.modifyDatastream(runObject,EVENTS,premisObject.toString(),"comments");
        } catch (InvalidCredentialsException | MethodFailedException | InvalidResourceException e) {
            throw new CommunicationException(e);
        } catch (JAXBException e) {
            //okay, fair, if the existing premis is bad
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public String createBatchRun(String batchId, int runNr) throws CommunicationException {
        String id = toFullID(batchId, runNr);
        try {
            try {//find the run object
                return domsAPI.getFileObjectWithURL(id);
            } catch (InvalidResourceException e) {
                //no run object, so sad
            }
            //but alas, we can continue
            //find the batch object
            String batchObject;
            try {
                batchObject = domsAPI.getFileObjectWithURL(toBatchID(batchId));
            } catch (InvalidResourceException e) {
                //no batch object either, more sad
                //create it, then
                try {
                    batchObject = domsAPI.newObject(BATCH_TEMPLATE, Arrays.asList(toBatchID(batchId)),"comment");
                } catch (InvalidResourceException e1) {
                    //template not found.... This is serious
                    throw new CommunicationException(e1);
                }
                try {
                    domsAPI.setObjectLabel(batchObject,toBatchID(batchId),"comment2");
                } catch (InvalidResourceException e1) {
                    //no, I have just created it..... So I KNOW it's there
                    throw new CommunicationException(e1);
                }
            }
            String runObject;
            try {
                runObject = domsAPI.newObject(RUN_TEMPLATE, Arrays.asList(id), "comment");
            } catch (InvalidResourceException e) {
                //run template not found, this is serious
                throw new CommunicationException(e);
            }
            try {
                //set label
                domsAPI.setObjectLabel(runObject,id,"comment");

                //connect batch object to run object
                Relation relation = new Relation();
                relation.setLiteral(false);
                relation.setPredicate(HAS_PART);
                relation.setSubject(toFedoraID(batchObject));
                relation.setObject(toFedoraID(runObject));
                domsAPI.addRelation(batchObject,relation,"comment");

                //create the initial EVENTS datastream
                try {
                    String premisBlob = PremisManipulator.createInitialPremisBlob(id).toString();
                    domsAPI.modifyDatastream(runObject,EVENTS,premisBlob,"comment");
                } catch (JAXBException e) {
                    //how can this fail???
                    throw new RuntimeException(e);
                }
            } catch (InvalidResourceException e) {
                //run object not found
                //no, I have just created it....
            }
            return runObject;
        } catch (InvalidCredentialsException | MethodFailedException e) {
            throw new CommunicationException(e);
        }
    }

    private String toFedoraID(String batchObject) {
        if (!batchObject.startsWith("info:fedora/")){
            return "info:fedora/"+batchObject;
        }
        return batchObject;
    }

    private String toBatchID(String batchId) {
        return "B"+batchId;
    }

    private String toFullID(String batchId, int runNr) {
        return "B"+batchId+"-RT"+runNr;
    }
}
