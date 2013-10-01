package dk.statsbiblioteket.doms.iterator.common;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 4:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class YesFilter implements ContentModelFilter {
    public boolean isAttributeDatastream(String dsid, List<String> types) {
        return true;
    }

    public boolean isChildRel(String predicate, List<String> types) {
        return true;
    }
}
