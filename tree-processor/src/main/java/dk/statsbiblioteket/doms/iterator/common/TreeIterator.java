package dk.statsbiblioteket.doms.iterator.common;

import java.util.Iterator;

/**
 * The iterator interface for the tree structure. Iterates over the tree, which will be expressed as a
 * series of parsing events.
 *
 *
 *
 * @see ParsingEvent
 * @see EventType
 * @see AttributeParsingEvent
 * @see NodeBeginsParsingEvent
 * @see NodeBeginsParsingEvent
 *
 *
 */
public interface TreeIterator extends Iterator<ParsingEvent> {

    /**
     * Do:
     *  Extract the subtree originating from the current node as a separate TreeIterator.
     *  Skip to the next node begin event that is not in this subtree.
     * @return the subtree originating from the current node
     */
    public TreeIterator skipToNextSibling();



}
