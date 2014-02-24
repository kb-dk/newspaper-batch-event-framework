package dk.statsbiblioteket.medieplatform.batchcontext;

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
        Assert.assertEquals(context.getAvisId(), "adresseavisen1759");
        
        Assert.assertNotNull(context.getBatchOptions());
        Assert.assertTrue(context.getBatchOptions().isOptionB7());
        
        Assert.assertNotNull(context.getEntities());
        Assert.assertFalse(context.getEntities().isEmpty());
        
        Assert.assertNotNull(context.getDateRanges());
        Assert.assertEquals(context.getDateRanges(), ranges);
        
        Assert.assertNotNull(context.getShipmentDate());
        Assert.assertEquals(context.getShipmentDate(), new Date(0));
        
        Assert.assertEquals(context.getBatch(), batch);
    }

}
