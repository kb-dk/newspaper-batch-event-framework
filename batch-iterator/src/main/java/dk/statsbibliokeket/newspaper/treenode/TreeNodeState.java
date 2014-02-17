package dk.statsbibliokeket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;

/**
 * Provides functionality for accessing the current state for the current node in the batch structure.
 */
public class TreeNodeState extends DefaultTreeEventHandler {
    private TreeNode currentNode = null;
    private TreeNode previousNode = null;

    public TreeNode getCurrentNode() {
        return currentNode;
    }

    /**
     * Useful on NodeEnds event where we're really interested in knowing the node we left.
     */
    public TreeNode getPreviousNode() {
        return previousNode;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        updateCurrentNode(event);
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        previousNode = currentNode;
        currentNode = currentNode.getParent();
    }

    // Todo This is becoming a complicated. consider switch to state machine pattern.
    private void updateCurrentNode(NodeBeginsParsingEvent event) {
        NodeType nextNodeType = null;
        if (currentNode == null) {
            nextNodeType = NodeType.BATCH;
        }  else if (currentNode.getType().equals(NodeType.BATCH)) {
            if (event.getName().endsWith("WORKSHIFT-ISO-TARGET")) {
                nextNodeType = NodeType.WORKSHIFT_ISO_TARGET;
            } else {
                nextNodeType = NodeType.FILM;
            }
        } else if (currentNode.getType().equals(NodeType.FILM)) {
            if (event.getName().endsWith("FILM-ISO-target")) {
                nextNodeType = NodeType.FILM_ISO_TARGET;
            } else if (event.getName().endsWith("UNMATCHED")) {
                nextNodeType = NodeType.UNMATCHED;
            } else {
                nextNodeType = NodeType.EDITION;
            }
        } else if (currentNode.getType().equals(NodeType.EDITION) ||
                currentNode.getType().equals(NodeType.UNMATCHED)) {
            if(event.getName().contains("brik")) {
                nextNodeType = NodeType.BRIK;
            } else {
                nextNodeType = NodeType.PAGE;
            }
        } else if (currentNode.getType().equals(NodeType.FILM_ISO_TARGET)) {
            nextNodeType = NodeType.FILM_TARGET;
        } else if (currentNode.getType().equals(NodeType.WORKSHIFT_ISO_TARGET)) {
            nextNodeType = NodeType.WORKSHIFT_TARGET;
        } else if (currentNode.getType().equals(NodeType.PAGE)) {
            nextNodeType = NodeType.PAGE_IMAGE;
        } else if (currentNode.getType().equals(NodeType.FILM_TARGET)) {
            nextNodeType = NodeType.ISO_TARGET_IMAGE;
        } else if (currentNode.getType().equals(NodeType.WORKSHIFT_TARGET)) {
            nextNodeType = NodeType.TARGET_IMAGE;
        } else if(currentNode.getType().equals(NodeType.BRIK)){
            nextNodeType = NodeType.BRIK_IMAGE;
        } else {
            throw new IllegalStateException("Unexpected event: " + event + " for current node: " + currentNode);
        }
        assert (nextNodeType != null);
        previousNode = currentNode;
        currentNode = createNode(event.getName(), nextNodeType, currentNode, event.getLocation());
    }

    public TreeNode createNode(String name, NodeType nodeType, TreeNode previousNode, String location) {
        return new TreeNode(name, nodeType, previousNode, location);
    }
}