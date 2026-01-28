package io.legomaniac.fileutil.core.config.node;

/**
 * This node represents the "Root" directory of any {@link io.legomaniac.fileutil.core.config.ConfigFile}. This allows
 * sorting and positioning inside the file is correct.
 */
public final class RootNode extends SectionNode {

    private int lastIndex = 0;

    public RootNode(){
        super(0, null, null);
    }

    /**
     * Retrieves the last index of the {@link ConfigNode} at the end of the Root directory's path.
     * @return The last node in the root Section's list
     */
    public int getLastIndex(){
        return lastIndex;
    }

    @Override
    public SectionNode add(ConfigNode node){
        if(node==null) return this;
        int childIndex = !children.isEmpty() ? children.size() : 0;

        node.setIndex(childIndex);

        children.add(node);
        lastIndex++;
        return this;
    }

    /**
     * Saves the root directory and all of its nodes to the config file, starting at an indent of 0, as that is the root
     * path.
     * @param out The buffer to write to
     */
    public void saveRoot(StringBuilder out){
        write(out, 0);
        if(out.isEmpty()) return;
        out.replace(out.lastIndexOf("\n"), out.length(), ""); // Removes the ending new line
    }

}
