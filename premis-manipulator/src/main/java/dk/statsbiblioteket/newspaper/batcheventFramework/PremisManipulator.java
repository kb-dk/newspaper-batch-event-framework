package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.premis.AgentComplexType;
import dk.statsbiblioteket.newspaper.premis.AgentIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.EventComplexType;
import dk.statsbiblioteket.newspaper.premis.EventIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.EventOutcomeDetailComplexType;
import dk.statsbiblioteket.newspaper.premis.EventOutcomeInformationComplexType;
import dk.statsbiblioteket.newspaper.premis.LinkingAgentIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.LinkingObjectIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.ObjectComplexType;
import dk.statsbiblioteket.newspaper.premis.ObjectFactory;
import dk.statsbiblioteket.newspaper.premis.ObjectIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.PremisComplexType;
import dk.statsbiblioteket.newspaper.premis.Representation;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Event;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

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

//TODO javadoc
//Document not thread safe
public class PremisManipulator {

    private final static QName _EventOutcome_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcome");
    private final static QName _EventOutcomeDetailNote_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcomeDetailNote");
    private final static QName _EventOutcomeDetail_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcomeDetail");


    private final PremisComplexType premis;
    private final JAXBContext context;


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");
    private final String type;
    private final IDFormatter idFormat;

    PremisManipulator(Long batchID, int runNr, IDFormatter idFormat, String type) throws JAXBException {
        premis = new ObjectFactory().createPremisComplexType();
        premis.setVersion("2.2");
        this.idFormat = idFormat;
        this.type = type;
        context = JAXBContext.newInstance(ObjectFactory.class);
        addObjectIfNessesary(premis.getObject(), idFormat.formatFullID(batchID, runNr));

    }

    PremisManipulator(InputStream premis, IDFormatter idFormat, String type) throws JAXBException {
        this.idFormat = idFormat;
        this.type = type;
        context = JAXBContext.newInstance(ObjectFactory.class);
        this.premis = ((JAXBElement<PremisComplexType>) context.createUnmarshaller().unmarshal(premis)).getValue();
    }


    /**
     * Make this Premis as a Batch. Know that some things get lost here
     * @return the blob as a Batch
     */
    public Batch toBatch() {
        Batch result = new Batch();
        String fullID = getObjectID();
        IDFormatter.SplitID splits = idFormat.unformatFullID(fullID);
        result.setBatchID(splits.getBatchID());
        result.setRunNr(splits.getRunNr());
        result.setEventList(getEvents());
        return result;
    }


    /**
     * Get the id of the object object in premis
     * @return the object
     */
    private String getObjectID() {
        Representation object = (Representation) premis.getObject().get(0);
        return object.getObjectIdentifier().get(0).getObjectIdentifierValue();
    }

    /**
     * convert the events to a list of Events
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
     * @param premisEvent the event
     * @return the Event
     */
    private Event convert(EventComplexType premisEvent) {
        Event result = new Event();
        result.setEventID(EventID.valueOf(premisEvent.getEventType()));
        result.setDetails(premisEvent.getEventDetail());
        try {
            result.setDate(dateFormat.parse(premisEvent.getEventDateTime()));
        } catch (ParseException e) {
            //log this, no date is set, then
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
     * Add an event to the premis blob. Not thread safe. Will return the premis manipulator, but the premis manipulator
     * will have been modified. Do not think this class will clone and return.
     * @param agent the agent that did it
     * @param timestamp when the thing was done
     * @param details details about how it went
     * @param eventType the kind of thing that was done
     * @param outcome was it successful?
     * @return the premis with the event added.
     */
    public PremisManipulator addEvent(String agent,
                                      Date timestamp,
                                      String details,
                                      EventID eventType,
                                      boolean outcome) {

        addAgentIfNessesary(premis.getAgent(), agent);

        ObjectFactory factory = new ObjectFactory();
        EventComplexType event = factory.createEventComplexType();

        event.setEventDateTime(dateFormat.format(timestamp));
        event.setEventType(eventType.toString());


        EventIdentifierComplexType identifier = factory.createEventIdentifierComplexType();
        identifier.setEventIdentifierType(type);
        identifier.setEventIdentifierValue(getObjectID() + "-" + String.valueOf(timestamp.getTime()));
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
     * Get the premis as xml
     * @return the premis as xml
     */
    public String toXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(new ObjectFactory().createPremis(premis), writer);
            return writer.toString();
        } catch (JAXBException e) {
            return null;
        }
    }

    /**
     * Add the objectList
     * @param objectList the list to add it to
     * @param fullID the full id of the objectList
     */
    private void addObjectIfNessesary(List<ObjectComplexType> objectList, String fullID) {
        if (objectList.size() == 0){
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
     * @param agentList the agent list to add the agent to
     * @param agent1 the agent name
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
