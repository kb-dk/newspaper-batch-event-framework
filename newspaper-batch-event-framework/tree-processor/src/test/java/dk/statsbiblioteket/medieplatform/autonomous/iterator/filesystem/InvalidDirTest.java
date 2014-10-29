package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;

public class InvalidDirTest {

    @Test(expectedExceptions = RuntimeException.class)

    public void testInvalidDir() {
        TransformingIteratorForFileSystems iterator = new TransformingIteratorForFileSystems(
                new File("/invalid"), "\\.", ".*\\.jp2", ".md5",
                Arrays.asList("transfer_complete", "transfer_acknowledged"));
        iterator.next();
        iterator.next();
        iterator.next();
    }
}
