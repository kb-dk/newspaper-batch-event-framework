package dk.statsbibliokeket.newspaper.treenode;

import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of TreeNode which also maintains a list of its own children.
 */
public class TreeNodeWithChildren extends TreeNode {

    List<TreeNodeWithChildren> children;

    public TreeNodeWithChildren(String name, NodeType type, TreeNodeWithChildren parent, String location) {
        super(name, type, parent, location);
        children = new ArrayList<>();
        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Add a child to this node.
     * @param childNode
     */
    private void addChild(TreeNodeWithChildren childNode) {
       if (children != null) {
           children.add(childNode);
       }
    }

    /**
     * Get the list of children of this node.
     * @return the children.
     */
    public List<TreeNodeWithChildren> getChildren() {
        return children;
    }
}
