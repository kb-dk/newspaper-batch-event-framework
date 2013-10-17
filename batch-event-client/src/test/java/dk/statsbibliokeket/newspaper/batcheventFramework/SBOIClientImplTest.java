package dk.statsbibliokeket.newspaper.batcheventFramework;

import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClient;
import dk.statsbiblioteket.medieplatform.autonomous.DomsEventClientFactory;
import dk.statsbiblioteket.medieplatform.autonomous.NewspaperIDFormatter;
import dk.statsbiblioteket.medieplatform.autonomous.PremisManipulatorFactory;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public class SBOIClientImplTest {

    @Test(groups = "integrationTest",enabled = true)
    public void testGetBatches() throws Exception {

        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ITtest.properties"));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty("fedora.location"));
        factory.setUsername(props.getProperty("fedora.username"));
        factory.setPassword(props.getProperty("fedora.password"));
        factory.setPidGeneratorLocation(props.getProperty("pidgenerator.location"));


        DomsEventClient doms = factory.createDomsEventClient();

        SBOIClientImpl summa = new SBOIClientImpl(props.getProperty("sboi.summa"), new PremisManipulatorFactory(new NewspaperIDFormatter(),
                                                                     PremisManipulatorFactory.TYPE));
        Iterator<Batch> batches = summa.getBatches(
                Arrays.asList("Data_Received"),
                new ArrayList<String>(),
                Arrays.asList("Approved"));
        int count = 0;
        while (batches.hasNext()) {
            Batch next = batches.next();
            count++;
        }
        Assert.assertTrue( count > 0,"No batches Found");

    }
}
