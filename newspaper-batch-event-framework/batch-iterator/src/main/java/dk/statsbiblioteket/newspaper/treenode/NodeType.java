package dk.statsbiblioteket.newspaper.treenode;

/**
 * Defines the types of nodes found in a batch.
 */
public enum NodeType {
    BATCH,
    WORKSHIFT_ISO_TARGET,
    WORKSHIFT_TARGET,
    TARGET_IMAGE,
    FILM,
    FILM_ISO_TARGET,
    FILM_TARGET,
    ISO_TARGET_IMAGE,
    UNMATCHED,
    EDITION,
    PAGE,
    BRIK,
    BRIK_IMAGE,
    PAGE_IMAGE;
}
