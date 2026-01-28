package io.legomaniac.fileutil.core.config.node;

/**
 * This represents a ConfigNode that is inherited by any node in the config structure. Every node should have a path and
 * key variable saved, as it allows the parser to correctly identify what section and placement it should have. This
 * includes in-memory and on disk. If a class extends this isn't a {@link SectionNode} or its subclasses then it SHOULD
 * be the final tail end for this path.
 */
public interface ConfigNode {

    /**
     * Writes this Node to the config file that this belongs to
     * @param out The buffer containing all the information to write to disk
     * @param indent How many indents are placed before the key pair. Must be divisible by 2 to ensure proper parsing
     */
    void write(StringBuilder out, int indent);

    /**
     * This represents the dot separated path for this node. If there is no "." in this, then that implies that this node
     * is in the "root" Section. If this node is inside of a {@link SectionNode} and it's not inside the root SectionNode,
     * this will cause an issue.
     * @return path of this Node
     */
    default String getPath(){
        return null;
    }

    /**
     * This is the final "key" in the dot separated path.
     * @return The key belonging to this Node
     */
    default String getKey(){
        return null;
    }

    /**
     * The index for this node should correspond to its placement in the parent {@link SectionNode}. When saving and
     * traversing the config structure, it will go by every node index in the parent node recursively.
     * @return The current index for this node
     */
    int getIndex();

    /**
     * Updates or sets the index for this node. This is the current position inside the parent node, not the "overall"
     * index. This ensures that saving every node is properly positioned where it was when loading or creating the file.
     * @param index The index of this Node
     */
    void setIndex(int index);

}
