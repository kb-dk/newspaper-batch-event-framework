package dk.statsbiblioteket.newspaper;

import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.CommunicationException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.NotFoundException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.TCKTestSuite;
import dk.statsbiblioteket.util.Pair;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

public class SBOIDatasourceTest
        extends TCKTestSuite {

    private static final long MINUTES = 60 * 1000;
    private DataSource dataSource;

    @Override
    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            Properties props = new Properties();
            try {
                props.load(Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResourceAsStream("ITtest.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SBOIDatasourceConfiguration conf = new SBOIDatasourceConfiguration();

            conf.setDomsUrl(props.getProperty("fedora.location"));
            conf.setDomsUser(props.getProperty("fedora.username"));
            conf.setDomsPass(props.getProperty("fedora.password"));
            conf.setUrlToPidGen(props.getProperty("pidgenerator.location"));
            conf.setSummaLocation(props.getProperty("sboi.summa"));
            dataSource = new SBOIDatasource(conf);
            BatchEventClientImpl batchClient =
                    new BatchEventClientImpl(conf.getSummaLocation(), conf.getDomsUrl(), conf.getDomsUser(),
                                             conf.getDomsPass(), conf.getUrlToPidGen());
            try {
                try {
                    Batch testBatch = batchClient.getBatch(getValidBatchID().getLeft(), getValidBatchID().getRight());
                } catch (NotFoundException e) {
                    //So, the test batch was not in the system

                    //Add it
                    batchClient.addEventToBatch(getValidBatchID().getLeft(), getValidBatchID().getRight(), "SBOI unit test",new Date(),"no details",getValidAndSucessfullEventIDForValidBatch(),true);

                    try {
                        //Wait for the Summa system to reindex the doms
                        Thread.sleep(6*MINUTES);
                    } catch (InterruptedException e1) {
                       //so, if you want to interruped me, fix it yourself.
                    }
                }

            } catch (CommunicationException e) {
                throw new RuntimeException(e);
            }
        } return dataSource;
    }

    @Override
    public Pair<Long, Integer> getValidBatchID() {
        return new Pair<>(400022028247l, 1);
    }

    @Override
    public Pair<Long, Integer> getInvalidBatchID() {
        return new Pair<>(300022028241l, 1);
    }

    @Override
    public String getValidAndSucessfullEventIDForValidBatch() {
        return "Data_Received";
    }
}
