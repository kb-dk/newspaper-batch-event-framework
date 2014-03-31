package dk.statsbiblioteket.medieplatform.autonomous
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/** This class represents a batch, a specific thing on which work will be done */
@Canonical()
@EqualsAndHashCode(includes = ["batchID","roundTripNumber"])
@ToString(includeNames = true,includes = ["batchID","roundTripNumber","eventList"])
class Batch {

    final String batchID;
    /**
     * The round trip number. This will never be less than 1. It counts the number of times a batch
     * have been redelivered
     */
    def Integer roundTripNumber = 1;

    List<Event> eventList;
    String domsID;

    /**
     * Get the full ID in the form B<batchID>-RT<roundTripNumber>
     *
     * @return the full ID
     */
    String getFullID() {
        "B$batchID-RT$roundTripNumber";
    }

}
