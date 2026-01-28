package io.legomaniac.fileutil.core.config.node;

/**
 * This represents a node that contains a comment String, which YAML loaders will recognize as a comment, and not treat
 * anything past the "#" as value objects. This should only be a single line, as anything more will break the parser
 */
public final class CommentNode implements ConfigNode {

    private final String comment;
    private int index;

    public CommentNode(String line){
        this.comment = (line==null || line.isEmpty()) ? "# " : line;
    }

    /**
     * The comment for this Node. This is useful for providing information to users or leaving notes inside config files.
     * @return Comment string
     */
    public String getComment(){
        return comment;
    }

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
        if(out==null) return;
        if(!comment.strip().startsWith("#")){
            out.append(" ".repeat(indent)).append("# ").append(comment).append("\n");
        } else {
            out.append(" ".repeat(indent)).append(comment).append("\n");
        }
    }

}
