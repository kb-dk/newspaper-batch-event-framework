package dk.statsbiblioteket.medieplatform.hadoop;

import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.testng.annotations.Test;

import java.io.IOException;

public class ConvertMapperTest {

    @Test
    public void testSimplest() throws IOException {
        MapDriver<Text, Text, Text, Text> mapDriver;
        ConvertMapper mapper = new ConvertMapper();
        mapDriver = MapDriver.newMapDriver(mapper);
        mapDriver.getConfiguration().set(ConvertMapper.HADOOP_CONVERTER_PATH, "echo");
        mapDriver.getConfiguration().set(ConfigConstants.BATCH_ID, "test");
        mapDriver.getConfiguration().set(ConvertMapper.HADOOP_CONVERTER_OUTPUT_PATH, "/tmp/");
        mapDriver.getConfiguration().set(ConvertMapper.HADOOP_CONVERTER_OUTPUT_EXTENSION_PATH, ".pgm");

        mapDriver.withInput(new Text("1"), new Text("ein"));
        mapDriver.withOutput(new Text("1"), new Text("/tmp/test/ein.pgm"));
        mapDriver.runTest();
    }

}
