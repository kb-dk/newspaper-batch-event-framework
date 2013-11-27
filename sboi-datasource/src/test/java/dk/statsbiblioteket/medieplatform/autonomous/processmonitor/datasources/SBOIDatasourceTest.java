package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.CommunicationException;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.NotFoundException;
import dk.statsbiblioteket.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class SBOIDatasourceTest extends TCKTestSuite {

    private static final long MINUTES = 60 * 1000;
    private DataSource dataSource;

    @Override
    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = new Properties();
            try {
                props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SBOIDatasourceConfiguration conf = new SBOIDatasourceConfiguration();

            conf.setDomsUrl(props.getProperty(ConfigConstants.DOMS_URL));
            conf.setDomsUser(props.getProperty(ConfigConstants.DOMS_USERNAME));
            conf.setDomsPass(props.getProperty(ConfigConstants.DOMS_PASSWORD));
            conf.setUrlToPidGen(props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
            conf.setSummaLocation(props.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL));
            dataSource = new SBOIDatasource(conf);
            BatchEventClientImpl batchClient = new BatchEventClientImpl(
                    conf.getSummaLocation(),
                    conf.getDomsUrl(),
                    conf.getDomsUser(),
                    conf.getDomsPass(),
                    conf.getUrlToPidGen());
            try {
                try {
                    Batch testBatch = batchClient.getBatch(getValidBatchID().getLeft(), getValidBatchID().getRight());
                } catch (NotFoundException e) {
                    //So, the test batch was not in the system

                    //Add it
                    batchClient.addEventToBatch(
                            getValidBatchID().getLeft(),
                            getValidBatchID().getRight(),
                            "SBOI unit test",
                            new Date(),
                            "no details",
                            getValidAndSucessfullEventIDForValidBatch(),
                            true);

                    try {
                        //Wait for the Summa system to reindex the doms
                        Thread.sleep(6 * MINUTES);
                    } catch (InterruptedException e1) {
                        //so, if you want to interruped me, fix it yourself.
                    }
                }

            } catch (CommunicationException e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }

    @Override
    public Pair<String, Integer> getValidBatchID() {
        return new Pair<>("400022028241", 1);
    }

    @Override
    public Pair<String, Integer> getInvalidBatchID() {
        return new Pair<>("300022028241", 1);
    }

    @Override
    public String getValidAndSucessfullEventIDForValidBatch() {
        return "Data_Received";
    }
}
