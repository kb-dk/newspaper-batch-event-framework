package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;

public class DomsEventClientCentralTest {

    public static final long BATCH_ID = 400022028241l;
    public static final int RUN_NR = 1;

    @Test
    public void testAddEventToBatch3() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE,DomsEventClientFactory.BATCH_TEMPLATE,DomsEventClientFactory.RUN_TEMPLATE,DomsEventClientFactory.HAS_PART,DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(BATCH_ID, RUN_NR+1,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true);

        Assert.assertEquals(10, log.size());
        for (String s : log) {
            Assert.assertNotSame(s,AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }

    }


    @Test
    public void testAddEventToBatch1() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);

        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE,DomsEventClientFactory.BATCH_TEMPLATE,DomsEventClientFactory.RUN_TEMPLATE,DomsEventClientFactory.HAS_PART,DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(BATCH_ID, RUN_NR,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true);

        Assert.assertEquals(10, log.size());
        for (String s : log) {
            Assert.assertNotSame(s,AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }

    }

    @Test
    public void testAddEventToBatch2() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupBatchNoRun(log);
        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE,DomsEventClientFactory.BATCH_TEMPLATE,DomsEventClientFactory.RUN_TEMPLATE,DomsEventClientFactory.HAS_PART,DomsEventClientFactory.EVENTS);

        doms.addEventToBatch(BATCH_ID, RUN_NR,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true);

        Assert.assertEquals(3,log.size());
        for (String s : log) {
            Assert.assertNotSame(s,AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }

    }


    @Test
    public void testCreateBatchRun() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);
        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE,DomsEventClientFactory.BATCH_TEMPLATE,DomsEventClientFactory.RUN_TEMPLATE,DomsEventClientFactory.HAS_PART,DomsEventClientFactory.EVENTS);
        doms.createBatchRun(BATCH_ID, RUN_NR);
        Assert.assertEquals(8,log.size());
        for (String s : log) {
            Assert.assertNotSame(s,AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }

    }
}
