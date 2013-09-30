package dk.statsbiblioteket.autonomous;

import com.netflix.curator.test.TestingServer;

public class TestingComponent extends AbstractComponent{

    TestingServer server;

    public TestingComponent() throws Exception {
        server = new TestingServer();

    }


    public void doWorkOnBatch(Long batchId, int roundTripNumber, ResultCollector resultCollector) throws Exception{
        System.out.println("working");
    }


}
