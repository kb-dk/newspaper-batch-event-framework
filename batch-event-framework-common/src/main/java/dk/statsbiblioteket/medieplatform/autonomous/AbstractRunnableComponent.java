package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Properties;

/** This is an implementation that adds the code for constructing a tree iterator */
public abstract class AbstractRunnableComponent implements RunnableComponent {


    private final Properties properties;

    protected AbstractRunnableComponent(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public final String getComponentName() {
        return getClass().getSimpleName();
    }

    @Override
    public final String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void doWorkOnItem(Item item, ResultCollector resultCollector) throws Exception {
        if (item instanceof Batch) {
            Batch batch = (Batch) item;
            doWorkOnBatch(batch,resultCollector);
        } else {
            throw new IllegalArgumentException("Item must be a batch");
        }
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {

    }
}
