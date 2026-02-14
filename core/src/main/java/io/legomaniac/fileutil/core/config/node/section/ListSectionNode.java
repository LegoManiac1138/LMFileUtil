package io.legomaniac.fileutil.core.config.node.section;

import io.legomaniac.fileutil.core.config.node.BlankNode;
import io.legomaniac.fileutil.core.config.node.CommentNode;
import io.legomaniac.fileutil.core.config.node.ConfigNode;
import io.legomaniac.fileutil.core.config.node.SectionNode;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This node contains a {@link List} of {@link ListSectionNode}s. The object type of every node must match, which means
 * the first inserted node will define the data type for the following nodes.
 */
public final class ListSectionNode extends SectionNode {

    private Class<?> elementType;

    public ListSectionNode(int sectionIndex, String path, String key){
        super(sectionIndex, path, key);
    }

    /**
     * Adds a new ListValueNode to this Section. If the element type hasn't been set yet, then the class type is set and
     * the node is added. If the class type has already been set, then the new node's value is checked. The node will only
     * be inserted if the class type matches the one set in this Section, ensuring homogeneous element type
     * @param node The node to insert
     * @return The current section to change objects together
     */
    public ListSectionNode addValueNode(ListValueNode node){
        if(node==null) return this;

        Object value = node.getValue();
        if(value!=null){
            if(elementType==null){
                elementType = value.getClass();
            } else if(!elementType.isInstance(value)){
                return this;
            }
        }

        if(value!=null && !elementType.isInstance(value)) return this;

        int childIndex = children.isEmpty() ? 0 : children.size();
        node.setIndex(childIndex);
        children.add(node);
        return this;
    }

    /**
     * Checks this section for an object value found in this list. This is useful for Strings or enum data types.
     * @param value The value to check for
     * @return True if the value is from, false if not found or if this Section has no sub nodes
     */
    public boolean containsNode(Object value){
        if(children.isEmpty()) return false;
        return children.stream()
                .filter(ListValueNode.class::isInstance)
                .map(ListValueNode.class::cast)
                .anyMatch(lvn -> lvn.getValue().equals(value));
    }

    /**
     * Returns all nodes in this Section that are an instance of {@link ListValueNode}. This will ensure that if any node
     * somehow is null or not the right instance, will be ignored.
     * @return A list of all the nodes found, null if there are none present.
     */
    public List<ListValueNode> getNodes(){
        return children.stream()
                .filter(ListValueNode.class::isInstance)
                .map(ListValueNode.class::cast)
                .toList();
    }

    /**
     * Returns all values in this Section as a List of the object type.
     * @return A list of values in this Section
     */
    public List<?> getValues(){
        return children.stream()
                .filter(ListValueNode.class::isInstance)
                .map(ListValueNode.class::cast)
                .map(ListValueNode::getValue)
                .map(elementType::cast)
                .toList();
    }

    /**
     * Collects all values inside of this Section, first converting to {@link ListValueNode}, then mapping all their
     * values to the proper element type. The function will act as an additional converter for every specific type
     * @param type The class type to convert to
     * @param converter Used to convert the list to a specific data type if not already instance of.
     * @return A list of type T
     */
    private <T> List<T> collectValues(Class<T> type, Function<Object, T> converter){
        return children.stream()
                .filter(ListValueNode.class::isInstance) // Keep only elements that are instances of ListValueNode
                .map(ListValueNode.class::cast)
                .map(ListValueNode::getValue) // Extracts the wrapped value from each ListValueNode
                .filter(Objects::nonNull) // removes any null objects
                .map(value -> {
                    if(type.isInstance(value)){ // if the value is already of type T, return it directly
                        return type.cast(value);
                    }
                    try { // If not already of type T, attempt to convert it with the converter provider
                        return converter.apply(value);
                    } catch (Exception ignored){
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Again filter for null objects
                .collect(Collectors.toList()); // Return the stream as a list of type T.
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of BigDecimal values
     * @return A list of BigDecimal values
     */
    public List<BigDecimal> getBigDecimalList(){
        return collectValues(
                BigDecimal.class,
                v -> v instanceof BigDecimal bd ? bd :
                v instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : new BigDecimal(v.toString())
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of doubles
     * @return A list of double values
     */
    public List<Double> getDoubleList(){
        return collectValues(
                Double.class,
                v -> v instanceof Number n ? n.doubleValue() : Double.parseDouble(v.toString())
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of Enum values
     * @param enumClass The Enum class type
     * @return A list of Enum values
     */
    public <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass){
        return collectValues(
                enumClass,
                v -> v instanceof Enum<?> e ? enumClass.cast(e) : Enum.valueOf(enumClass, v.toString().trim().toUpperCase(Locale.ROOT))
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of floats
     * @return A list of floats values
     */
    public List<Float> getFloatList(){
        return collectValues(
                Float.class,
                v -> v instanceof Number n ? n.floatValue() : Float.parseFloat(v.toString())
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of integers
     * @return A list of int values
     */
    public List<Integer> getIntList(){
        return collectValues(
                Integer.class,
                v -> v instanceof Number n ? n.intValue() : Integer.parseInt(v.toString())
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a List of long values
     * @return A list of long values
     */
    public List<Long> getLongList(){
        return collectValues(
                Long.class,
                v -> v instanceof Number n ? n.longValue() : Long.parseLong(v.toString())
        );
    }

    /**
     * Concrete function for converting the list of {@link ListValueNode} values into a String List
     * @return A list of floats values
     */
    public List<String> getStringList(){
        return collectValues(
                String.class,
                Object::toString
        );
    }

    @Override
    @Nullable
    public Boolean getBoolean(String key){
        return null;
    }

    @Override
    @Nullable
    public BigDecimal getAsDecimal(String key){
        return null;
    }

    @Override
    @Nullable
    public Double getDouble(String key){
        return null;
    }

    @Override
    @Nullable
    public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass){
        return null;
    }

    @Override
    @Nullable
    public Integer getInt(String key){
        return null;
    }

    @Override
    @Nullable
    public Long getLong(String key){
        return null;
    }

    @Override
    @Nullable
    public String getString(String key){
        return null;
    }

    @Override
    public SectionNode add(ConfigNode node){
        return this;
    }

    @Override
    @Nullable
    public SectionNode getSection(String key){
        return null;
    }

    @Override
    @Nullable
    public ConfigNode getChild(String key){
        return null;
    }

    @Override
    public boolean hasChild(String key){
        return false;
    }

    @Override
    @Nullable
    public ListSectionNode getListSection(String key){
        return null;
    }

    @Override
    @Nullable
    public CommentNode getComment(String string, int index){
        return null;
    }

    @Override
    @Nullable
    public BlankNode getBlankNode(int index){
        return null;
    }

}
