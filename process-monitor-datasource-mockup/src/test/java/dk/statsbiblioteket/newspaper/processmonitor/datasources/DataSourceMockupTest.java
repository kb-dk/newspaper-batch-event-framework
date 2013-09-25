package dk.statsbiblioteket.newspaper.processmonitor.datasources;

public class DataSourceMockupTest extends TCKTestSuite {

    private DataSource<Long> dataSource = null;


    @Override
    public synchronized DataSource<Long> getDataSource() {
        if (dataSource == null) {
            dataSource = new DataSourceMockup();
        }
        return dataSource;
    }

    @Override
    public Long getValidBatchID() {
        return 3001l;
    }

    @Override
    public Long getInvalidBatchID() {
        return 8001l;
    }

    @Override
    public EventID getValidAndSucessfullEventIDForValidBatch() {
        return EventID.Shipped_to_supplier;
    }

}
