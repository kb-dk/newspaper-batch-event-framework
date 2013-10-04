package dk.statsbiblioteket.doms.iterator.filesystem;

import dk.statsbiblioteket.doms.AbstractTests;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;


public class IteratorForFileSystemsTest extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new IteratorForFileSystems(file);
        }
        return iterator;

    }



    @Override
    @Test
    public void testIterator() throws Exception {
        super.testIterator();
    }

    @Override
    @Test
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();
    }
}
