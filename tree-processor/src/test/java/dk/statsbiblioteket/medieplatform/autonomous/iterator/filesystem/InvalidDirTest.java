package dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.testng.annotations.Test;

import java.io.File;

public class InvalidDirTest {

    @Test(expectedExceptions = RuntimeException.class)

    public void testInvalidDir(){
        TransformingIteratorForFileSystems iterator =
                new TransformingIteratorForFileSystems(new File("/invalid"), "\\.", ".*\\.jp2", ".md5");
        iterator.next();
        iterator.next();
        iterator.next();
    }
}
