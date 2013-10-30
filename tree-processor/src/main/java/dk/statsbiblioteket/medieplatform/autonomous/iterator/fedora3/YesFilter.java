package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

/**
 * Stupid implementation of the FedoraTreeFilter that returns true for all calls.
 */
public class YesFilter implements FedoraTreeFilter {
    public boolean isAttributeDatastream(String dsid) {
        return true;
    }

    public boolean isChildRel(String predicate) {
        return true;
    }
}
