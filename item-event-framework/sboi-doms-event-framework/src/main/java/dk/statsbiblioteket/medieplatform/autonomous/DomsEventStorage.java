package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Access to DOMS batch and event storage using the Central Webservice library to communicate with DOMS.
 * Implements the {@link EventStorer} interface.
 */
public class DomsEventStorage<T extends Item> implements EventStorer<T> {

    private static Logger log = LoggerFactory.getLogger(DomsEventStorage.class);

    protected final EnhancedFedora fedora;
    protected final String eventsDatastream;
    protected final PremisManipulatorFactory<T> premisFactory;
    private String addEventToItemComment = "Adding event to Item";
    private String removeEventFromItemComment = "Removing event from item: ";

    DomsEventStorage(EnhancedFedora fedora, String type, String eventsDatastream, ItemFactory<T> itemFactory) throws JAXBException {
        this.fedora = fedora;
        this.eventsDatastream = eventsDatastream;
        premisFactory = new PremisManipulatorFactory<>(type, itemFactory);
    }



    @Override
    public Date appendEventToItem(T item, String agent, Date timestamp, String details, String eventType,
                               boolean outcome) throws CommunicationException, NotFoundException {
        PremisManipulator<T> premisObject = getPremisForItem(item);
        premisObject = premisObject.appendEvent(agent, timestamp, details, eventType, outcome);
        try{
            try {
                return fedora.modifyDatastreamByValue(item.getDomsID(),
                        eventsDatastream,
                        null,
                        null,
                        premisObject.toXML().getBytes(),
                        null,
                        "text/xml",
                        addEventToItemComment,
                        null);
            } catch (ConcurrentModificationException | BackendMethodFailedException | BackendInvalidCredsException e) {
                throw new CommunicationException("Failed appending event to item '" + item + "'", e);    
            }
        } catch (BackendInvalidResourceException e1) {
            //But I just created the object, it must be there
            throw new NotFoundException("Failed appending event to item '" + item + "'", e1);
        }
    }

    @Override
    public Date prependEventToItem(T item, String agent, Date timestamp, String details, String eventType,
                               boolean outcome) throws CommunicationException, NotFoundException {
        PremisManipulator<T> premisObject = getPremisForItem(item);

        premisObject = premisObject.prependEvent(agent, timestamp, details, eventType, outcome);
        try {
            try {
                return fedora.modifyDatastreamByValue(item.getDomsID(),
                        eventsDatastream,
                        null,
                        null,
                        premisObject.toXML().getBytes(),
                        null,
                        "text/xml",
                        addEventToItemComment,
                        null);
            } catch (ConcurrentModificationException | BackendMethodFailedException | BackendInvalidCredsException e) {
                throw new CommunicationException("Failed prepending event to item '" + item + "'", e); 
            }
        } catch (BackendInvalidResourceException e1) {
            //But I just created the object, it must be there
            throw new NotFoundException("Failed prepending event to item '" + item + "'", e1);
        }
    }
    
    private PremisManipulator<T> getPremisForItem(T item) throws CommunicationException, NotFoundException {
        try {
            String itemID = item.getDomsID();
            if (itemID == null){
                itemID = getPidFromDCIdentifier(item.getFullID());
                item.setDomsID(itemID);
            }
            PremisManipulator<T> premisObject;
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(itemID, eventsDatastream, null);

                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) {
                //okay, no EVENTS datastream
                premisObject = premisFactory.createInitialPremisBlob(item.getFullID());
            }
            return premisObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }
    }
    
    /**
     * Removes all instances of events with the given type from the item
     * @param item        The item to remove events from
     * @param eventType     The eventType to remove
     *
     * @return the number of events removed
     * @throws CommunicationException
     * @throws NotFoundException
     */
    @Override
    public int removeEventFromItem(T item,  String eventType) throws
                                                             CommunicationException,
                                                             NotFoundException {
        try {
            String itemID = item.getDomsID();
            if (itemID == null) {
                itemID = getPidFromDCIdentifier(item.getFullID());
            }
            PremisManipulator<T> premisObject;
            String premisPreBlob = fedora.getXMLDatastreamContents(itemID, eventsDatastream, null);
            premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));

            int removed = premisObject.removeEvents(eventType);
            if (removed > 0) {
                fedora.modifyDatastreamByValue(itemID,
                                                      eventsDatastream,
                                                      null,
                                                      null,
                                                      premisObject.toXML().getBytes(),
                                                      null,
                                                      "text/xml",
                                                      removeEventFromItemComment + eventType,
                                                      null);

            }
            return removed;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        } catch (BackendInvalidResourceException e){
            throw new NotFoundException(e);
        }
    }


    public T getItemFromFullID(String itemFullID) throws CommunicationException, NotFoundException {
        String roundTripID = getPidFromDCIdentifier(itemFullID);
        return getItemFromDomsID(roundTripID);
    }

    /**
     * Retrieve a batch
     *
     * @param domsId the id of the round trip object in doms
     *
     * @return the batch
     * @throws NotFoundException      if the batch is not found
     * @throws CommunicationException if communication with doms failed
     */
    public T getItemFromDomsID(String domsId) throws CommunicationException, NotFoundException {
        PremisManipulator<T> premisObject;
        try {
            try {
                String premisPreBlob = fedora.getXMLDatastreamContents(domsId, eventsDatastream, null);
                premisObject = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            } catch (BackendInvalidResourceException e) { //This could be a missing datastream or object
                try {
                    ObjectProfile profile = fedora.getObjectProfile(domsId, null); //Get profile to check that the obejct is there
                    premisObject = premisFactory.createInitialPremisBlob(domsId); //Okay, an object, create an empty premis block
                } catch (BackendInvalidResourceException e1) { //Not event the object
                    throw new NotFoundException(e1);
                }
            }
            T item = premisObject.toItem();
            item.setDomsID(domsId);
            return item;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | JAXBException e) {
            throw new CommunicationException(e);
        }

    }

    @Override
    public int triggerWorkflowRestartFromFirstFailure(T item) throws CommunicationException, NotFoundException {
        return triggerWorkflowRestartFromFirstFailure(item, null);
    }

    /**
     * This method carries out a single attempt to restart the workflow from where it first failed.
     *
     * @param eventId         the first event to remove or null if all events after the first failure are to be
     *                        removed.
     *
     * @return the number of events removed or -1 of there was a ConcurrentModificationException thrown.
     * @throws CommunicationException if there was a problem communicating with DOMS.
     */
    @Override
    public int triggerWorkflowRestartFromFirstFailure(T item, String eventId) throws
                                                                              CommunicationException,
                                                                              NotFoundException {
        String itemPid = item.getDomsID();
        if (itemPid == null) {
            itemPid = getPidFromDCIdentifier(item.getFullID());
        }
        try {
            Date lastModifiedDate = fedora.getObjectProfile(itemPid, null).getObjectLastModifiedDate();
            String premisPreBlob = fedora.getXMLDatastreamContents(itemPid, eventsDatastream, null);
            PremisManipulator<T> premisObject
                    = premisFactory.createFromBlob(new ByteArrayInputStream(premisPreBlob.getBytes()));
            int eventsRemoved = premisObject.removeEventsFromFailureOrEvent(eventId);
            if (eventsRemoved > 0) {
                //backupEventsForBatch(batchId, roundTripNumber);
                try {
                    fedora.modifyDatastreamByValue(
                            itemPid,
                            eventsDatastream,
                            null,
                            null,
                            premisObject.toXML().getBytes("UTF-8"),
                            null,
                            "text/xml",
                            "Event list trimmed of all events after earliest failure",
                            lastModifiedDate.getTime());
                } catch (ConcurrentModificationException e) {
                    log.warn(
                            "Failed to trigger restart of batch round trip for " + item.getFullID() +
                            " on this attempt. Another process modified the object concurrently."
                            );
                    return -1;
                } catch (UnsupportedEncodingException e) {
                    throw new Error("UTF-8 not supported.", e);
                }
            }
            return eventsRemoved;
        } catch (BackendInvalidResourceException | JAXBException | BackendInvalidCredsException | BackendMethodFailedException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Retrieve the corresponding doms pid of the object with this dc identifier
     *
     * @return the doms pid
     * @throws CommunicationException          failed to communicate
     * @throws BackendInvalidResourceException object not found
     */
    String getPidFromDCIdentifier(String fullID) throws
                                                               CommunicationException,
                                                               NotFoundException {

        try {
            final String dcIdentifier = toDCIdentifier(fullID);
            List<String> founds = fedora.findObjectFromDCIdentifier(dcIdentifier);
            if (founds.size() > 0) {
                return founds.get(0);
            }
            throw new NotFoundException("Doms Object not found for dc identifier " + dcIdentifier);
        } catch (BackendMethodFailedException | BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }
    }

    public static String toDCIdentifier(String fullID) {
        if (!fullID.startsWith("path:")) {
            return String.format("path:%s", fullID);
        }
        return fullID;
    }

}
