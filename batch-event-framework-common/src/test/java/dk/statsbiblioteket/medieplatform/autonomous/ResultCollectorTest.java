package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ResultCollectorTest {

    @Test
    public void testMaxResults() {
        int maxResults = 10;
        int actualResults = 42;
        ResultCollector resultCollector = new ResultCollector("check1", "0.1", maxResults);
        String reference = "reference";
        String type = "type";
        for (int i=0; i < actualResults; i++) {
                   resultCollector.addFailure(reference, type, "check1", "description", "details1\n", "details2\n");
        }
        String report = resultCollector.toReport();
        assertEquals(maxResults, report.split("<failure>").length - 1, "More results than maxresults");
        assertTrue(report.contains(actualResults + ""), "Report does not contain expected result");
        assertFalse(report.contains((actualResults -1 ) + ""), "Report contains unexpected result");
    }


    /** Test the merging of resultCollectors */
    @Test()
    public void testMerger() {
        ResultCollector resultCollector = new ResultCollector("check1", "0.1");
        String reference = "reference";
        String type = "type";
        resultCollector.addFailure(reference, type, "check1", "description", "details1\n", "details2\n");


        ResultCollector resultCollector2 = new ResultCollector("check2", "0.2");

        String type2 = "type2";
        resultCollector2.addFailure(reference, type2, "check2", "description2", "details1\n", "details2\n");

        //test identity
        ResultCollector resultCollectorIdentity = new ResultCollector("check1", "0.1");
        resultCollector.mergeInto(resultCollectorIdentity);
        Date now = new Date();
        resultCollector.setTimestamp(now);
        resultCollectorIdentity.setTimestamp(now);
        assertEquals(resultCollectorIdentity.toReport(), resultCollector.toReport(),"The two reports are not identical");

        ResultCollector result = new ResultCollector("batch", "0.1");

        assertTrue(result.isSuccess(),"A new report is not succesful");

        resultCollector.mergeInto(result);
        assertFalse(result.isSuccess(),"Success after merge");
        String firstMerger = result.toReport();


        resultCollector2.mergeInto(result);
        assertFalse(result.isSuccess(),"Success after merge");
        String secondMerger = result.toReport();

        Assert.assertNotEquals(firstMerger, secondMerger, "The second merger changed nothing");

        resultCollector.mergeInto(result);
        assertFalse(result.isSuccess(),"Success after merge");
        String thirdMerger = result.toReport();

        Assert.assertNotEquals(secondMerger, thirdMerger, "merging twice is not idempotent");


    }

}
