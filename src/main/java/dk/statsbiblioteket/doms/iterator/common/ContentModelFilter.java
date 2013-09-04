package dk.statsbiblioteket.doms.iterator.common;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 4:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ContentModelFilter {


    boolean isAttributeDatastream(String dsid, List<String> types);


    boolean isChildRel(String predicate, List<String> types);
}
