package dk.statsbiblioteket.medieplatform.autonomous;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This is an implementation that adds the code for constructing a tree iterator
 */
public abstract class AbstractRunnableComponent
        implements RunnableComponent {


    private final Properties properties;

    protected AbstractRunnableComponent(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {

        return properties;
    }

    /**
     * Create a tree iterator for the given batch. It will use the properties construct to get nessesary properties
     * 1. useFileSystem: boolean: Determines if the batch should be read from the filesystem
     * 2. scratch: path: The folder where the batch resides
     * 3. groupingChar: Char. The character that separates the prefix and the postfix. Default "."
     * 4. dataFilePattern: The regular expression pattern to identify datafiles. Default ".*\\.jp2$"
     * 5. checksumPostFix: The postfix to append to filenames to get their checksum files. Default ".md5"
     * @param batch the batch
     * @return a tree iterator
     */
    protected TreeIterator createIterator(
                                        Batch batch) {
        boolean useFileSystem = Boolean.parseBoolean(properties.getProperty("useFileSystem", "true"));
        if (useFileSystem) {
            File scratchDir = new File(properties.getProperty("scratch"));
            File batchDir = new File(scratchDir, batch.getFullID());
            String groupingChar = Pattern.quote(properties.getProperty("groupingChar","."));
            String dataFilePattern = properties.getProperty("dataFilePattern", ".*\\.jp2$");
            String checksumPostFix = properties.getProperty("checksumPostfix",".md5");
            return new TransformingIteratorForFileSystems(batchDir,groupingChar,dataFilePattern,checksumPostFix);

        }
        throw new UnsupportedOperationException("Presently only supported for filesystems, sorry");
    }

    /**
     * Gets the full name including the version number of this component
     * @return the fulle name
     */
    public String getFullName(){
        return getComponentName()+"-"+getComponentVersion();
    }

}
