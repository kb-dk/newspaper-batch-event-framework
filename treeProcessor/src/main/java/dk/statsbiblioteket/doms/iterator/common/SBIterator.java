package dk.statsbiblioteket.doms.iterator.common;

import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: abr
 * Date: 9/4/13
 * Time: 1:13 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SBIterator extends Iterator<Event> {

    public SBIterator skipToNextSibling();


}
