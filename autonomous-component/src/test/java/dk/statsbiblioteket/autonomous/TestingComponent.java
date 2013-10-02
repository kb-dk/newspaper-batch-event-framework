package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

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
    public EventID getEventID() {
        return EventID.Data_Archived;
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        System.out.println("working");
        resultCollector.setSuccess(true);
    }
}
