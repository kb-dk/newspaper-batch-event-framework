package dk.statsbiblioteket.medieplatform.autonomous.iterator.fedora3;

/**
 * This class defines a fedora tree filter. Rather than being content model driven during execution, the content
 * model specific behaviour is meant to be defined in an implementation of this interface.
 */
public interface FedoraTreeFilter {


    /**
     * For each datastream in an object, this method will be called. If it returns true, the datastream will result
     * in an AttributeParsingEvent. If not, the datastream will be ignored.
     *
     * @param dsid the id of the datastream
     *
     * @return true if the datastream should be event'ed
     */
    boolean isAttributeDatastream(String dsid);


    /**
     * For each relation in an object, this method will be called. If it returns true, the relation will be used
     * to find a child. If not, the relation will be ignored
     *
     * @param predicate the full predicate of the relation
     *
     * @return true if the relation should denote a node in tree
     */
    boolean isChildRel(String predicate);
}
