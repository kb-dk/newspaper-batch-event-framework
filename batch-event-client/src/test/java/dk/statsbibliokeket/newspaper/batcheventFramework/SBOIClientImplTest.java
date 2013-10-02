package dk.statsbibliokeket.newspaper.batcheventFramework;

import org.testng.annotations.Test;

public class SBOIClientImplTest {
    @Test
    public void testGetBatches() throws Exception {

        SBOIClientImpl summa = new SBOIClientImpl("http://achernar:57608/domsgui/search/services/SearchWS?wsdl");
        summa.getBatches(null,null,null);

    }
}
