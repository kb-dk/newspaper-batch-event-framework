package dk.statsbibliokeket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 2/17/14
 * Time: 3:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreeNodeStateWithChildrenTest {
    @Test
    public void filmNodeTest() {
        TreeNodeStateWithChildren treeNodeState = new TreeNodeStateWithChildren();

        treeNodeState.handleNodeBegin(new NodeBeginsParsingEvent("B400022028241-RT1"));
        NodeBeginsParsingEvent workNodeBegin = new NodeBeginsParsingEvent("400022028241-14");
        treeNodeState.handleNodeBegin(workNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getName(), workNodeBegin.getName());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.FILM);
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.BATCH);
        assertTrue(treeNodeState.getCurrentNode() instanceof TreeNodeWithChildren);
        assertTrue(treeNodeState.getPreviousNode() instanceof TreeNodeWithChildren);
    }
}
