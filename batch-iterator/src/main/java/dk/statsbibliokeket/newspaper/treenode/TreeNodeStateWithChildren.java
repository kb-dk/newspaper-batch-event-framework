package dk.statsbibliokeket.newspaper.treenode;

/**
 * Specialised subclass of TreeNodeState which uses TreeNodeWithChildren elements so that one
 * can take action based on the children of a given node.
 */
public class TreeNodeStateWithChildren extends TreeNodeState {

    @Override
    protected TreeNode createNode(String name, NodeType nodeType, TreeNode previousNode, String location) {
        return new TreeNodeWithChildren(name, nodeType, (TreeNodeWithChildren) previousNode, location);
    }
}
