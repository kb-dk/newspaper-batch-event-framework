package dk.statsbiblioteket.medieplatform.autonomous;

import java.util.Properties;

/** This is an implementation that adds the code for constructing a tree iterator */
public abstract class AbstractRunnableComponent<T extends Item> implements RunnableComponent<T> {


    private final Properties properties;

    protected AbstractRunnableComponent(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

}
