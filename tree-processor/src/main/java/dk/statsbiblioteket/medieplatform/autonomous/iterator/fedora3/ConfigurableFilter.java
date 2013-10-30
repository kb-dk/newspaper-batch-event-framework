package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

import java.util.List;

/**
 * Filter that filters on configurable list of values.
 */
public class ConfigurableFilter implements FedoraTreeFilter {
    private final List<String> predicateNames;
    private final List<String> names;

    /**
     * Initialise filter with given values
     * @param attributeNames Names of attributes to include
     * @param predicateNames Names of predicates to include. Not that a unique substring is enough.
     */
    public ConfigurableFilter(List<String> attributeNames, List<String> predicateNames) {
        this.predicateNames = predicateNames;
        this.names = attributeNames;
    }

    @Override
    public boolean isAttributeDatastream(String dsid) {
        return names.contains(dsid);
    }

    @Override
    public boolean isChildRel(String predicate) {
        for (String predicateName : predicateNames) {
            if (predicate.equals(predicateName)) {
                return true;
            }
        }
        return false;
    }
}
