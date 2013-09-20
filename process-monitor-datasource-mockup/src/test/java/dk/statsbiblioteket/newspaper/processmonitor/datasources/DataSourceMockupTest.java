package dk.statsbiblioteket.newspaper.processmonitor.datasources;

public class DataSourceMockupTest extends TCKTestSuite {

    private DataSource dataSource = null;

    @Override
    public boolean isRunNrInBatchID() {
        return false;
    }

    @Override
    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new DataSourceMockup();
        }
        return dataSource;
    }

    @Override
    public String getValidBatchID() {
        return "hans";
    }

    @Override
    public String getInvalidBatchID() {
        return "steffen";
    }

    @Override
    public String getValidAndSucessfullEventIDForValidBatch() {
        return "reels-sent";
    }

    @Override
    public String getInvalidEventIDForValidBatch() {
        return "invalidEvent";  //To change body of implemented methods use File | Settings | File Templates.
    }


}
