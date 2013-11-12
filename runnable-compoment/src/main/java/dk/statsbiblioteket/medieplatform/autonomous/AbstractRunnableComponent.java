package dk.statsbiblioteket.medieplatform.autonomous;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.ConfigurableFilter;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3.IteratorForFedora3;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/** This is an implementation that adds the code for constructing a tree iterator */
public abstract class AbstractRunnableComponent implements RunnableComponent {


    private final Properties properties;
    private final Logger log = LoggerFactory.getLogger(getClass());

    protected AbstractRunnableComponent(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Create a tree iterator for the given batch. It will use the properties construct to get necessary properties
     * 1. useFileSystem: boolean: Determines if the batch should be read from the filesystem (in this case 2-5 are
     * used)
     * 2. scratch: path: The folder where the batch resides
     * 3. groupingChar: Char. The character that separates the prefix and the postfix. Default "."
     * 4. dataFilePattern: The regular expression pattern to identify datafiles. Default ".*\\.jp2$"
     * 5. checksumPostFix: The postfix to append to filenames to get their checksum files. Default ".md5"
     * 6. fedora.admin.username: The username for communicating with fedora
     * 7. fedora.admin.password: The password used for communicating with fedora
     * 8. fedora.server: The fedora server used
     * 9. fedora.iterator.attributenames Datastream names in fedora used for attributes
     * 10. fedora.iterator.predicatenames Predicates of relations in fedora used for generating tree
     *
     * @param batch the batch
     *
     * @return a tree iterator
     */
    protected TreeIterator createIterator(Batch batch) {
        String dataFilePattern = properties.getProperty("dataFilePattern", ".*\\.jp2$");
        boolean useFileSystem = Boolean.parseBoolean(properties.getProperty("useFileSystem", "true"));

        if (useFileSystem) {
            File scratchDir = new File(properties.getProperty("scratch"));
            File batchDir = new File(scratchDir, batch.getFullID());
            String groupingChar = Pattern.quote(properties.getProperty("groupingChar", "."));

            String checksumPostFix = properties.getProperty("checksumPostfix", ".md5");
            return new TransformingIteratorForFileSystems(batchDir, groupingChar, dataFilePattern, checksumPostFix);

        } else {
            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter(properties.getProperty("fedora.admin.username"),
                                                     properties.getProperty("fedora.admin.password")));

            String pid;
            try {
                EnhancedFedoraImpl fedora =
                        new EnhancedFedoraImpl(new Credentials(properties.getProperty("fedora.admin.username"),
                                                               properties.getProperty("fedora.admin.password")),
                                               properties.getProperty("fedora.server").replaceFirst("/(objects)?/?$",
                                                                                                    ""),
                                               null,
                                               null);
                pid = fedora.findObjectFromDCIdentifier("path:" + batch.getFullID()).get(0);
            } catch (MalformedURLException | PIDGeneratorException | BackendMethodFailedException | JAXBException |
                    BackendInvalidCredsException e) {
                log.error("Unable to initialise iterator", e);
                throw new InitialisationException("Unable to initialise iterator", e);
            }

            return new IteratorForFedora3(pid,
                                          client,
                                          properties.getProperty("fedora.server"),
                                          new ConfigurableFilter(Arrays.asList(properties.getProperty(
                                                  "fedora.iterator.attributenames").split(",")),
                                                                 Arrays.asList(properties.getProperty(
                                                                         "fedora.iterator.predicatenames").split(","))),
                                          dataFilePattern);
        }
    }

    /**
     * Gets the full name including the version number of this component
     *
     * @return the fulle name
     */
    public String getFullName() {
        return getComponentName() + "-" + getComponentVersion();
    }

    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
