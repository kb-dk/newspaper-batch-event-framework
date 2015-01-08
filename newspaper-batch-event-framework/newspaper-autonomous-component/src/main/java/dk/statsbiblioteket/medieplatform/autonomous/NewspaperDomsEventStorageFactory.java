package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

@SuppressWarnings("deprecation")//Credentials
public class NewspaperDomsEventStorageFactory extends DomsEventStorageFactory<Batch> {

    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String ROUND_TRIP_TEMPLATE = "doms:Template_RoundTrip";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";

    protected String batchTemplate = BATCH_TEMPLATE;
    protected String roundTripTemplate = ROUND_TRIP_TEMPLATE;
    protected String hasPartRelation = HAS_PART;

    @Override
    @SuppressWarnings("deprecation")  //Credentials
    public NewspaperDomsEventStorage createDomsEventStorage() throws
                                                            JAXBException,
                                                            PIDGeneratorException,
                                                            MalformedURLException {
        Credentials creds = new Credentials(username, password);
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(creds,
                                                                  fedoraLocation.replaceFirst("/(objects)?/?$", ""),
                                                                  pidGeneratorLocation,
                                                                  null,
                                                                  retries,
                                                                  retries,
                                                                  retries,
                                                                  delayBetweenRetries);
        if (itemFactory == null){
            itemFactory = new BatchItemFactory();
        }
        return new NewspaperDomsEventStorage(fedora,
                                                    premisIdentifierType, batchTemplate,roundTripTemplate,hasPartRelation,eventsDatastream, itemFactory);
    }

    public String getBatchTemplate() {
        return batchTemplate;
    }

    /**
     * Set the template objects used to generate batch objects. Default  doms:Template_Batch
     *
     * @param batchTemplate the template
     */
    public void setBatchTemplate(String batchTemplate) {
        this.batchTemplate = batchTemplate;
    }

    public String getRoundTripTemplate() {
        return roundTripTemplate;
    }

    /**
     * Set the template object used to generate round trip objects. Default doms:Template_RoundTrip
     *
     * @param roundTripTemplate the template
     */
    public void setRoundTripTemplate(String roundTripTemplate) {
        this.roundTripTemplate = roundTripTemplate;
    }

    public String getHasPartRelation() {
        return hasPartRelation;
    }

    /**
     * The full predicate for the hasPartRelation. Default info:fedora/fedora-system:def/relations-external#hasPart
     *
     * @param hasPartRelation the relation
     */
    public void setHasPartRelation(String hasPartRelation) {
        this.hasPartRelation = hasPartRelation;
    }
}
