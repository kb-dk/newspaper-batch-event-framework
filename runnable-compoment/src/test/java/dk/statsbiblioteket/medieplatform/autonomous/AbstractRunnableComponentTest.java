package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.util.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AbstractRunnableComponentTest {
    public static final String BATCH_TEMPLATE = "doms:Template_Batch";
    public static final String ROUND_TRIP_TEMPLATE = "doms:Template_RoundTrip";
    public static final String HAS_PART = "info:fedora/fedora-system:def/relations-external#hasPart";

    @Test
    public void testBatchStructureFromFileSystem() throws Exception {
        Properties properties = new Properties(System.getProperties());

        File temp = createTempDir();
        properties.setProperty("batchStructure.storageDir", temp.getAbsolutePath());
        TestingRunnableComponent component = new TestingRunnableComponent(properties);

        String testData = "<test>hej, this is test data</test>";

        Batch batch = new Batch("5000");
        component.storeBatchStructure(batch, new ByteArrayInputStream(testData.getBytes()));
        InputStream retrieved = component.retrieveBatchStructure(batch);
        String retrievedString = toString(retrieved);
        Assert.assertEquals(testData, retrievedString);
    }

    private String toString(InputStream retrieved) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Streams.pipe(retrieved, out);
        return out.toString();
    }

    private File createTempDir() throws IOException {
        File temp = File.createTempFile("folder-name", "");
        temp.delete();
        temp.mkdir();
        return temp;
    }

    @Test(groups = "integrationTest")
    public void testBatchStructureFromDoms() throws Exception {
        System.out.println("Testing batch structure storing vs. DOMS");
        Properties properties = new Properties(System.getProperties());
        properties.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));

        properties.setProperty("batchStructure.useFileSystem", Boolean.FALSE.toString());
        TestingRunnableComponent component = new TestingRunnableComponent(properties);


        Batch batch = new Batch("5000");
        EnhancedFedora enhancedFedora = getEnhancedFedora(properties);

        String testData = "<test>hej, this is test data</test>";

        createBatchRoundTrip(batch, enhancedFedora);

        component.storeBatchStructure(batch, new ByteArrayInputStream(testData.getBytes()));
        InputStream retrieved = component.retrieveBatchStructure(batch);
        String retrievedString = toString(retrieved);
        Assert.assertEquals(testData, retrievedString);
    }

    /**
     * Utility method to initialise an enhanced fedora object
     *
     * @return the enhanced fedora object
     * @throws java.net.MalformedURLException if the URL in "fedora.server" is invalid
     * @throws PIDGeneratorException          if the pid generator webservice choked again. Should not be possible
     * @throws javax.xml.bind.JAXBException   if jaxb fails to understand the wsdl
     */
    private EnhancedFedora getEnhancedFedora(Properties properties) throws
                                                                    MalformedURLException,
                                                                    PIDGeneratorException,
                                                                    JAXBException {
        return new EnhancedFedoraImpl(new Credentials(properties.getProperty("fedora.admin.username"),
                                                      properties.getProperty("fedora.admin.password")),
                                      properties.getProperty("fedora.server").replaceFirst("/(objects)?/?$", ""),
                                      properties.getProperty("pidgenerator.location"),
                                      null);
    }

    public String createBatchRoundTrip(Batch batch,
                                       EnhancedFedora fedora) throws CommunicationException {
        try {
            try {
                //find the roundTrip Object
                return getRoundTripID(batch, fedora);
            } catch (BackendInvalidResourceException e) {
                //no roundTripObject, so sad
                //but alas, we can continue
            }

            //find the batch object
            String batchObject;


            List<String> founds = fedora.listObjectsWithThisLabel(batch.getFullID());
            String createBatchRoundTripComment = "";
            if (founds.size() > 0) {
                batchObject = founds.get(0);
            } else {
                //no batch object either, more sad
                //create it, then
                batchObject = fedora.cloneTemplate(BATCH_TEMPLATE,
                                                   Arrays.asList("path:"+batch.getFullID()),
                                                   createBatchRoundTripComment);
                fedora.modifyObjectLabel(batchObject, batch.getFullID(), createBatchRoundTripComment);
            }
            String roundTripObject;

            roundTripObject = fedora.cloneTemplate(ROUND_TRIP_TEMPLATE,
                                                   Arrays.asList("path:"+batch.getFullID()),
                                                   createBatchRoundTripComment);


            //set label
            fedora.modifyObjectLabel(roundTripObject, batch.getFullID(), createBatchRoundTripComment);


            //connect batch object to round trip object
            fedora.addRelation(batchObject,
                               toFedoraID(batchObject),
                               HAS_PART,
                               toFedoraID(roundTripObject),
                               false,
                               createBatchRoundTripComment);

            //create the initial EVENTS datastream


            return roundTripObject;
        } catch (BackendMethodFailedException | BackendInvalidCredsException | PIDGeneratorException |
                BackendInvalidResourceException | ObjectIsWrongTypeException e) {
            throw new CommunicationException(e);
        }


    }

    /**
     * Retrieve the corresponding doms pid of the round trip object
     *
     * @param fedora@return the doms round trip pid
     *
     * @throws CommunicationException failed to communicate
     * @throws BackendInvalidResourceException
     *                                object not found
     */
    private String getRoundTripID(Batch batch,
                                  EnhancedFedora fedora) throws
                                                         CommunicationException,
                                                         BackendInvalidResourceException {

        try {
            //find the Round Trip object
            List<String> founds = fedora.findObjectFromDCIdentifier("path:"+batch.getFullID());
            if (founds.size() > 0) {
                return founds.get(0);
            }
            throw new BackendInvalidResourceException("Round Trip object not found");
        } catch (BackendMethodFailedException | BackendInvalidCredsException e) {
            throw new CommunicationException(e);
        }
    }

    /**
     * Append "info:fedora/" to the fedora pid if needed
     *
     * @param fedoraPid the fedora pid
     *
     * @return duh
     */
    private String toFedoraID(String fedoraPid) {
        if (!fedoraPid.startsWith("info:fedora/")) {
            return "info:fedora/" + fedoraPid;
        }
        return fedoraPid;
    }
}