package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.AbstractTests;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformingIteratorForFileSystemsTestTestdata1 extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
            File testRoot = new File(rootTestdataDir, "small-test-batch_contents-included/B400022028241-RT1");
            assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
            iterator = new TransformingIteratorForFileSystems(testRoot,"\\.",".*\\.jp2");
        }
        return iterator;
    }



    @Override
    @Test(groups = "integrationTest")
    public void testIterator() throws Exception {
        super.testIterator();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Test(groups = "integrationTest")
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
