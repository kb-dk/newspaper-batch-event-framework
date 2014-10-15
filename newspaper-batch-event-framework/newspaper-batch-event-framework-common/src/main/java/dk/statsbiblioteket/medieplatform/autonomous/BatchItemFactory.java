package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This is the item factory for Newspaper batches
 */
public class BatchItemFactory implements ItemFactory<Batch> {

    /**
     * Create a batch, which is a subtype of Item
     *
     * @param id the batch round trip id, of the form Bxxxxxx-RTx
     *
     * @return a new batch object, without a doms pid
     */
    @Override
    public Batch create(String id) {
        Batch.BatchRoundtripID splits = new Batch.BatchRoundtripID(id);
        Batch result = new Batch(splits.getBatchID());
        result.setRoundTripNumber(splits.getRoundTripNumber());
        return result;
    }
}

