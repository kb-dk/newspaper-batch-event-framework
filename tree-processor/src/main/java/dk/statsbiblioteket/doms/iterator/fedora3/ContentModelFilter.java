package dk.statsbiblioteket.doms.iterator.fedora3;

import java.util.List;

/**
 * This class defines a content model filter. Rather than being content model driven during execution, the content
 * model specific behaivour is meant to be defined in an implementation of this interface.
 *
 */
public interface ContentModelFilter {


    /**
     * For each datastream in an object, this method will be called. If it returns true, the datastream will result
     * in an AttributeParsingEvent. If not, the datastream will be ignored.
     * @param dsid the id of the datastream
     * @param types the list of pids of the content models of the object
     * @return true if the datastream should be event'ed
     */
    boolean isAttributeDatastream(String dsid, List<String> types);


    /**
     * For each relation in an object, this method will be called. If it returns true, the relation will be used
     * to find a child. If not, the relation will be ignored
     * @param predicate the full predicate of the relation
     * @param types the list of pids of the content models of the object
     * @return true if the relation should denote a node in tree
     */
    boolean isChildRel(String predicate, List<String> types);
}
