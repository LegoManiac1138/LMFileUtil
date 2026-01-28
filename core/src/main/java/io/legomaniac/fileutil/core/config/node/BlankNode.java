package io.legomaniac.fileutil.core.config.node;

/**
 * This is a Node representing a blank line, or "\n" in bytes. This allows separation of sections, while allowing blank
 * lines to persist between saves
 */
public final class BlankNode implements ConfigNode {

    private int index;

    @Override
    public int getIndex(){
        return index;
    }

    @Override
    public void setIndex(int index){
        this.index = index;
    }

    @Override
    public void write(StringBuilder out, int indent){
        if(out!=null) out.append("\n");
    }

}
