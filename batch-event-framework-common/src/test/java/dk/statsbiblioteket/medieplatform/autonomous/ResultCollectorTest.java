package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        assertEquals(maxResults, report.split("<failure>").length - 1, "More results than maxresults " + report);
        assertTrue(report.contains("number of results ("+  actualResults + ")"), "Report does not contain expected result " + report);
        assertFalse(report.contains("number of results ("+  (actualResults -1 ) + ""), "Report contains unexpected result " + report);
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

    /**
     * Test that invoking addFailure in parallel works as expected
     * @throws InterruptedException
     */
    @Test
    public void testParallelResultCollector() throws InterruptedException {
        final ResultCollector resultCollector1 = new ResultCollector("check1", "0.1");

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 2000; i++) {
            final String description = "error" + i;
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    resultCollector1.addFailure("a", "b", "c", description);
                }
            });
        }
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        Matcher matcher = Pattern.compile("error").matcher(resultCollector1.toReport());
        int matches = 0;
        while(matcher.find()) {
            matches++;
        }

        assertEquals(2000, matches);
    }
}
