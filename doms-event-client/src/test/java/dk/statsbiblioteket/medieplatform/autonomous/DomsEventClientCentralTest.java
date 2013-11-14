package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;

public class DomsEventClientCentralTest {

    public static final String BATCH_ID = "400022028241";
    public static final int ROUND_TRIP_NUMBER = 1;

    @Test
    public void testAddEventToBatch3() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventClientCentral doms = new DomsEventClientCentral(
                fedora,
                new NewspaperIDFormatter(),
                PremisManipulatorFactory.TYPE,
                DomsEventClientFactory.BATCH_TEMPLATE,
                DomsEventClientFactory.ROUND_TRIP_TEMPLATE,
                DomsEventClientFactory.HAS_PART,
                DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(
                BATCH_ID, ROUND_TRIP_NUMBER + 1, "agent", new Date(0), "Details here", "Data_Received", true);

        Assert.assertEquals(8, log.size());
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out
                  .println(s);
        }

    }

    @Test
    public void testAddEventToBatch1() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventClientCentral doms = new DomsEventClientCentral(
                fedora,
                new NewspaperIDFormatter(),
                PremisManipulatorFactory.TYPE,
                DomsEventClientFactory.BATCH_TEMPLATE,
                DomsEventClientFactory.ROUND_TRIP_TEMPLATE,
                DomsEventClientFactory.HAS_PART,
                DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(BATCH_ID, ROUND_TRIP_NUMBER, "agent", new Date(0), "Details here", "Data_Received", true);

        Assert.assertEquals(8, log.size());
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out
                  .println(s);
        }

    }

    @Test
    public void testAddEventToBatch2() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupBatchNoRoundTripObject(log);
        DomsEventClientCentral doms = new DomsEventClientCentral(
                fedora,
                new NewspaperIDFormatter(),
                PremisManipulatorFactory.TYPE,
                DomsEventClientFactory.BATCH_TEMPLATE,
                DomsEventClientFactory.ROUND_TRIP_TEMPLATE,
                DomsEventClientFactory.HAS_PART,
                DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(BATCH_ID, ROUND_TRIP_NUMBER, "agent", new Date(0), "Details here", "Data_Received", true);

        Assert.assertEquals(log.size(), 3);
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out
                  .println(s);
        }

    }

    @Test
    public void testCreateBatchRoundTrip() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);
        DomsEventClientCentral doms = new DomsEventClientCentral(
                fedora,
                new NewspaperIDFormatter(),
                PremisManipulatorFactory.TYPE,
                DomsEventClientFactory.BATCH_TEMPLATE,
                DomsEventClientFactory.ROUND_TRIP_TEMPLATE,
                DomsEventClientFactory.HAS_PART,
                DomsEventClientFactory.EVENTS);
        doms.createBatchRoundTrip(BATCH_ID, ROUND_TRIP_NUMBER);
        Assert.assertEquals(log.size(), 6);
        for (String s : log) {
            Assert.assertNotSame(s, AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out
                  .println(s);
        }

    }
}
