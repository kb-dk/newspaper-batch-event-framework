package dk.statsbibliokeket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class TreeNodeStateWithChildrenTest {

    /**
     * Test that a node can remember its children.
     */
    @Test
    public void testGetChildren() {
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
        treeNodeState.handleNodeEnd(new NodeEndParsingEvent("400022028241-14"));
        assertEquals(((TreeNodeWithChildren) treeNodeState.getCurrentNode()).getChildren().size(), 1);
    }
}
