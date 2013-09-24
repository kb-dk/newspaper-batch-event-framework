package dk.statsbiblioteket.newspaper.processmonitor.datasources;

public class DataSourceMockupTest extends TCKTestSuite {

    private DataSource dataSource = null;


    @Override
    public synchronized DataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new DataSourceMockup();
        }
        return dataSource;
    }

    @Override
    public int getValidBatchID() {
        return 3001;
    }

    @Override
    public int getInvalidBatchID() {
        return 8001;
    }

    @Override
    public EventID getValidAndSucessfullEventIDForValidBatch() {
        return EventID.Shipped_to_supplier;
    }

}
