package dk.statsbiblioteket.medieplatform.autonomous;

/** This class formats batchs IDs to the form B324o0893404-RT1 */
public class NewspaperIDFormatter implements IDFormatter {


    @Override
    public String formatBatchID(String batchID) {
        return "path:B" + batchID;
    }

    @Override
    public Long unformatBatchID(String batchID) {
        return Long.parseLong(batchID.replaceFirst("^B", ""));
    }

    @Override
    public String formatFullID(String roundTripID) {
        return String.format("path:%s", roundTripID);
    }

    @Override
    public SplitID unformatFullID(String fullID) {
        String[] splits = fullID.split("-RT");

        return new SplitID(
                splits[0].replaceFirst("(path:)?B", ""), Integer.parseInt(splits[1]));
    }
}
