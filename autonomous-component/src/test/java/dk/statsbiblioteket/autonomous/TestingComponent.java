package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;

public class TestingComponent implements RunnableComponent {




    @Override
    public String getComponentName() {
        return "TestingComponent";

    }

    @Override
    public String getComponentVersion() {
        return "0.1-SNAPSHOT";
    }

    @Override
    public String getEventID() {
        return "Data_Archived";
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
    }
}
