package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import java.util.List;

/**
 * Stupid implementation of the ContentModelFilter that returns true for all calls.
 */
public class YesFilter implements ContentModelFilter {
    public boolean isAttributeDatastream(String dsid, List<String> types) {
        return true;
    }

    public boolean isChildRel(String predicate, List<String> types) {
        return true;
    }
}
