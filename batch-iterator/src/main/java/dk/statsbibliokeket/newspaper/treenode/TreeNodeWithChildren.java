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
        parent.addChild(this);
    }

    public void addChild(TreeNodeWithChildren childNode) {
       if (children != null) {
           children.add(childNode);
       }
    }

    public List<TreeNodeWithChildren> getChildren() {
        return children;
    }
}
