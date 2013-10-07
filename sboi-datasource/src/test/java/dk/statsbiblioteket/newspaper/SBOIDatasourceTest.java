package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.TCKTestSuite;

import java.io.IOException;
import java.util.Properties;

public class SBOIDatasourceTest extends TCKTestSuite {

    @Override
    public DataSource getDataSource() {
        Properties props = new Properties();

        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("ITtest.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SBOIDatasourceConfiguration conf = new SBOIDatasourceConfiguration();

        conf.setDomsUrl(props.getProperty("fedora.location"));
        conf.setDomsUser(props.getProperty("fedora.username"));
        conf.setDomsPass(props.getProperty("fedora.password"));
        conf.setUrlToPidGen(props.getProperty("pidgenerator.location"));
        conf.setSummaLocation(props.getProperty("sboi.summa"));
        return new SBOIDatasource(conf);
    }

    @Override
    public Long getValidBatchID() {
        return 400022028241l;
    }

    @Override
    public Long getInvalidBatchID() {
        return 300022028241l;
    }

    @Override
    public EventID getValidAndSucessfullEventIDForValidBatch() {
        return EventID.Data_Received;
    }
}
