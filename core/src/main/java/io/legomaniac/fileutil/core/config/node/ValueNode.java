package io.legomaniac.fileutil.core.config.node;

import java.util.List;

/**
 * This is a Node that has a key-value pair, allowing saving information from disk into variables later on.
 */
public class ValueNode implements ConfigNode {

    private final String key;
    private final String path;
    protected int index;

    protected Object value; // Value
    protected List<String> inlineComments; // Comment after the value

    public ValueNode(String path, String key, Object defaultValue){
        this.path = path;
        this.key = key;
        this.value = defaultValue;
    }

    @Override
    public String getPath(){
        return path;
    }

    @Override
    public String getKey(){
        return key;
    }

    /**
     * This returns the value as the object type or class it was parsed as. Primitives shouldn't be returned, as this could
     * cause issues with loading and saving information
     * @return The value as an Object
     */
    public final Object getValue(){
        return value;
    }

    @Override
    public int getIndex(){
        return index;
    }

    @Override
    public void setIndex(int index){
        this.index = index;
    }

    /**
     * This sets the current value of this node to the object type that is provided. Supported objects are {@link Boolean},
     * {@link Integer}, {@link Long}, {@link java.math.BigDecimal}, {@link Double}, {@link Float}, {@link Enum}, and {@link String}
     * @param value The value to set
     */
    public final void setValue(Object value){
        if(value instanceof String str){
            if(str.startsWith(" ") || str.endsWith(" ")){
                this.value = "\"" + str + "\"";
            } else {
                this.value = str;
            }
        } else {
            this.value = value;
        }
    }

    /**
     * Adds comments after the value that will properly be saved and parsed between loads
     * @param comments List of comments to append to the line
     */
    public void setInlineComments(List<String> comments){
        this.inlineComments = comments;
    }

    /**
     * @return The list of comments after the value
     */
    public List<String> getInlineComments(){
        return inlineComments;
    }

    @Override
    public void write(StringBuilder out, int indent){
        if(out==null) return;
        out.append(" ".repeat(indent))
                .append(key)
                .append(": ");
        if(value instanceof List<?> list){
            out.append(list);
        } else if(value instanceof Enum<?> e){
            out.append("\"").append(e.name()).append("\"");
        } else if(value instanceof Integer integer){
            out.append(integer);
        } else if(value instanceof Long lng){
            out.append(lng);
        } else if(value instanceof Double dbl){
            out.append(dbl);
        } else if(value instanceof Float fl){
            out.append(fl);
        } else if(value instanceof String str){
            out.append("\"").append(str).append("\"");
        } else {
            out.append(value);
        }

        // Inline comments
        if(inlineComments !=null && !inlineComments.isEmpty()){
            out.append(" # ").append(String.join(" | ", inlineComments));
        }
        out.append("\n");
    }

}
