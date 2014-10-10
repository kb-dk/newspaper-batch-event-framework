package dk.statsbiblioteket.medieplatform.hadoop;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

/**
 * This reducer saves the result in doms
 * The input is filepath, jpylyzer xml output
 * The output is filepath, domspid
 */
public class DomsSaverReducer extends AbstractDomsReducer {

    private static Logger log = Logger.getLogger(DomsSaverReducer.class);

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
    }

    /**
     * Reduce (save result in doms)
     *
     * @param key     the filename
     * @param values  the xml to store
     * @param context the task context
     *
     * @throws java.io.IOException  Any checked exception that is not an InterruptedException
     * @throws InterruptedException from Hadoop
     */
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        try {

            String pid = getDomsPid(key);
            log.debug("Found doms pid '" + pid + "' for key '" + key + "'");
            String translate = translate(key.toString());
            log.trace("Translated filename '" + key + "'to '" + translate + "'");

            boolean first = false;
            for (Text value : values) {
                if (!first) {
                    first = true;
                } else {
                    log.error("Found multiple results for file '" + translate + "'");
                    throw new RuntimeException("Found multiple results for file '" + translate + "'");
                }
                log.debug("Stored output for file '" + translate + "' in object '" + pid + "'");
                fedora.modifyDatastreamByValue(
                        pid,
                        datastreamName,
                        value.toString(),
                        Arrays.asList(translate + "." + datastreamName.toLowerCase() + ".xml"),
                        "added data from Hadoop");
                context.write(key, new Text(pid));
            }
        } catch (BackendInvalidCredsException e) {
            log.error(e);
            throw new IOException(e);
        } catch (BackendMethodFailedException e) {
            log.error(e);
            throw new IOException(e);
        } catch (BackendInvalidResourceException e) {
            log.error(e);
            throw new IOException(e);
        }
    }
}
