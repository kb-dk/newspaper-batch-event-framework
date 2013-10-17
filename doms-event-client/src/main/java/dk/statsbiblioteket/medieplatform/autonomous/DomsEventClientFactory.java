package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.medieplatform.autonomous.IDFormatter;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperIDFormatter;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 * Factory to create the doms event client. Usage pattern: Create a new factory object. Use the setters to set
 * properties. When done, call createDomsEventClient, and a doms event client will be constructed
 */
public class DomsEventClientFactory {


    //Default values
    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String ROUND_TRIP_TEMPLATE = "doms:Template_RoundTrip";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";
    public static final String EVENTS = "EVENTS";
    public static final String USERNAME = "fedoraAdmin";
    public static final String PASSWORD = "fedoraAdminPass";
    public static final String FEDORA_LOCATION = "http://localhost:8080/fedora";
    public static final String PIDGENERATOR_LOCATION = "http://localhost:8080/pidgenerator-service";
    public static final NewspaperIDFormatter NEWSPAPER_ID_FORMATTER = new NewspaperIDFormatter();
    //The initial values of the properties
    private String username = USERNAME;
    private String password = PASSWORD;
    private String fedoraLocation = FEDORA_LOCATION;
    private String pidGeneratorLocation = PIDGENERATOR_LOCATION;
    private IDFormatter idFormatter = NEWSPAPER_ID_FORMATTER;
    private String premisIdentifierType = PremisManipulatorFactory.TYPE;
    private String batchTemplate = BATCH_TEMPLATE;
    private String roundTripTemplate = ROUND_TRIP_TEMPLATE;
    private String hasPartRelation = HAS_PART;
    private String eventsDatastream = EVENTS;

    /**
     * Create the doms event client from the properties
     *
     * @return a new doms event client
     * @throws JAXBException         if failure to parse the included premis schemas. Should not happen
     * @throws PIDGeneratorException Failure to communicate with the pid generator
     * @throws MalformedURLException if any of the urls were broken
     */
    public DomsEventClient createDomsEventClient()
            throws
            JAXBException,
            PIDGeneratorException,
            MalformedURLException {
        Credentials creds = new Credentials(username, password);
        EnhancedFedoraImpl
                fedora =
                new EnhancedFedoraImpl(creds,
                                       fedoraLocation.replaceFirst("/(objects)?/?$", ""),
                                       pidGeneratorLocation,
                                       null);
        return new DomsEventClientCentral(fedora,
                                          idFormatter,
                                          premisIdentifierType,
                                          batchTemplate,
                                          roundTripTemplate,
                                          hasPartRelation,
                                          eventsDatastream);
    }

    public String getUsername() {
        return username;
    }

    /**
     * Set the username used to communicate with DOMS.
     *
     * @param username the username
     *
     * @see #USERNAME
     */
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Set the password used to communicate with doms
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFedoraLocation() {
        return fedoraLocation;
    }

    /**
     * SEt the location of the fedora server. Default http://localhost:8080/fedora
     *
     * @param fedoraLocation fedora location
     */
    public void setFedoraLocation(String fedoraLocation) {
        this.fedoraLocation = fedoraLocation;
    }

    public String getPidGeneratorLocation() {
        return pidGeneratorLocation;
    }

    /**
     * Set the location of the pid generator. Default http://localhost:8080/pidgenerator-service
     *
     * @param pidGeneratorLocation the pid generator
     */
    public void setPidGeneratorLocation(String pidGeneratorLocation) {
        this.pidGeneratorLocation = pidGeneratorLocation;
    }

    public IDFormatter getIdFormatter() {
        return idFormatter;
    }

    /**
     * Set the id formatter used to format batch ids to strings. Default new NewspaperIDFormatter()
     *
     * @param idFormatter the formatter
     *
     * @see NewspaperIDFormatter
     */
    public void setIdFormatter(IDFormatter idFormatter) {
        this.idFormatter = idFormatter;
    }

    public String getPremisIdentifierType() {
        return premisIdentifierType;
    }

    /**
     * Set the premis identifier type. Default
     * @param premisIdentifierType the type
     * @see PremisManipulatorFactory#TYPE
     */
    public void setPremisIdentifierType(String premisIdentifierType) {
        this.premisIdentifierType = premisIdentifierType;
    }

    public String getBatchTemplate() {
        return batchTemplate;
    }

    /**
     * Set the template objects used to generate batch objects. Default  doms:Template_Batch
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
     * @param hasPartRelation the relation
     */
    public void setHasPartRelation(String hasPartRelation) {
        this.hasPartRelation = hasPartRelation;
    }

    public String getEventsDatastream() {
        return eventsDatastream;
    }

    /**
     * The name of the premis events datastream in the round trip objects. Default EVENTS
     * @param eventsDatastream EVENTS
     */
    public void setEventsDatastream(String eventsDatastream) {
        this.eventsDatastream = eventsDatastream;
    }
}
