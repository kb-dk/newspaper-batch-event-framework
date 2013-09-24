package dk.statsbiblioteket.newspaper.batcheventFramework;

public interface IDFormatter<T> {

    String formatBatchID(T batchID);

    T unformatBatchID(String batchID);

    String formatFullID(T batchID, int runNr);

    SplitID<T> unformatFullID(String fullID);

    public static class SplitID<T>{
        private T batchID;
        private int runNr;

        public SplitID(T batchID, int runNr) {
            this.batchID = batchID;
            this.runNr = runNr;
        }

        public T getBatchID() {
            return batchID;
        }

        public int getRunNr() {
            return runNr;
        }
    }

}
