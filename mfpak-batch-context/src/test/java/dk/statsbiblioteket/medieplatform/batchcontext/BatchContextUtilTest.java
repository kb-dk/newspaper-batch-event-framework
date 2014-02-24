package dk.statsbiblioteket.medieplatform.batchcontext;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;

public class BatchContextUtilTest {
    
    /**
     * Test generation of a BatchContext object.
     */
    @Test
    public void testGenerateBatchContextGood() throws Exception {
        MfPakDAO mfPakDAO = mock(MfPakDAO.class);       
        when(mfPakDAO.getNewspaperID(anyString())).thenReturn("adresseavisen1759");
        NewspaperEntity entity = new NewspaperEntity();
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        entity.setNewspaperID("adresseavisen1759");
        entity.setPublicationLocation("København");
        entity.setNewspaperDateRange(new NewspaperDateRange(new SimpleDateFormat("yyyy").parse("1600"), new Date()));
        when(mfPakDAO.getBatchNewspaperEntities(anyString())).thenReturn(Arrays.asList(entity));
        NewspaperEntity entity2 = new NewspaperEntity();
        entity2.setPublicationLocation("København");
        entity2.setNewspaperID("adresseavisen1759");
        entity2.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity2);
        when(mfPakDAO.getBatchShipmentDate(anyString())).thenReturn(new Date(0));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        NewspaperDateRange filmDateRange = new NewspaperDateRange(sdf.parse("1795-06-01"), sdf.parse("1795-06-15"));
        List<NewspaperDateRange> ranges = new ArrayList<>();
        ranges.add(filmDateRange);
        when(mfPakDAO.getBatchDateRanges(anyString())).thenReturn(ranges);
        NewspaperBatchOptions options = mock(NewspaperBatchOptions.class);
        when(options.isOptionB7()).thenReturn(true);
        when(mfPakDAO.getBatchOptions(anyString())).thenReturn(options);

        Batch batch = new Batch("400022028240");
        batch.setRoundTripNumber(1);
        
        BatchContext context = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        Assert.assertNotNull(context.getAvisId());
        Assert.assertNotNull(context.getBatchOptions());
        Assert.assertNotNull(context.getEntities());
        Assert.assertNotNull(context.getDateRanges());
        Assert.assertNotNull(context.getShipmentDate());
        Assert.assertEquals(context.getAvisId(), "adresseavisen1759");
        Assert.assertTrue(context.getBatchOptions().isOptionB7());
        Assert.assertFalse(context.getEntities().isEmpty());
        Assert.assertEquals(context.getBatch(), batch);
        Assert.assertEquals(context.getDateRanges(), ranges);
        Assert.assertEquals(context.getShipmentDate(), new Date(0));
        
    }

}
