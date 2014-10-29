package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

public class InvalidDirTest {

    @Test(expectedExceptions = RuntimeException.class)

    public void testInvalidDir() {
        TransformingIteratorForFileSystems iterator = new TransformingIteratorForFileSystems(new File("/invalid"),
                                                                                             TransformingIteratorForFileSystems.GROUPING_PATTERN_DEFAULT_VALUE,
                                                                                             TransformingIteratorForFileSystems.DATA_FILE_PATTERN_JP2_VALUE,
                                                                                             TransformingIteratorForFileSystems.CHECKSUM_POSTFIX_DEFAULT_VALUE,
                                                                                             Arrays.asList(
                                                                                                     TransformingIteratorForFileSystems.IGNORED_FILES_DEFAULT_VALUE
                                                                                                             .split(",")));
        iterator.next();
        iterator.next();
        iterator.next();
    }
}
