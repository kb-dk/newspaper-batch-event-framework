package dk.statsbiblioteket.newspaper.processmonitor.datasources;


import dk.statsbiblioteket.util.Pair;

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
    public Pair<Long,Integer> getValidBatchID() {
        return new Pair<>(3001l,1);
    }

    @Override
    public Pair<Long,Integer> getInvalidBatchID() {
        return new Pair<>(8001l,1);
    }

    @Override
    public EventID getValidAndSucessfullEventIDForValidBatch() {
        return EventID.Shipped_to_supplier;
    }

}
