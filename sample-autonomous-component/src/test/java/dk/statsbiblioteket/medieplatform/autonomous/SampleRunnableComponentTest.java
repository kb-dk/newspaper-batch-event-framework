package dk.statsbiblioteket.medieplatform.autonomous;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleRunnableComponentTest {
    @Test
    public void testDoWorkOnBatch()
            throws
            Exception {
        SampleRunnableComponent runnableComponent =new MockupIteratorSuper(System.getProperties());

        ResultCollector result =
                new ResultCollector(runnableComponent.getComponentName(), runnableComponent.getComponentVersion());
        Batch batch = new Batch("60000");
        runnableComponent.doWorkOnBatch(batch,result);
        Assert.assertTrue(result.isSuccess());

    }
}
