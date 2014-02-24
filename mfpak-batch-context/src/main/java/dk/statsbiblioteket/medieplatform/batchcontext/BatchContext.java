package dk.statsbiblioteket.medieplatform.batchcontext;

import java.util.List;

import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;

/**
 * Class to represent the context of a batch based on information from the MFPAK database.  
 */
public class BatchContext {

    private List<NewspaperEntity> entities;
    private NewspaperBatchOptions batchOptions;
    private String avisId;
    
    public List<NewspaperEntity> getEntities() {
        return entities;
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
