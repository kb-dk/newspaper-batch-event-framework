package dk.statsbiblioteket.medieplatform.autonomous;

public class BatchItemFactory implements ItemFactory<Batch> {
    @Override
    public Batch createItem(String fullID) {
        SplitID splits = unformatFullID(fullID);
        Batch result = new Batch(splits.getBatchID());
        result.setRoundTripNumber(splits.getRoundTripNumber());
        return result;
    }

    public String formatBatchID(String batchID) {
        return "path:B" + batchID;
    }

    public Long unformatBatchID(String batchID) {
        return Long.parseLong(batchID.replaceFirst("^B", ""));
    }

    public String formatFullID(String roundTripID) {
        return String.format("path:%s", roundTripID);
    }

    public SplitID unformatFullID(String fullID) {
        String[] splits = fullID.split("-RT");

        return new SplitID(splits[0].replaceFirst("(path:)?B", ""), Integer.parseInt(splits[1]));
    }

    public static class SplitID {
        private String batchID;
        private int roundTripNumber;

        public SplitID(String batchID, int roundTripNumber) {
            this.batchID = batchID;
            this.roundTripNumber = roundTripNumber;
        }

        public String getBatchID() {
            return batchID;
        }

        public int getRoundTripNumber() {
            return roundTripNumber;
        }
    }
}
