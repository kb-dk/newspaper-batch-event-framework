package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Properties;

public class DomsEventClientIntegrationTest {

    @Test(groups = "integrationTest")
    public void testAddEventToBatch1() throws Exception {
        Properties props = new Properties();
        props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ITtest.properties"));

        DomsEventClientFactory factory = new DomsEventClientFactory();
        factory.setFedoraLocation(props.getProperty("fedora.location").replaceFirst("/objects/",""));
        factory.setUsername(props.getProperty("fedora.username"));
        factory.setPassword(props.getProperty("fedora.password"));
        factory.setPidGeneratorLocation(props.getProperty("pidGenerator.location"));

        DomsEventClient doms = factory.createDomsEventClient();


        doms.addEventToBatch(400022028241l, 1,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true,
                "");
    }

}
