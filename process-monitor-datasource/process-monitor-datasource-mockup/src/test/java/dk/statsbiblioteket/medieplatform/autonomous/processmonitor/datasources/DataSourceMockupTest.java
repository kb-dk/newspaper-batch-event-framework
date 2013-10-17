package dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources;


import dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources.TCKTestSuite;
import dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources.DataSource;
import dk.statsbiblioteket.medieplatform.autonomous.processmonitor.datasources.DataSourceMockup;
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
    public Pair<String,Integer> getValidBatchID() {
        return new Pair<>("3001",1);
    }

    @Override
    public Pair<String,Integer> getInvalidBatchID() {
        return new Pair<>("8001",1);
    }

    @Override
    public String getValidAndSucessfullEventIDForValidBatch() {
        return "Shipped_to_supplier";
    }

}
