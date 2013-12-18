package dk.statsbiblioteket.medieplatform.hadoop;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.util.xml.XSLT;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.MRConfig;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is the abstract hadoop runnable component. It is meant for the more common hadoop tasks that the
 * autonomous components must do.
 *
 * The doWorkOnBatch method have been implemented, and a abstract getTool()
 */
public abstract class AbstractHadoopRunnableComponent extends AbstractRunnableComponent {

    private static Logger log = LoggerFactory.getLogger(AbstractHadoopRunnableComponent.class);


    /**
     * Constructor matching super. Super requires a properties to be able to initialise the tree iterator, if needed.
     * If you do not need the tree iterator, ignore properties.
     *
     * You can use properties for your own stuff as well
     *
     * @param properties properties
     *
     * @see #getProperties()
     */
    public AbstractHadoopRunnableComponent(Properties properties) {
        super(properties);
    }


    protected abstract Tool getTool();

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        runTool(getTool(), batch, resultCollector);
    }

    private void runTool(Tool tool, Batch batch, ResultCollector resultCollector) throws
                                                                                  IOException,
                                                                                  InterruptedException,
                                                                                  TransformerException {
        //create the input as a file on the cluster
        Configuration conf = new Configuration();
        getProperties().setProperty(ConfigConstants.ITERATOR_USE_FILESYSTEM, "False");
        propertiesToHadoopConfiguration(conf, getProperties());

        conf.set(ConfigConstants.BATCH_ID, batch.getFullID());

        String user = conf.get(ConfigConstants.HADOOP_USER, "newspapr");
        conf.set(MRConfig.FRAMEWORK_NAME, MRConfig.YARN_FRAMEWORK_NAME);


        FileSystem fs = FileSystem.get(FileSystem.getDefaultUri(conf), conf, user);
        long time = System.currentTimeMillis();
        String jobFolder = getProperties().getProperty(ConfigConstants.JOB_FOLDER);
        Path inputFile = createInputFile(batch, fs, time, jobFolder);
        Path outDir = new Path(
                jobFolder, "output_" + batch.getFullID() + "_" + time);


        runJob(tool, batch, resultCollector, conf, inputFile, outDir, user);

    }

    private Path createInputFile(Batch batch, FileSystem fs, long time, String jobFolder) throws
                                                                                          IOException,
                                                                                          TransformerException {
        Path inputFile = new Path(
                jobFolder, "input_" + batch.getFullID() + "_" + time + "_files.txt");

        //make file list stream from batch structure
        fs.mkdirs(inputFile.getParent());
        FSDataOutputStream fileoutStream = fs.create(
                inputFile);
        buildFileList(batch, fileoutStream);
        fileoutStream.close();
        return inputFile;
    }

    private void propertiesToHadoopConfiguration(Configuration conf, Properties properties) {
        for (Map.Entry<Object, Object> objectObjectEntry : properties.entrySet()) {
            conf.set(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue().toString());
        }
    }

    private void runJob(final Tool job, final Batch batch, final ResultCollector resultCollector,
                        final Configuration conf, final Path inputFile, final Path outDir, String username) throws
                                                                                                            IOException,
                                                                                                            InterruptedException {
        //upload job to cluster if not already present
        //execute job on file

        UserGroupInformation ugi = UserGroupInformation.createRemoteUser(username);
        ugi.doAs(
                new PrivilegedExceptionAction<ResultCollector>() {
                    public ResultCollector run() throws Exception {
                        job.setConf(conf);
                        try {
                            int result = ToolRunner.run(
                                    conf, job, new String[]{inputFile.toString(), outDir.toString()});

                            if (result != 0) {
                                resultCollector.addFailure(
                                        batch.getFullID(),
                                        "jp2file",
                                        getClass().getName(),
                                        "Failed to run on this batch");
                            }
                        } catch (Exception e) {
                            resultCollector.addFailure(
                                    batch.getFullID(), "exception", getClass().getName(), e.toString());
                        }
                        return resultCollector;
                    }
                });

    }

    private void buildFileList(Batch batch, OutputStream outputStream) throws IOException, TransformerException {
        InputStream structure;
        try {
            structure = retrieveBatchStructure(batch);
        } catch (NullPointerException e) {
            throw new IOException("The batch '" + batch.getFullID() + "' was not found in doms");
        }
        if (structure == null) {
            throw new IOException("The structure for the batch '" + batch.getFullID() + "'is not available");
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(
                ConfigConstants.PREFIX, getProperties().getProperty(ConfigConstants.PREFIX));
        XSLT.transform(
                Thread.currentThread().getContextClassLoader().getResource("fileNamesFromStructure.xslt"),
                structure,
                outputStream,
                params);

    }
}
