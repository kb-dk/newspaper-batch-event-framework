package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class ResultCollectorTest {

    /**
     * Test the merging of resultCollectors
     */
    @Test
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
        Assert.assertEquals(resultCollectorIdentity.toReport(), resultCollector.toReport());

        ResultCollector result = new ResultCollector("batch", "0.1");

        Assert.assertTrue(result.isSuccess());

        resultCollector.mergeInto(result);
        Assert.assertFalse(result.isSuccess());
        String firstMerger = result.toReport();


        resultCollector2.mergeInto(result);
        Assert.assertFalse(result.isSuccess());
        String secondMerger = result.toReport();

        Assert.assertNotEquals(firstMerger, secondMerger, "The second merger changed nothing");

        resultCollector.mergeInto(result);
        Assert.assertFalse(result.isSuccess());
        String thirdMerger = result.toReport();

        Assert.assertNotEquals(secondMerger, thirdMerger, "merging twice is not idempotent");



    }

}
