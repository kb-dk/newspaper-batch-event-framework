package dk.statsbiblioteket.medieplatform.hadoop;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomsSaverReducerTest {

    @Test
    public void testSimplest() throws
                               IOException,
                               BackendInvalidCredsException,
                               BackendMethodFailedException,
                               BackendInvalidResourceException {
        ReduceDriver<Text, Text, Text, Text> reduceDriver;

        final EnhancedFedora fedora = mock(EnhancedFedora.class);
        String testPid = "uuid:testPid";
        when(fedora.findObjectFromDCIdentifier(anyString())).thenReturn(Arrays.asList(testPid));
        String jpylyzer = "JPYLYZER";
        doNothing().when(fedora).modifyDatastreamByValue(
                eq(testPid), eq(jpylyzer), anyString(), anyList(), anyString());

        reduceDriver = ReduceDriver.newReduceDriver(
                new DomsSaverReducer() {
                    @Override
                    protected EnhancedFedora createFedoraClient(Context context) throws IOException {
                        return fedora;
                    }
                });
        String batchID = "B400022028241-RT1";
        reduceDriver.getConfiguration().setIfUnset(ConfigConstants.BATCH_ID, batchID);
        reduceDriver.getConfiguration().setIfUnset(ConfigConstants.HADOOP_SAVER_DATASTREAM, jpylyzer);
        Text key = new Text(batchID + "/testFile");
        reduceDriver.withInput(key, Arrays.asList(new Text("<jpylyzer/>")));
        reduceDriver.withOutput(key, new Text(testPid));
        reduceDriver.runTest();

    }

}
