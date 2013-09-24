package dk.statsbiblioteket.newspaper.batcheventFramework;

public class NewspaperIDFormatter implements IDFormatter<Long>{


    @Override
    public String formatBatchID(Long batchID) {
        return "B"+batchID.toString();
    }

    @Override
    public Long unformatBatchID(String batchID) {
        return Long.parseLong(batchID.replaceFirst("^B", ""));
    }

    @Override
    public String formatFullID(Long batchID, int runNr) {
        return String.format("B%d-RT%d",batchID,runNr);
    }

    @Override
    public SplitID<Long> unformatFullID(String fullID) {
        String[] splits = fullID.split("-RT");

        return new SplitID<>(Long.parseLong(splits[0].replaceFirst("B","")),
                Integer.parseInt(splits[1]));
    }
}
