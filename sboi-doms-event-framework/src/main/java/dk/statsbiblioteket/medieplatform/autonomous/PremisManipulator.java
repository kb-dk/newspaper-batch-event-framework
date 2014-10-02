package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.autonomous.premis.AgentComplexType;
import dk.statsbiblioteket.autonomous.premis.AgentIdentifierComplexType;
import dk.statsbiblioteket.autonomous.premis.EventComplexType;
import dk.statsbiblioteket.autonomous.premis.EventIdentifierComplexType;
import dk.statsbiblioteket.autonomous.premis.EventOutcomeDetailComplexType;
import dk.statsbiblioteket.autonomous.premis.EventOutcomeInformationComplexType;
import dk.statsbiblioteket.autonomous.premis.LinkingAgentIdentifierComplexType;
import dk.statsbiblioteket.autonomous.premis.LinkingObjectIdentifierComplexType;
import dk.statsbiblioteket.autonomous.premis.ObjectComplexType;
import dk.statsbiblioteket.autonomous.premis.ObjectFactory;
import dk.statsbiblioteket.autonomous.premis.ObjectIdentifierComplexType;
import dk.statsbiblioteket.autonomous.premis.PremisComplexType;
import dk.statsbiblioteket.autonomous.premis.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for transforming the premis structure. Contains methods for creating the premis from scratch, and for adding
 * events.
 * The class is not thread safe, so do not use it as such
 */
public class PremisManipulator {

    private final static QName _EventOutcome_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcome");
    private final static QName _EventOutcomeDetailNote_QNAME = new QName(
            "info:lc/xmlns/premis-v2", "eventOutcomeDetailNote");
    private final static QName _EventOutcomeDetail_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcomeDetail");
    //TODO logging in all the methods
    private static Logger log = LoggerFactory.getLogger(PremisManipulator.class);
    private final PremisComplexType premis;
    private Marshaller marshaller;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final SimpleDateFormat legacyDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
    private final String type;
    private final IDFormatter idFormat;


    public PremisManipulator(InputStream premis, IDFormatter format, String type, JAXBContext context) throws JAXBException {
        this.idFormat = format;
        this.type = type;
        this.marshaller = createMarshaller(context);
        this.premis = ((JAXBElement<PremisComplexType>) context.createUnmarshaller().unmarshal(premis)).getValue();
    }

    private static Marshaller createMarshaller(JAXBContext context) throws JAXBException {
        Marshaller temp = context.createMarshaller();
        temp.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        return temp;
    }

    public PremisManipulator(String itemID, IDFormatter format, String type, JAXBContext context) throws
                                                                                                                        JAXBException {
        this.marshaller = createMarshaller(context);
        premis = new ObjectFactory().createPremisComplexType();
        premis.setVersion("2.2");
        this.idFormat = format;
        this.type = type;
        addObjectIfNessesary(premis.getObject(), itemID);
    }

    /**
     * Make this Premis as a Batch. The Event class is poorer than the Premis events, so the "agent" information
     * will not be included
     *
     * @return the blob as a Batch
     */
    public Item toItem() {

        String fullID = getObjectID();
        IDFormatter.SplitID splits = idFormat.unformatFullID(fullID);
        Batch result = new Batch(splits.getBatchID());
        result.setRoundTripNumber(splits.getRoundTripNumber());
        result.setEventList(getEvents());
        return result;
    }

    /**
     * Get the id of the object object in premis
     *
     * @return the object
     */
    private String getObjectID() {
        Representation object = (Representation) premis.getObject().get(0);
        return object.getObjectIdentifier().get(0).getObjectIdentifierValue();
    }

    /**
     * convert the events to a list of Events
     *
     * @return the Events
     */
    private List<Event> getEvents() {
        List<EventComplexType> premisEvents = premis.getEvent();
        List<Event> result = new ArrayList<>(premisEvents.size());
        for (EventComplexType premisEvent : premisEvents) {
            result.add(convert(premisEvent));
        }
        return result;
    }

    /**
     * Convert one event to a Event
     *
     * @param premisEvent the event
     *
     * @return the Event
     */
    private Event convert(EventComplexType premisEvent) {
        Event result = new Event();
        result.setEventID(premisEvent.getEventType());
        result.setDetails(premisEvent.getEventDetail());
        try {
            result.setDate(dateFormat.parse(premisEvent.getEventDateTime()));
        } catch (ParseException e) {
            try {
                result.setDate(legacyDateFormat.parse(premisEvent.getEventDateTime()));
            } catch(ParseException e2){
                //no date is set, then
                log.warn("Failed to parse Premis event {} date {} (may not be set)",
                         result.getEventID(), premisEvent.getEventDateTime());
            }
        }
        EventOutcomeInformationComplexType eventOutcomeInformation = premisEvent.getEventOutcomeInformation().get(0);
        for (JAXBElement<?> jaxbElement : eventOutcomeInformation.getContent()) {
            if (jaxbElement.getName().equals(_EventOutcome_QNAME)) {
                String value = jaxbElement.getValue().toString();
                if (value.equals("success")) {
                    result.setSuccess(true);
                }
            }
            if (jaxbElement.getName().equals(_EventOutcomeDetail_QNAME)) {
                EventOutcomeDetailComplexType detailBlobs = (EventOutcomeDetailComplexType) jaxbElement.getValue();
                String details = detailBlobs.getEventOutcomeDetailNote();
                if (details != null) {
                    result.setDetails(details);
                }
            }
        }
        return result;
    }

    /**
     * Add an event to the premis blob. Will return itself to allow for method chaining.
     *
     * @param agent     the agent that did it
     * @param timestamp when the thing was done
     * @param details   details about how it went
     * @param eventType the kind of thing that was done
     * @param outcome   was it successful?
     *
     * @return the premis with the event added.
     */
    public PremisManipulator addEvent(String agent, Date timestamp, String details, String eventType, boolean outcome) {

        String eventID = getEventID(timestamp);
        if (eventExists(eventID)) {
            return this;
        }
        addAgentIfNessesary(premis.getAgent(), agent);

        ObjectFactory factory = new ObjectFactory();
        EventComplexType event = factory.createEventComplexType();

        event.setEventDateTime(dateFormat.format(timestamp));
        event.setEventType(eventType);


        EventIdentifierComplexType identifier = factory.createEventIdentifierComplexType();
        identifier.setEventIdentifierType(type);

        identifier.setEventIdentifierValue(eventID);
        event.setEventIdentifier(identifier);

        EventOutcomeInformationComplexType outcomeObject = factory.createEventOutcomeInformationComplexType();
        String outcomeString = (outcome ? "success" : "failure");
        outcomeObject.getContent().add(factory.createEventOutcome(outcomeString));
        EventOutcomeDetailComplexType eventOutcomeDetail = factory.createEventOutcomeDetailComplexType();
        eventOutcomeDetail.setEventOutcomeDetailNote(details);
        outcomeObject.getContent().add(factory.createEventOutcomeDetail(eventOutcomeDetail));

        event.getEventOutcomeInformation().add(outcomeObject);

        LinkingAgentIdentifierComplexType linkingAgentObject = factory.createLinkingAgentIdentifierComplexType();
        linkingAgentObject.setLinkingAgentIdentifierType(type);
        linkingAgentObject.setLinkingAgentIdentifierValue(agent);
        event.getLinkingAgentIdentifier().add(linkingAgentObject);

        LinkingObjectIdentifierComplexType linkingObjectObject = factory.createLinkingObjectIdentifierComplexType();
        linkingObjectObject.setLinkingObjectIdentifierType(type);
        linkingObjectObject.setLinkingObjectIdentifierValue(getObjectID());
        event.getLinkingObjectIdentifier().add(linkingObjectObject);

        premis.getEvent().add(event);
        return this;

    }


    /**
     * Remove all events from the PREMIS blob with date time later-than-or-equal to the earliest failure. This method
     * does not remove the corresponding agent (if any) from the PREMIS blob so there could be agents left which have
     * no referrent. There is no reason to believe that this is a problem.
     *
     * @param eventId the earliest event to remove. If null, find the earliest failure instead.
     *
     * @return the number of events removed, or the number which would have been removed if doit=false
     */
    public int removeEventsFromFailureOrEvent(String eventId) {
        List<EventComplexType> premisEvents = premis.getEvent();
        List<EventComplexType> eventsToRemove = findEventsToRemove(premisEvents, eventId);
        premisEvents.removeAll(eventsToRemove);
        return eventsToRemove.size();
    }

    /**
     * Find events to remove.
     *
     * @param premisEvents the complete events list.
     * @param eventId      the earliest event to remove. If null, find the earliest failure instead.
     *
     * @return the list of events to remove.
     */
    private List<EventComplexType> findEventsToRemove(List<EventComplexType> premisEvents, String eventId) {
        Date earliestEventToRemove = null;
        if (eventId == null) {
            earliestEventToRemove = findDateOfEarliestFailure(premisEvents);
        } else {
            earliestEventToRemove = findDateOfGivenEvent(premisEvents, eventId);
        }
        List<EventComplexType> eventsToRemove = new ArrayList<EventComplexType>();
        if (earliestEventToRemove != null) {
            for (EventComplexType premisEvent : premisEvents) {
                Event event = convert(premisEvent);
                if (event.getDate().compareTo(earliestEventToRemove) >= 0) {
                    eventsToRemove.add(premisEvent);
                }
            }
        }
        return eventsToRemove;
    }

    private Date findDateOfEarliestFailure(List<EventComplexType> premisEvents) {
        Date earliestFailure = null;
        for (EventComplexType premisEvent : premisEvents) {
            Event event = convert(premisEvent);
            if (!event.isSuccess()) {
                if (earliestFailure == null || event.getDate().before(earliestFailure)) {
                    earliestFailure = event.getDate();
                }
            }
        }
        return earliestFailure;
    }

    private Date findDateOfGivenEvent(List<EventComplexType> premisEvents, String eventId) {
        for (EventComplexType premisEvent : premisEvents) {
            Event event = convert(premisEvent);
            if (event.getEventID().equals(eventId)) {
                return event.getDate();
            }
        }
        return null;
    }


    /**
     * Returns true, if an event with the same identifier in the same type exists
     *
     * @param eventID the event id to search for
     *
     * @return true if found
     */
    private boolean eventExists(String eventID) {
        for (EventComplexType event : premis.getEvent()) {
            if (event.getEventIdentifier().getEventIdentifierType().equals(type) && event.getEventIdentifier()
                                                                                         .getEventIdentifierValue()
                                                                                         .equals(eventID)) {
                return true;
            }
        }
        return false;
    }

    private String getEventID(Date timestamp) {
        return getObjectID() + "-" + String.valueOf(timestamp.getTime());
    }

    /**
     * Get the premis as xml
     *
     * @return the premis as xml
     */
    public String toXML() {
        try {
            StringWriter writer = new StringWriter();
            marshaller.marshal(new ObjectFactory().createPremis(premis), writer);
            return writer.toString();
        } catch (JAXBException e) {
            log.error("Failed to serialize premis as Xml",e);
            return null;
        }
    }

    /**
     * Add the objectList
     *
     * @param objectList the list to add it to
     * @param fullID     the full id of the objectList
     */
    private void addObjectIfNessesary(List<ObjectComplexType> objectList, String fullID) {
        if (objectList.size() == 0) {
            ObjectFactory factory = new ObjectFactory();
            Representation representation = factory.createRepresentation();
            ObjectIdentifierComplexType objectIdentifier = factory.createObjectIdentifierComplexType();
            objectIdentifier.setObjectIdentifierType(type);
            objectIdentifier.setObjectIdentifierValue(fullID);
            representation.getObjectIdentifier().add(objectIdentifier);
            objectList.add(representation);
        }
    }

    /**
     * Add the agentList, if it is not there already
     *
     * @param agentList the agent list to add the agent to
     * @param agent1    the agent name
     */
    private void addAgentIfNessesary(List<AgentComplexType> agentList, String agent1) {
        ObjectFactory factory = new ObjectFactory();
        for (AgentComplexType agentComplexType : agentList) {
            for (AgentIdentifierComplexType agentIdentifierComplexType : agentComplexType.getAgentIdentifier()) {
                if (agentIdentifierComplexType.getAgentIdentifierValue().equals(agent1)) {
                    return;
                }
            }

        }
        AgentIdentifierComplexType identifier = factory.createAgentIdentifierComplexType();
        identifier.setAgentIdentifierValue(agent1);
        identifier.setAgentIdentifierType(type);

        AgentComplexType agentCreated = factory.createAgentComplexType();
        agentCreated.getAgentIdentifier().add(identifier);
        agentList.add(agentCreated);
    }
}
