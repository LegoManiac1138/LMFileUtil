package io.legomaniac.fileutil.core.config.node;

import io.legomaniac.fileutil.core.LMFileUtil;
import io.legomaniac.fileutil.core.config.node.section.ListSectionNode;
import io.legomaniac.fileutil.core.util.MessageUtil;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * This is a node that will contain other nodes along its path. Should there be no nodes inside, the section should be
 * considered as "empty". This allows traversing other section nodes and including itself.
 */
public class SectionNode implements ConfigNode {

    protected final String path;
    protected final String key;
    protected final List<ConfigNode> children = new ArrayList<>();
    protected int sectionIndex;

    public SectionNode(int sectionIndex, String path, String key){
        this.sectionIndex = sectionIndex;
        this.path = path;
        this.key = key;
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
     * These are all the child nodes inside of this section. This can include other {@link SectionNode}s or {@link ValueNode}s.
     * @return A list of every child node inside of this section.
     */
    public final List<ConfigNode> getChildren(){
        return children;
    }

    /**
     * This will retrieve only the {@link ValueNode}s inside of this Node.
     * @return Null if there are no children or a list of all ValueNodes found inside this Section
     */
    public final @Nullable List<ValueNode> getValueNodes(){
        if(children.isEmpty()) return null;
        return children.stream()
                .filter(ValueNode.class::isInstance)
                .map(ValueNode.class::cast)
                .toList();
    }

    @Override
    public int getIndex(){
        return sectionIndex;
    }

    @Override
    public void setIndex(int sectionIndex){
        this.sectionIndex = sectionIndex;
    }

    /**
     * Clears all the current entries inside of this SectionNode
     */
    public final void clear(){
        if(!this.children.isEmpty()) this.children.clear();
    }

    /**
     * Sorts every node in the current section by their index in ascending order, then recursively checks every subsection.
     */
    public final void sort(){
        if(this.children.isEmpty()) return;
        children.sort(Comparator.comparingInt(ConfigNode::getIndex));
        children.forEach(cn -> {
            if(!(cn instanceof SectionNode sn) || sn instanceof ListSectionNode) return;
            sn.sort();
        });
    }

    /**
     * Adds a new node to this Section. If the node is not null, the next index is first calculated before insertion.
     * @param node The node to insert
     * @return This section, allowing chain-adding nodes in a single line.
     */
    public SectionNode add(ConfigNode node){
        if(node==null) return this;
        int childIndex = 0;
        if(!children.isEmpty()) childIndex = children.size();
        node.setIndex(childIndex);
        children.add(node);
        return this;
    }

    /**
     * Adds a new node to this Section at the position provided. If there are nodes at or after this index, those nodes
     * will be pushed forward in the list before inserting the new node.
     * @param node The node to insert
     * @param index The index for which to place this node
     * @return This section, allowing chain-adding nodes in a single line.
     */
    public SectionNode add(ConfigNode node, int index){
        if(node==null) return this;
        node.setIndex(index);

        if(children.isEmpty()){
            if(index!=0) return this;
            children.add(index, node);
            return this;
        }

        // In bounds
        if(index > children.size() || index < 0) return this;

        for(int i = 0; i < children.size(); i++){
            if(i < index) continue;
            ConfigNode configNode = children.get(i);
            configNode.setIndex((i+1));
            children.add((i+1), configNode);
        }
        children.add(index, node);
        return this;
    }

    /**
     * Checks the current section for a key.
     * @param key The key to search for
     * @return True if the key was found, false if it's not present in the current "level"
     */
    public boolean hasChild(String key){
        if(children.isEmpty()) return false;
        return children.stream().anyMatch(cn -> cn.getKey()!=null && cn.getKey().equals(key));
    }

    /**
     * Checks this section for a nested SectionNode inside this. This will return null if the key is not valid, there's
     * no other nodes inside it, or if the node found is somehow null. If the key passed in contains a "." inside it
     * @param key The key to search for. If the
     * @return The section node nested inside this section
     */
    public SectionNode getSection(String key){
        if(key==null || key.isEmpty() || this.children.isEmpty()) return null;
        String[] parts = key.split("\\.");
        for(ConfigNode cn : children){
            if(!(cn instanceof SectionNode sn)) continue;
            String parentNode = parts[0];
            if(parts.length==1){
                if(sn.getKey().equals(parentNode)) return sn;
            } else {
                if(!sn.getKey().equals(parentNode)) continue;
                return sn.getSection(removeParent(key));
            }
        }
        return null;
    }

    /**
     * Returns a node that contains a list of a specific data type.
     * @param key The key to search for
     * @return The node if found, null if the key is invalid or the node wasn't found
     */
    public ListSectionNode getListSection(String key){
        if(key==null || key.isEmpty() || this.children.isEmpty()) return null;
        String[] parts = key.split("\\.");
        for(ConfigNode cn : children){
            if(!(cn instanceof ListSectionNode ln)) continue;
            String parentNode = parts[0];
            if(parts.length==1 && ln.getKey().equals(parentNode)) return ln;
        }
        return null;
    }

    /**
     * Retrieve a child node from a section. If a child wasn't found, it will return null. When searching through all
     * the nodes in this section, if the key contains a path, it will search the next available section until either the
     * node is found, or null is returned.
     * @param key The string key of a Node. For every "." found, the next key is without the first word
     * @return A node object if found
     */
    public ConfigNode getChild(String key){
        if(key==null || this.children.isEmpty()) return null;
        String[] parts = key.split("\\.");
        if(parts.length==1){
            // This is the final SectionNode, search based off the child's key
            return children.stream()
                    .filter(cn -> cn.getKey() != null && cn.getKey().equals(key))
                    .findFirst()
                    .orElse(null);
        } else {  // The child is in a sub-SectionNode, retrieve recursively
            // This is the parent node to look for
            String parentNode = parts[0];
            for(ConfigNode cn : children){
                // If the node isn't a SectionNode or the key doesn't match, continue on
                if(!(cn instanceof SectionNode sn) || !sn.getKey().equals(parentNode)) continue;
                // The next parent node was found
                return sn.getChild(removeParent(key));
            }
        }
        return null;
    }

    /**
     * Retrieves a comment node based off the comment line and the index in the current section only. This will not
     * recurse into lower sections.
     * @param string The comment to match for
     * @param index The index to check against
     * @return The node if found, null if not
     */
    public CommentNode getComment(String string, int index){
        if(this.children.isEmpty() || string==null || string.isEmpty()) return null;
        for(ConfigNode cn : children){
            if(!(cn instanceof CommentNode comm) || comm.getComment()==null || !comm.getComment().equals(string)) continue;
            if(comm.getIndex()==index) return comm;
        }
        return null;
    }

    public BlankNode getBlankNode(int index){
        if(this.children.isEmpty() || index < this.children.size() || index > this.children.size()) return null;
        for(ConfigNode cn : children) if(cn instanceof BlankNode bn && bn.getIndex()==index) return bn;
        return null;
    }

    /**
     * Gets the next index to insert a child node
     * @return The next index, 0 if there are no items inside this Section yet
     */
    public final int getNextIndex(){
        if(children.isEmpty()) return 0;
        return children.size();
    }

    // Getters

    /**
     * Retrieves a value from this section AS-IS
     * @param key The key to search for
     * @param classOfT The class type that should be cast to
     * @return The value belonging to the key if found
     */
    public final <T> @Nullable T getValue(String key, Class<T> classOfT){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value==null || !classOfT.isAssignableFrom(value.getClass())){
            LMFileUtil fu = LMFileUtil.getInst();
            MessageUtil mu = fu.getMessageUtil();
            mu.console(fu.getPluginName(), "&cUnable to get value for " + path + " with type: " + classOfT);
        }
        return value==null ? null : classOfT.cast(value);
    }

    /**
     * Used to retrieve a BigDecimal value, which is used to avoid rounding issues when it comes to Double and Float
     * @param key The key to search for
     * @return A BigDecimal object if found, null otherwise
     */
    public BigDecimal getAsDecimal(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof BigDecimal dec) return dec;
        try {
            return new BigDecimal(String.valueOf(vn.getValue()));
        } catch (Exception ex){
            return null;
        }
    }

    /**
     * Retrieves a boolean value from a key
     * @param key The key without any "." in it
     * @return Null if not present or the value is invalid, True or false if found
     */
    public Boolean getBoolean(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof Boolean b) return b;
        try {
            return Boolean.parseBoolean(String.valueOf(vn.getValue()));
        } catch (Exception ex){
            return false;
        }
    }

    /**
     * Retrieves a double value from a key-pair
     * @param key The key to search for
     * @return A double if found, null otherwise
     */
    public Double getDouble(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof Double d) return d;
        try {
            return Double.parseDouble(String.valueOf(vn.getValue()));
        } catch (Exception ex){
            return 0.0;
        }
    }

    /**
     * Retrieves an Enum type from a key-pair
     * @param key The key to search for
     * @param enumClass The class type of the Enum
     * @return an Enum type if found, otherwise will return null
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof Enum e) return (T)e;
        if(value instanceof String s){
            try {
                return Enum.valueOf(enumClass, s.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored){}
        }
        return null;
    }

    /**
     * Retrieves an integer from a key-pair
     * @param key The key to search for
     * @return an integer if found, null otherwise
     */
    public Integer getInt(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof Integer i) return i;
        try {
            return Integer.parseInt(String.valueOf(vn.getValue()));
        } catch (Exception ex){
            return 0;
        }
    }

    /**
     * Retrieves a long from a key-pair
     * @param key the key to search for
     * @return a long value if found, null otherwise
     */
    public Long getLong(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof Long l) return l;
        try {
            return Long.parseLong(String.valueOf(vn.getValue()));
        } catch (Exception ex){
            return 0L;
        }
    }

    /**
     * Retrieves a String from a key-pair. String.strip() will be needed to remove any trailing or leading space.
     * @param key the key to search for
     * @return A string if found, null otherwise.
     */
    public String getString(String key){
        if(!hasChild(key)) return null;
        ConfigNode node = getChild(key);
        if(!(node instanceof ValueNode vn) || vn.getValue()==null) return null;
        Object value = vn.getValue();
        if(value instanceof String str) return str;
        try {
            return String.valueOf(vn.getValue());
        } catch (Exception ignored){
            return null;
        }
    }

    /**
     * Removes the parent path from the current "full path", leaving only the node's key
     * @param path The parent path as a dot separated string
     * @return The key of the end node
     */
    protected String removeParent(String path){
        if(path==null) return null;
        int index = path.indexOf(".");
        return index >= 0 ? path.substring(index + 1) : path;
    }

    @Override
    public void write(StringBuilder out, int indent){
        if(key!=null && !key.isEmpty()){
            out.append(" ".repeat(indent))
                    .append(key) // The key of the section
                    .append(":"); // Adds colon to the end of the section
            if(children.isEmpty()) out.append(" "); // optional trailing space for empty sections
            out.append("\n");
            indent += 2; // increase indent for children
        }
        if(children.isEmpty()) return;
        children.sort(Comparator.comparingInt(ConfigNode::getIndex));
        for(ConfigNode cn : children) cn.write(out, indent);
    }

}
