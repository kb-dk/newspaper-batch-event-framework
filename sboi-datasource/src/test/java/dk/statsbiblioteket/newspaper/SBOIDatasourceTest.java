package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.newspaper.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.TCKTestSuite;

public class SBOIDatasourceTest extends TCKTestSuite {

    @Override
    public DataSource getDataSource() {

        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getValidBatchID() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Long getInvalidBatchID() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public EventID getValidAndSucessfullEventIDForValidBatch() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
