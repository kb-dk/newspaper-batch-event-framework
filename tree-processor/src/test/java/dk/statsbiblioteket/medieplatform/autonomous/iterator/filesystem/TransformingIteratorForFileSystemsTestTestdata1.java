package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractTests;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;


public class TransformingIteratorForFileSystemsTestTestdata1 extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
            File testRoot = new File(rootTestdataDir, "small-test-batch/B400022028241-RT1");
            assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
            iterator = new TransformingIteratorForFileSystems(testRoot, "\\.", ".*\\.jp2", ".md5");
        }
        return iterator;
    }


    @Test(groups = "integrationTest")
    public void testIterator() throws Exception {
        super.testIterator(true, false);
    }

    @Test(groups = "integrationTest")
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping(false, false);
    }
}
