package dk.statsbibliokeket.newspaper.treenode;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TreeNodeWithChildren extends TreeNode {

    List<TreeNodeWithChildren> children;

    public TreeNodeWithChildren(String name, NodeType type, TreeNodeWithChildren parent, String location) {
        super(name, type, parent, location);
        children = new ArrayList<>();
    }

    public void addChild(TreeNodeWithChildren childNode) {
        final TreeNode parent = getParent();
        if (parent != null) {
            ((TreeNodeWithChildren) parent).addChild(this);
        }
    }

    public List<TreeNodeWithChildren> getChildren() {
        return children;
    }
}
