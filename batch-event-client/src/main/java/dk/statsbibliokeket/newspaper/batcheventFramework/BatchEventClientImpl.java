package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClient;
import dk.statsbiblioteket.newspaper.batcheventFramework.DomsEventClientFactory;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.SBOIInterface;
import org.slf4j.Logger;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Delegating BatchEventClientImpl
 * @see SBOIClientImpl
 * @see DomsEventClientFactory
 */
public class BatchEventClientImpl implements BatchEventClient {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BatchEventClientImpl.class);
    private String summaLocation;
    private String domsUrl;
    private String domsUser;
    private String domsPass;
    private String urlToPidGen;

    private SBOIInterface sboiClient;
    private DomsEventClient domsEventClient;

    public BatchEventClientImpl(String summaLocation, String domsUrl, String domsUser, String domsPass, String urlToPidGen) {
        this.summaLocation = summaLocation;
        this.domsUrl = domsUrl;
        this.domsUser = domsUser;
        this.domsPass = domsPass;
        this.urlToPidGen = urlToPidGen;
    }

    private SBOIInterface getSboiClient() {
        if (sboiClient == null){
            sboiClient = new SBOIClientImpl(getDomsEventClient(),summaLocation);
        }
        return sboiClient;
    }

    private DomsEventClient getDomsEventClient()  {
        if (domsEventClient == null){
            DomsEventClientFactory factory = new DomsEventClientFactory();
            factory.setPidGeneratorLocation(urlToPidGen);
            factory.setPassword(domsPass);
            factory.setUsername(domsUser);
            factory.setFedoraLocation(domsUrl);
            try {
                domsEventClient = factory.createDomsEventClient();
            } catch (JAXBException | MalformedURLException | PIDGeneratorException e) {
                throw new RuntimeException(e);
            }
        }
        return domsEventClient;
    }

    @Override
    public void addEventToBatch(Long batchId, int roundTripNumber, String agent, Date timestamp, String details, EventID eventType, boolean outcome) throws CommunicationException {
        getDomsEventClient().addEventToBatch(batchId, roundTripNumber, agent, timestamp, details, eventType, outcome);
    }

    @Override
    public String createBatchRoundTrip(Long batchId, int roundTripNumber) throws CommunicationException {
        return getDomsEventClient().createBatchRoundTrip(batchId, roundTripNumber);
    }

    @Override
    public Batch getBatch(Long batchId, int roundTripNumber) throws CommunicationException, NotFoundException {
        return getDomsEventClient().getBatch(batchId, roundTripNumber);
    }

    @Override
    public Batch getBatch(String domsID) throws CommunicationException, NotFoundException {
        return getDomsEventClient().getBatch(domsID);
    }

    @Override
    public Iterator<Batch> getBatches(List<String> pastEvents, List<String> pastEventsExclude, List<String> futureEvents) throws CommunicationException {
        return getSboiClient().getBatches(pastEvents, pastEventsExclude, futureEvents);

    }

    @Override
    public Batch getBatch(Long batchID) throws CommunicationException, NotFoundException {
        return getSboiClient().getBatch(batchID);
    }
}
