package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractTests;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;

public class TransformingIteratorForFileSystemsTest extends AbstractTests {

    private TreeIterator iterator;

    @Override
    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null) {
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new TransformingIteratorForFileSystems(file,
                                                              TransformingIteratorForFileSystems.GROUPING_PATTERN_DEFAULT_VALUE,
                                                              TransformingIteratorForFileSystems.DATA_FILE_PATTERN_JP2_VALUE,
                                                              TransformingIteratorForFileSystems.CHECKSUM_POSTFIX_DEFAULT_VALUE,
                                                              Arrays.asList(
                                                                      TransformingIteratorForFileSystems.IGNORED_FILES_DEFAULT_VALUE
                                                                              .split(",")));
        }
        return iterator;

    }


    @Test
    public void testIterator() throws Exception {
        super.testIterator(true, false);
    }

    @Test
    public void testIteratorWithSkipping() throws Exception {
        super.testIteratorWithSkipping(false, false);
    }
}
