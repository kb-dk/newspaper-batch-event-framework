package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Properties;

public class TestingRunnableComponent extends TreeProcessorAbstractRunnableComponent {

    protected TestingRunnableComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getEventID() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doWorkOnItem(Item item, ResultCollector resultCollector) throws Exception {

    }
}
