package dk.statsbiblioteket.medieplatform.hadoop;

import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.util.console.ProcessRunner;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Input is line-number, origignal file path.
 * Output is original file path, converted file path
 */
public class ConvertMapper extends Mapper<Text, Text, Text, Text> {


    protected final static Logger log = Logger.getLogger(ConvertMapper.class);
    public static final String HADOOP_CONVERTER_OUTPUT_EXTENSION_PATH = "hadoop.converter.output.extension";
    public static final String HADOOP_CONVERTER_OUTPUT_PATH = "hadoop.converter.output.path";
    public static final String HADOOP_CONVERTER_PATH = "hadoop.converter.executable.path";
    private String batchID;
    private String commandPath;
    private File batchFolder;
    private String resultExtention;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        batchID = context.getConfiguration().get(ConfigConstants.BATCH_ID);
        commandPath = context.getConfiguration().get(HADOOP_CONVERTER_PATH);
        String outputFolder = context.getConfiguration().get(HADOOP_CONVERTER_OUTPUT_PATH);
        resultExtention = context.getConfiguration().get(HADOOP_CONVERTER_OUTPUT_EXTENSION_PATH);
        batchFolder = new File(outputFolder, batchID);
        batchFolder.mkdirs();

    }

    public String getBatchID() {
        return batchID;
    }

    public String getCommandPath() {
        return commandPath;
    }

    public File getBatchFolder() {
        return batchFolder;
    }

    /**
     * run command on the given file
     *
     * @param dataPath the path to the jp2 file
     *
     * @return the path to the converted file
     * @throws java.io.IOException if the execution of jpylyzer failed in some fashion (not invalid file, if the
     *                             program
     *                             returned non-zero returncode)
     */
    protected File convert(String dataPath) throws IOException {


        File resultPath = getConvertedPath(dataPath);
        String[] commandLine = makeCommandLine(dataPath, getCommandPath(), resultPath);
        ProcessRunner runner = new ProcessRunner(commandLine);

        log.debug("Running command '" + Arrays.deepToString(commandLine) + "'");
        Map<String, String> myEnv = new HashMap<String, String>(System.getenv());
        runner.setEnviroment(myEnv);
        runner.setOutputCollectionByteSize(Integer.MAX_VALUE);

        //this call is blocking
        runner.run();

        if (runner.getReturnCode() == 0) {
            return resultPath;
        } else {
            String message
                    = "failed to run, returncode:" + runner.getReturnCode() + ", stdOut:" + runner.getProcessOutputAsString() + " stdErr:" + runner
                    .getProcessErrorAsString();
            throw new IOException(message);
        }
    }

    protected String[] makeCommandLine(String dataPath, String commandPath, File resultFile) {


        String[] commandBits = commandPath.split(" ");
        List<String> commandList = Arrays.asList(commandBits);
        ArrayList<String> result = new ArrayList<String>(commandList);
        result.addAll(
                Arrays.asList(
                        "-i", dataPath, "-o", resultFile.getAbsolutePath()));
        return result.toArray(new String[result.size()]);
    }

    protected File getConvertedPath(String dataPath) {
        return new File(getBatchFolder(), new File(dataPath + resultExtention).getName());
    }

    @Override
    protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
        try {
            log.debug("Mapping for '" + key + "' and '" + value + "'");
            File converted = convert(value.toString());
            context.write(key, new Text(converted.getAbsolutePath()));
        } catch (Exception e) {
            log.error(e);
            throw new IOException(e);
        }
    }

}
