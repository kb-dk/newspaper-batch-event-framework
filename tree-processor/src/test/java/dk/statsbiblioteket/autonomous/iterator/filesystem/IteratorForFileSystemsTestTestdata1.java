package dk.statsbiblioteket.autonomous.iterator.filesystem;

import dk.statsbiblioteket.autonomous.AbstractTests;
import dk.statsbiblioteket.autonomous.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;

public class IteratorForFileSystemsTestTestdata1 extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
            File testRoot = new File(rootTestdataDir, "small-test-batch_contents-included/B400022028241-RT1");
            assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
            iterator = new SimpleIteratorForFilesystems(testRoot);
        }
        return iterator;
    }



    @Override
    @Test(groups = "integrationTest")
    public void testIterator() throws Exception {
        super.testIterator();
    }

    @Override
    @Test(groups = "integrationTest")
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();
    }
}
