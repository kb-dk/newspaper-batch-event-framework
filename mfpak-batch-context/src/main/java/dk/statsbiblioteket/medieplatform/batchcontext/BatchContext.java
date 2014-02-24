package dk.statsbiblioteket.medieplatform.batchcontext;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;

/**
 * Class to represent the context of a batch based on information from the MFPAK database.  
 */
public class BatchContext {

    private List<NewspaperEntity> entities;
    private List<NewspaperDateRange> dateRanges;
    private String avisId;
    private Batch batch;
    private NewspaperBatchOptions batchOptions;
    private Date shipmentDate;
    
    public Date getShipmentDate() {
        if(shipmentDate == null) {
            return null;
        } else {
            return new Date(shipmentDate.getTime());
        }
    }

    public void setShipmentDate(Date shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    public List<NewspaperDateRange> getDateRanges() {
        if(dateRanges == null) {
            return null;
        } else {
            return Collections.unmodifiableList(dateRanges);    
        }
    }

    public void setDateRanges(List<NewspaperDateRange> dateRanges) {
        this.dateRanges = dateRanges;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }

    public List<NewspaperEntity> getEntities() {
        return Collections.unmodifiableList(entities);
    }
    
    public void setEntities(List<NewspaperEntity> entities) {
        this.entities = entities;
    }
    
    public NewspaperBatchOptions getBatchOptions() {
        return batchOptions;
    }
    
    public void setBatchOptions(NewspaperBatchOptions batchOptions) {
        this.batchOptions = batchOptions;
    }
    
    public String getAvisId() {
        return avisId;
    }
    
    public void setAvisId(String avisId) {
        this.avisId = avisId;
    }
}
