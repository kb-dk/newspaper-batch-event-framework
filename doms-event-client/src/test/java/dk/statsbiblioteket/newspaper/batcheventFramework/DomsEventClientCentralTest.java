package dk.statsbiblioteket.newspaper.batcheventFramework;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import org.junit.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;

public class DomsEventClientCentralTest {
    @Test
    public void testAddEventToBatch1() throws Exception {
        ArrayList<String> log = new ArrayList<>();
        FedoraMockupEmpty fedora = new FedoraMockupEmpty(log);
        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);

        doms.addEventToBatch(400022028241l, 1,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true,
                "");

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
        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);

        doms.addEventToBatch(400022028241l, 1,
                "agent",
                new Date(0),
                "Details here",
                EventID.Data_Received,
                true,
                "");

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
        DomsEventClientCentral doms = new DomsEventClientCentral(fedora, new NewspaperIDFormatter(),PremisManipulatorFactory.TYPE);
        doms.createBatchRun(400022028241l, 1);
        Assert.assertEquals(8,log.size());
        for (String s : log) {
            Assert.assertNotSame(s,AbstractFedoraMockup.UNEXPECTED_METHOD);
            System.out.println(s);
        }

    }
}
