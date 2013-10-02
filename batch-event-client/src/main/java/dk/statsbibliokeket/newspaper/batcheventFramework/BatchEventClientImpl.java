package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.slf4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BatchEventClientImpl implements BatchEventClient {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchEventClientImpl.class);
    private String summaLocation;
    private String domsUrl;
    private String domsUser;
    private String domsPass;
    private String urlToPidGen;

    public BatchEventClientImpl(String summaLocation, String domsUrl, String domsUser, String domsPass, String urlToPidGen) {
        this.summaLocation = summaLocation;
        this.domsUrl = domsUrl;
        this.domsUser = domsUser;
        this.domsPass = domsPass;
        this.urlToPidGen = urlToPidGen;
    }

    @Override
    public void addEventToBatch(Long batchId, int roundTripNumber, String agent, Date timestamp, String details, EventID eventType, boolean outcome) throws CommunicationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String createBatchRoundTrip(Long batchId, int roundTripNumber) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Batch getBatch(Long batchId, int roundTripNumber) throws CommunicationException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastEvents, List<String> pastEventsExclude, List<String> futureEvents) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

   /* @Override
    public void addEvent(Batch batch, String premisAgent, Date timestamp, String details, EventID eventID, boolean outcome) throws CommunicationException {
        DomsEventClientFactory domsEventClientFactory = new DomsEventClientFactory();
        domsEventClientFactory.setFedoraLocation(domsUrl);
        domsEventClientFactory.setUsername(domsUser);
        domsEventClientFactory.setPassword(domsPass);
        domsEventClientFactory.setPidGeneratorLocation(urlToPidGen);


        DomsEventClient domsEventClient = null;
        try {
            domsEventClient = domsEventClientFactory.createDomsEventClient();
        } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
            throw new CommunicationException(e);
        }

        domsEventClient.addEventToBatch(batch.getBatchID(),
                    batch.getRoundTripNumber(),
                    premisAgent,
                    timestamp,
                    details,
                    eventID,
                    outcome);

    }*/

}
