package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.sbutil.webservices.authentication.Credentials;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 * Factory to create the {@link DomsEventStorage} implementation of {@link EventStorer}.
 *
 * Usage pattern:
 *   Create a new factory object. Use the setters to set properties.
 *   When done, call {@link #createDomsEventStorage}, and a doms event storage will be constructed.
 */
@SuppressWarnings("deprecation")//Credentials
public class DomsEventStorageFactory<T extends Item> {


    //Default values
    public static final String EVENTS = "EVENTS";
    public static final String USERNAME = "fedoraAdmin";
    public static final String PASSWORD = "fedoraAdminPass";
    public static final String FEDORA_LOCATION = "http://localhost:8080/fedora";
    public static final String PIDGENERATOR_LOCATION = "http://localhost:8080/pidgenerator-service";
    //The initial values of the properties
    protected String username = USERNAME;
    protected String password = PASSWORD;
    protected String fedoraLocation = FEDORA_LOCATION;
    protected String pidGeneratorLocation = PIDGENERATOR_LOCATION;
    protected int retries = 1;
    protected int delayBetweenRetries = 100;
    protected String premisIdentifierType = PremisManipulatorFactory.TYPE;
    protected String eventsDatastream = EVENTS;
    protected ItemFactory<T> itemFactory;

    /**
     * Create the doms event storage from the properties
     *
     * @return a new doms event storage
     * @throws JAXBException         if failure to parse the included premis schemas. Should not happen
     * @throws PIDGeneratorException Failure to communicate with the pid generator
     * @throws MalformedURLException if any of the urls were broken
     */
    @SuppressWarnings("deprecation")//Credentials
    public DomsEventStorage<T> build() throws JAXBException, PIDGeneratorException, MalformedURLException {
        Credentials creds = new Credentials(username, password);
        EnhancedFedoraImpl fedora = new EnhancedFedoraImpl(
                creds, fedoraLocation.replaceFirst("/(objects)?/?$", ""), pidGeneratorLocation, null, retries, delayBetweenRetries);
        return new DomsEventStorage<>(
                fedora, premisIdentifierType, eventsDatastream,
                itemFactory);
    }

    /**
     * Set the username used to communicate with DOMS.
     *
     * @param username the username
     *
     * @see #USERNAME
     */
    public DomsEventStorageFactory<T> setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Set the password used to communicate with doms
     *
     * @param password the password
     */
    public DomsEventStorageFactory<T> setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * SEt the location of the fedora server. Default http://localhost:8080/fedora
     *
     * @param fedoraLocation fedora location
     */
    public DomsEventStorageFactory<T> setFedoraLocation(String fedoraLocation) {
        this.fedoraLocation = fedoraLocation;
        return this;
    }

    /**
     * Set the location of the pid generator. Default http://localhost:8080/pidgenerator-service
     *
     * @param pidGeneratorLocation the pid generator
     */
    public void setPidGeneratorLocation(String pidGeneratorLocation) {
        this.pidGeneratorLocation = pidGeneratorLocation;
    }

    public DomsEventStorageFactory<T> setItemFactory(ItemFactory<T> itemFactory) {
        this.itemFactory = itemFactory;
        return this;
    }

    /**
     * Set the premis identifier type. Default
     *
     * @param premisIdentifierType the type
     *
     * @see PremisManipulatorFactory#TYPE
     */
    public void setPremisIdentifierType(String premisIdentifierType) {
        this.premisIdentifierType = premisIdentifierType;
    }


    /**
     * The name of the premis events datastream in the round trip objects. Default EVENTS
     *
     * @param eventsDatastream EVENTS
     */
    public void setEventsDatastream(String eventsDatastream) {
        this.eventsDatastream = eventsDatastream;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setDelayBetweenRetries(int delayBetweenRetries) {
        this.delayBetweenRetries = delayBetweenRetries;
    }
}
