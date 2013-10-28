package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractTests;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;


public class SimpleIteratorForFilesystemsTest
        extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new SimpleIteratorForFilesystems(file);
        }
        return iterator;

    }



    @Test
    public void testIterator() throws Exception {
        super.testIterator(false,false);
    }

    @Test
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping(false,false);
    }
}
