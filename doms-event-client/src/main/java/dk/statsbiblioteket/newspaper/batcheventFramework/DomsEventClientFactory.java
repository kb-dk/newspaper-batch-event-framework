package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

public class DomsEventClientFactory {


    //Extract factory with these properties. Perhaps constructor
    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String RUN_TEMPLATE = "doms:Template_Run";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";
    public static final String EVENTS = "EVENTS";
    public static final String USERNAME = "fedoraAdmin";
    public static final String PASSWORD = "fedoraAdminPass";
    public static final String FEDORA_LOCATION = "http://localhost:8080/fedora";
    public static final String PIDGENERATOR_LOCATION = "http://localhost:8080/pidgenerator-service";
    public static final NewspaperIDFormatter NEWSPAPER_ID_FORMATTER = new NewspaperIDFormatter();


    private String username = USERNAME;
    private String password = PASSWORD;
    private String fedoraLocation = FEDORA_LOCATION;
    private String pidGeneratorLocation = PIDGENERATOR_LOCATION;
    private IDFormatter idFormatter = NEWSPAPER_ID_FORMATTER;
    private String premisIdentifierType = PremisManipulatorFactory.TYPE;
    private String batchTemplate = BATCH_TEMPLATE;
    private String runTemplate = RUN_TEMPLATE;
    private String hasPartRelation = HAS_PART;
    private String eventsDatastream = EVENTS;


    public DomsEventClient createDomsEventClient() throws JAXBException, PIDGeneratorException, MalformedURLException {
        Credentials creds = new Credentials(username, password);
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(creds, fedoraLocation, pidGeneratorLocation, null);
        return new DomsEventClientCentral(fedora,idFormatter, premisIdentifierType,batchTemplate,runTemplate,hasPartRelation,eventsDatastream);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFedoraLocation() {
        return fedoraLocation;
    }

    public void setFedoraLocation(String fedoraLocation) {
        this.fedoraLocation = fedoraLocation;
    }

    public String getPidGeneratorLocation() {
        return pidGeneratorLocation;
    }

    public void setPidGeneratorLocation(String pidGeneratorLocation) {
        this.pidGeneratorLocation = pidGeneratorLocation;
    }

    public IDFormatter getIdFormatter() {
        return idFormatter;
    }

    public void setIdFormatter(IDFormatter idFormatter) {
        this.idFormatter = idFormatter;
    }

    public String getPremisIdentifierType() {
        return premisIdentifierType;
    }

    public void setPremisIdentifierType(String premisIdentifierType) {
        this.premisIdentifierType = premisIdentifierType;
    }

    public String getBatchTemplate() {
        return batchTemplate;
    }

    public void setBatchTemplate(String batchTemplate) {
        this.batchTemplate = batchTemplate;
    }

    public String getRunTemplate() {
        return runTemplate;
    }

    public void setRunTemplate(String runTemplate) {
        this.runTemplate = runTemplate;
    }

    public String getHasPartRelation() {
        return hasPartRelation;
    }

    public void setHasPartRelation(String hasPartRelation) {
        this.hasPartRelation = hasPartRelation;
    }

    public String getEventsDatastream() {
        return eventsDatastream;
    }

    public void setEventsDatastream(String eventsDatastream) {
        this.eventsDatastream = eventsDatastream;
    }
}
