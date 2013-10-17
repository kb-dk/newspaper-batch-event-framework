package dk.statsbiblioteket.autonomous;

import dk.statsbiblioteket.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.autonomous.processmonitor.datasources.Batch;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This is an implementation that adds the code for constructing a tree iterator
 */
public abstract class AbstractRunnableComponent
        implements RunnableComponent {


    /**
     * Create a tree iterator for the given batch. It will use the properties construct to get nessesary properties
     * 1. useFileSystem: boolean: Determines if the batch should be read from the filesystem
     * 2. scratch: path: The folder where the batch resides
     * 3. groupingChar: Char. The character that separates the prefix and the postfix. Default "."
     * 4. dataFilePattern: The regular expression pattern to identify datafiles. Default ".*\\.jp2$"
     * 5. checksumPostFix: The postfix to append to filenames to get their checksum files. Default ".md5"
     * @param properties the properties
     * @param batch the batch
     * @return a tree iterator
     */
    protected TreeIterator createIterator(Properties properties,
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

}
