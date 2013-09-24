package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.premis.AgentComplexType;
import dk.statsbiblioteket.newspaper.premis.AgentIdentifierComplexType;
import dk.statsbiblioteket.newspaper.premis.EventComplexType;
import dk.statsbiblioteket.newspaper.premis.EventIdentifierComplexType;
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

public class PremisManipulator {

    private final static QName _EventOutcome_QNAME = new QName("info:lc/xmlns/premis-v2", "eventOutcome");

    private final static String TYPE="Newspaper_digitisation_project";
    private final PremisComplexType premis;
    private final JAXBContext context;
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ");


    public PremisManipulator(PremisComplexType premis) throws JAXBException {
        this.premis = premis;
        context = JAXBContext.newInstance(ObjectFactory.class);
    }

    public PremisManipulator(InputStream premis) throws JAXBException {
        context = JAXBContext.newInstance(ObjectFactory.class);
        this.premis = ((JAXBElement< PremisComplexType >) context.createUnmarshaller().unmarshal(premis)).getValue();
    }




    public static PremisManipulator createFromBlob(InputStream blob) throws JAXBException {
        PremisManipulator that = new PremisManipulator(blob);
        return that;
    }


    public static PremisManipulator createInitialPremisBlob(String BatchID) throws JAXBException {
        PremisComplexType premis = new ObjectFactory().createPremisComplexType();
        premis.setVersion("2.2");
        PremisManipulator that = new PremisManipulator(premis);
        that.addObject(premis.getObject(), BatchID);
        return that;
    }


    public Batch asBatch(){
        Batch result = new Batch();
        String fullID = getBatchID();
        String[] splits = fullID.replaceAll("^B", "").replaceAll("RT", "").split("-");
        result.setBatchID(splits[0]);
        result.setRunNr(Integer.parseInt(splits[1]));
        result.setEventList(getEvents());
        return result;
    }

    public String getBatchID(){
        Representation object = (Representation) premis.getObject().get(0);
        return object.getObjectIdentifier().get(0).getObjectIdentifierValue();
    }

    public List<Event> getEvents(){
        List<EventComplexType> premisEvents = premis.getEvent();
        List<Event> result = new ArrayList<>(premisEvents.size());
        for (EventComplexType premisEvent : premisEvents) {
            result.add(convert(premisEvent));
        }
        return result;

    }

    private Event convert(EventComplexType premisEvent) {
        Event result = new Event();
        result.setEventID(EventID.valueOf(premisEvent.getEventType()));
        result.setDetails(premisEvent.getEventDetail());
        try {
            result.setDate(format.parse(premisEvent.getEventDateTime()));
        } catch (ParseException e) {
            //log this, no date is set, then
        }
        EventOutcomeInformationComplexType eventOutcomeInformation = premisEvent.getEventOutcomeInformation().get(0);
        for (JAXBElement<?> jaxbElement : eventOutcomeInformation.getContent()) {
            if (jaxbElement.getName().equals(_EventOutcome_QNAME)){
                String value = jaxbElement.getValue().toString();
                if (value.equals("success")){
                    result.setSuccess(true);
                }
            }
        }
        return result;
    }

    public PremisManipulator addEvent(String agent,
                                      Date timestamp,
                                      String details,
                                      EventID eventType,
                                      boolean outcome){

        addAgentIfNessesary(premis.getAgent(), agent);

        ObjectFactory factory = new ObjectFactory();
        EventComplexType event = factory.createEventComplexType();

        event.setEventDateTime(format.format(timestamp));
        event.setEventDetail(details);
        event.setEventType(eventType.toString());


        EventIdentifierComplexType identifier = factory.createEventIdentifierComplexType();
        identifier.setEventIdentifierType(TYPE);
        identifier.setEventIdentifierValue(String.valueOf(timestamp.getTime()));
        event.setEventIdentifier(identifier);

        EventOutcomeInformationComplexType outcomeObject = factory.createEventOutcomeInformationComplexType();
        String outcomeString = (outcome ? "success" : "failure");
        outcomeObject.getContent().add(factory.createEventOutcome(outcomeString));
        event.getEventOutcomeInformation().add(outcomeObject);

        LinkingAgentIdentifierComplexType linkingAgentObject = factory.createLinkingAgentIdentifierComplexType();
        linkingAgentObject.setLinkingAgentIdentifierType(TYPE);
        linkingAgentObject.setLinkingAgentIdentifierValue(agent);
        event.getLinkingAgentIdentifier().add(linkingAgentObject);

        LinkingObjectIdentifierComplexType linkingObjectObject = factory.createLinkingObjectIdentifierComplexType();
        linkingObjectObject.setLinkingObjectIdentifierType(TYPE);
        linkingObjectObject.setLinkingObjectIdentifierValue(getBatchID());
        event.getLinkingObjectIdentifier().add(linkingObjectObject);

        premis.getEvent().add(event);
        return this;

    }

    public String toString(){
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(new ObjectFactory().createPremis(premis),writer);
            return writer.toString();
        } catch (JAXBException e) {
            return null;
        }
    }

    private void addObject(List<ObjectComplexType> object, String batchID) {
        ObjectFactory factory = new ObjectFactory();
        Representation representation = factory.createRepresentation();
        ObjectIdentifierComplexType objectIdentifier = factory.createObjectIdentifierComplexType();
        objectIdentifier.setObjectIdentifierType(TYPE);
        objectIdentifier.setObjectIdentifierValue(batchID);
        representation.getObjectIdentifier().add(objectIdentifier);
        object.add(representation);
    }

    private void addAgentIfNessesary(List<AgentComplexType> agent, String agent1) {
        ObjectFactory factory = new ObjectFactory();
        for (AgentComplexType agentComplexType : agent) {
            for (AgentIdentifierComplexType agentIdentifierComplexType : agentComplexType.getAgentIdentifier()) {
                if (agentIdentifierComplexType.getAgentIdentifierValue().equals(agent1)){
                    return;
                }
            }

        }
        AgentIdentifierComplexType identifier = factory.createAgentIdentifierComplexType();
        identifier.setAgentIdentifierValue(agent1);
        identifier.setAgentIdentifierType(TYPE);

        AgentComplexType agentCreated = factory.createAgentComplexType();
        agentCreated.getAgentIdentifier().add(identifier);
        agent.add(agentCreated);
    }
}
