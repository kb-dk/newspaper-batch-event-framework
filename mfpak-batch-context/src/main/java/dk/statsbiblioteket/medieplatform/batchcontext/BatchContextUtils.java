package dk.statsbiblioteket.medieplatform.batchcontext;

import java.sql.SQLException;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;

/**
 * Util class to work with BatchContext objects 
 */
public class BatchContextUtils {

    /**
     * Method to obtain a populated BatchContext object.
     * @param mfPakDAO The DAO to extract information with.
     * @param batch The batch to extract the information for. 
     * @throws SQLException in case of SQL errors.
     */
    public static BatchContext buildBatchContext(MfPakDAO mfPakDAO, Batch batch) throws SQLException {
        BatchContext context = new BatchContext();
        
        context.setEntities(mfPakDAO.getBatchNewspaperEntities(batch.getBatchID()));
        context.setAvisId(mfPakDAO.getNewspaperID(batch.getBatchID()));
        context.setBatchOptions(mfPakDAO.getBatchOptions(batch.getBatchID()));
        context.setBatch(batch);
        context.setDateRanges(mfPakDAO.getBatchDateRanges(batch.getBatchID()));
        context.setShipmentDate(mfPakDAO.getBatchShipmentDate(batch.getBatchID()));
        
        return context;
    }
}
