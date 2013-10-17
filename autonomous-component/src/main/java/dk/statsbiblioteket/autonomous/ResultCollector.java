package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.autonomous.autonomous.Details;
import dk.statsbiblioteket.autonomous.autonomous.Failure;
import dk.statsbiblioteket.autonomous.autonomous.Failures;
import dk.statsbiblioteket.autonomous.autonomous.ObjectFactory;
import dk.statsbiblioteket.autonomous.autonomous.Result;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * This class collects the result of a run of a component.
 */
public class ResultCollector {

    private Result resultStructure;

    public ResultCollector(String tool,
                           String version) {
        resultStructure = new ObjectFactory().createResult();
        setSuccess(true);
        resultStructure.setFailures(new Failures());
        resultStructure.setTool(tool);
        resultStructure.setVersion(version);
        setTimestamp(new Date());
    }

    /**
     * Get the success value of the execution
     *
     * @return the success
     */
    public boolean isSuccess() {
        return "Success".equals(resultStructure.getOutcome());
    }

    /**
     * Set the success Value of the execution
     *
     * @param success the sucesss
     */
    private void setSuccess(boolean success) {
        resultStructure.setOutcome(success ? "Success" : "Failure");
    }

    /**
     * Add a specific failure to the result collector. All these parameters must be non-null and non-empty
     *
     * @param reference   the reference to the file/object that caused the failure
     * @param type        the type of failure
     * @param component   the component that failed
     * @param description Description of the failure.
     */
    public void addFailure(String reference,
                           String type,
                           String component,
                           String description) {
        addFailure(reference, type, component, description, new String[]{});
    }

    /**
     * Add a specific failure to the result collector. All these parameters must be non-null and non-empty
     *
     * @param reference   the reference to the file/object that caused the failure
     * @param type        the type of failure
     * @param component   the component that failed
     * @param description Description of the failure.
     * @param details     additional details, can be null
     */
    public void addFailure(String reference,
                           String type,
                           String component,
                           String description,
                           String... details) {
        List<Failure> list = resultStructure.getFailures().getFailure();
        Failure failure = new Failure();
        failure.setFilereference(reference);
        failure.setType(type);
        failure.setComponent(component);
        failure.setDescription(description);
        if (details != null) {
            Details xmlDetails = new Details();
            xmlDetails.getContent().addAll(Arrays.asList(details));
        }
        list.add(failure);
        setSuccess(false);
    }

    /**
     * Return the report as xml
     */
    public String toReport() {
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(resultStructure, writer);
            return writer.toString();
        } catch (JAXBException e) {
            return null;
        }
    }

    /**
     * The timestamp of the event
     *
     * @return
     */
    public Date getTimestamp() {
        return resultStructure.getDate().toGregorianCalendar().getTime();
    }

    /**
     * Timestamp the event that this is the result of
     *
     * @param timestamp
     */
    public void setTimestamp(Date timestamp) {
        resultStructure.setDate(format(timestamp));

    }

    private XMLGregorianCalendar format(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar date2 = null;
        try {
            date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            throw new Error(e);
        }
        return date2;
    }
}
