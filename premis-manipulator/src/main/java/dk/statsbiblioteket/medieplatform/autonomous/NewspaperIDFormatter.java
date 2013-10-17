package dk.statsbiblioteket.medieplatform.autonomous;

/**
 * This class formats batchs IDs to the form B324o0893404-RT1
 */
public class NewspaperIDFormatter implements IDFormatter{


    @Override
    public String formatBatchID(String batchID) {
        return "B"+ batchID;
    }

    @Override
    public Long unformatBatchID(String batchID) {
        return Long.parseLong(batchID.replaceFirst("^B", ""));
    }

    @Override
    public String formatFullID(String batchID, int roundTripNumber) {
        return String.format("B%s-RT%d",batchID, roundTripNumber);
    }

    @Override
    public SplitID unformatFullID(String fullID) {
        String[] splits = fullID.split("-RT");

        return new SplitID(splits[0].replaceFirst("B",""),
                Integer.parseInt(splits[1]));
    }
}
