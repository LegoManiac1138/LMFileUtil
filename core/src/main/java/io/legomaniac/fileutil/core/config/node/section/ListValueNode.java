package io.legomaniac.fileutil.core.config.node.section;

import io.legomaniac.fileutil.core.config.node.ValueNode;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

/**
 * A Node pertaining to an object inside a "List". This is useful for holding multiple lines of information, such as Strings,
 * data-types, or numeric values. Comments are NOT allowed for this node.
 */
public final class ListValueNode extends ValueNode {

    public ListValueNode(Object defaultValue){
        super(null, null, defaultValue);
    }

    @Override
    @Nullable
    public String getPath(){
        return null;
    }

    @Override
    @Nullable
    public String getKey(){
        return null;
    }

    @Override
    public void write(StringBuilder out, int indent){
        if(out==null) return;
        out.append(" ".repeat(indent));
        if(value instanceof BigDecimal bd){
            out.append(bd);
        } else if(value instanceof Integer i){
            out.append(i);
        } else if(value instanceof Long l){
            out.append(l);
        } else if(value instanceof Double d){
            out.append(d);
        } else if(value instanceof Enum<?> || value instanceof String){
            out.append(value);
        }
        out.append("\n");
    }

    @Override
    public void setInlineComments(List<String> comments){}

    @Override
    @Nullable
    public List<String> getInlineComments(){
        return null;
    }

}
