package dk.statsbiblioteket.doms;

import dk.statsbiblioteket.doms.iterator.IteratorForFileSystems;
import dk.statsbiblioteket.doms.iterator.common.SBIterator;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class IteratorForFileSystemsTest extends AbstractTests {

    private SBIterator iterator;

    @Override
    public SBIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new IteratorForFileSystems(file,file.getParentFile());
        }
        return iterator;

    }

    @Override
    @Test
    public void testIterator() throws Exception {
        super.testIterator();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    @Test
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
