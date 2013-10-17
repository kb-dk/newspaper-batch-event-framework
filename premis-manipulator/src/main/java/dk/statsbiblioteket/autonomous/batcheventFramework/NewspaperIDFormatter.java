package dk.statsbiblioteket.autonomous.batcheventFramework;

/**
 * This class formats batchs IDs to the form B324o0893404-RT1
 */
public class NewspaperIDFormatter implements IDFormatter{


    @Override
    public String formatBatchID(Long batchID) {
        return "B"+batchID.toString();
    }

    @Override
    public Long unformatBatchID(String batchID) {
        return Long.parseLong(batchID.replaceFirst("^B", ""));
    }

    @Override
    public String formatFullID(Long batchID, int roundTripNumber) {
        return String.format("B%d-RT%d",batchID, roundTripNumber);
    }

    @Override
    public SplitID unformatFullID(String fullID) {
        String[] splits = fullID.split("-RT");

        return new SplitID(Long.parseLong(splits[0].replaceFirst("B","")),
                Integer.parseInt(splits[1]));
    }
}
