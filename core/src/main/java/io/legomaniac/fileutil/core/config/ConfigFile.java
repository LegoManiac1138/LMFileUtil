package io.legomaniac.fileutil.core.config;

import io.legomaniac.fileutil.core.LMFileUtil;
import io.legomaniac.fileutil.core.config.node.*;
import io.legomaniac.fileutil.core.config.node.section.ListSectionNode;
import io.legomaniac.fileutil.core.config.node.section.ListValueNode;
import io.legomaniac.fileutil.core.config.type.DynamicConfig;
import io.legomaniac.fileutil.core.util.MessageUtil;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Abstract configuration file in the {@link org.bukkit.plugin.Plugin}'s main folder. Should this or its parent
 * folder be absent, they will be created.
 */
public abstract class ConfigFile {

    protected final File parentFile;
    protected final File file;
    protected final String fileName;

    protected RootNode root = new RootNode();

    protected final LMFileUtil fu = LMFileUtil.getInst();
    protected final MessageUtil mu = fu.getMessageUtil();

    protected ConfigFile(File parentFile, File file){
        this.parentFile = parentFile;
        this.file = file;
        this.fileName = file.getName();
        defaultNodes();
    }

    /**
     * The file name of this configuration file, along with the file extension.
     * @return The current configuration file's name
     */
    public final String getFileName(){
        return fileName;
    }

    /**
     * This will return the Root {@link SectionNode}, which should always exist and not be null.
     * @return The root section node
     */
    public final RootNode getRoot(){
        return root;
    }

    /**
     * This method exists so that the default nodes to be defined by any type of configuration file can be specified. These
     * nodes should always be present in the in-memory and on disk path.
     */
    protected abstract void defaultNodes();

    /**
     * Loads the configuration file into memory, merging the file structure and every value with the ones already in memory.
     */
    public final void load(){
        if(!file.exists()){
            boolean created = create();
            if(created) save();
            return;
        }
        SectionNode diskRoot = parseToTempTree();
        if(diskRoot!=null) mergeTemporary(diskRoot);
    }

    /**
     * Called when a plugin wants to reload the config file
     */
    public final void reload(){
        load();
    }

    /**
     * Creates the config file if it doesn't already exist
     * @return True if the file was created, false if there was an error creating it.
     */
    public final boolean create(){
        try {
            boolean created = file.createNewFile();
            if(created){
                mu.console(fu.getPluginName(), "&b" + fileName + " created.");
            } else {
                mu.console(fu.getPluginName(), "&cUnable to create " + fileName + ".");
            }
            return created;
        } catch (SecurityException ex){
            mu.console(fu.getPluginName(), "&cThe file was unable to be created due to a Security issue.");
        } catch (IOException ex){
            mu.console(fu.getPluginName(), "&cUnable to create " + fileName + " due to an IO Error.");
        }
        return false;
    }

    /**
     * Saves the config structure to the config file in the Plugin's directory
     */
    public final void save(){
        try(Writer writer = new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8)){
            StringBuilder out = new StringBuilder();
            root.saveRoot(out);
            writer.write(out.toString());
            mu.console(fu.getPluginName(), "&b" + fileName + " was saved.");
        } catch (IOException ex){
            mu.console(fu.getPluginName(), "&cUnable to save " + fileName + ".");
        }
    }

    /**
     * Checks if a {@link SectionNode} exists in this config file, checking recursively until it is either found or not.
     * @param path The dot separated path to search for
     * @return True if the section exists, false if not
     */
    protected final boolean sectionExists(String path){
        // The root has no children added yet
        if(root.getChildren()==null || root.getChildren().isEmpty()) return false;
        String[] parts = path.split("\\.");
        SectionNode current = root;
        // Search only the root path
        if(parts.length==1) return current.hasChild(parts[0]);
        // If the path isn't found, or it's not in the right order, return false
        for(String part : parts){
            if(!current.hasChild(part)) return false;
            ConfigNode node = current.getChild(part);
            if(node instanceof SectionNode sn){
                if(sn.getPath().equals(path)) return true;
                current = sn;
            }
        }
        return false;
    }

    /**
     * Creates a new section on the path, along with any parent sections that don't already exist. This avoids any conflicts
     * with sections in between not existing and being linked. The last part of the dot separated path represents the
     * final section to create.
     * @param path The path to create
     * @return The final created section, useful for then adding nodes directly.
     */
    protected final SectionNode createSection(String path){
        if(path==null || path.isEmpty()) return null; // Ignore empty or null paths
        String[] parts = path.split("\\.");
        SectionNode current = root;
        for(int i = 0; i < parts.length; i++){
            String part = parts[i];
            ConfigNode existing = current.getChild(part);
            if(i==parts.length-1){  // Last part. Create new node if it doesn't exist
                if(existing instanceof SectionNode sn){
                    current = sn;
                } else {
                    SectionNode created = new SectionNode(current.getNextIndex(), path, part);
                    current.add(created);
                    current = created;
                }
            } else {  // Intermediate section: must be SectionNode
                if(existing instanceof SectionNode sn){
                    current = sn;
                } else {
                    SectionNode created = new SectionNode(current.getNextIndex(), path, part);
                    current.add(created);
                    current = created;
                }
            }
        }
        return current;
    }

    /**
     * Creates a blank line on the root path
     * @return True if the node was created or not
     */
    public final boolean createBlank(){
        return createBlank(null);
    }

    /**
     * Creates a blank line on a specific path which can be used in subsections
     * @param path The dot separated path to add the node to
     * @return True if the node was added or false if not
     */
    public final boolean createBlank(String path){
        BlankNode node = new BlankNode();
        if(path==null || path.isEmpty()){
            root.add(node);
        } else {
            SectionNode parent = createSection(path);
            if(parent==null) return false;
            parent.add(node);
        }
        return true;
    }

    /**
     * Creates a comment line on the root path
     * @param comment The comment line to add
     * @return True if the node was created or not
     */
    public final boolean createCommentNode(String comment){
        return createCommentNode(null, comment);
    }

    /**
     * Creates a comment line on the specified path.
     * @param path The dot separated path to add the node to
     * @param comment The comment to add
     * @return True if the node was added, false if not
     */
    public final boolean createCommentNode(String path, String comment){
        CommentNode node = new CommentNode(comment);
        if(path==null || path.isEmpty()){
            root.add(node);  // Add to root
        } else {
            SectionNode parent = createSection(path);  // Add to the parent node of the path
            if(parent==null) return false;
            parent.add(node);
        }
        return true;
    }

    /**
     * Created a new {@link ValueNode} on the specified path, providing a value for it.
     * @param path The path for the node
     * @param key The key of the node, which is its "identification"
     * @param defaultValue The value inside the node
     * @return True if the node was created, false if not
     */
    public final boolean createValueNode(String path, String key, Object defaultValue){
        if(path==null || path.isEmpty()) return false;
        SectionNode parent = getParentNode(path);  // Determine the parent section from the path
        if(parent==null) return false;
        parent.add(new ValueNode(path, key, defaultValue));
        return true;
    }

    // Value getters

    /**
     * Returns the section along the path
     * @param path The dot separated path
     * @return An instance of {@link SectionNode} if found, null if not
     */
    public final SectionNode getSection(String path){
        if(path==null || path.isEmpty()) return null; // Ignore empty or null paths
        return root.getSection(path);
    }

    /**
     * Returns the parent node of a specific path. If there is only one "key", then the parent will be returned as the
     * Root directory.
     * @param path The path to search for
     * @return The parent instance of {@link SectionNode}
     */
    protected final SectionNode getParentNode(String path){
        if(path==null) return null;
        int lastDot = path.lastIndexOf(".");
        SectionNode parent = root;
        if(lastDot != -1){
            String parentPath = path.substring(0, lastDot);
            if(!sectionExists(parentPath)){
                parent = createSection(path);
            } else {
                parent = getSection(path);
            }
        }
        return parent;
    }

    /**
     * Parses a string retrieved from the config file into an {@link Object} value.
     * @param raw The raw string to parse through
     * @return The converted object, and if no conditions were met will return the original value as a String
     */
    protected final Object getValueFromString(String raw){
        raw = raw.strip();
        if(raw.equalsIgnoreCase("true") || raw.equalsIgnoreCase("false")) return Boolean.parseBoolean(raw);
        // Quoted String
        String value = raw;
        boolean hadQuotes = false;
        if((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))){
            value = raw.substring(1, raw.length() - 1).strip();
            hadQuotes = true;
        }
        // Number parsing
        try {  // Check for decimals
            if(value.contains(".") || value.toLowerCase().contains("e")) return new BigDecimal(value);
            return Integer.parseInt(value);  // Check for an integer
        } catch (NumberFormatException ignoredInt){
            try {
                return Long.parseLong(value);  // Check for a long
            } catch (NumberFormatException ignoredLong){
                try {
                    return new BigDecimal(value); // fallback for large numeric value
                } catch (NumberFormatException ignoredDecimal){  // Not a number, treat it as a String
                    if(hadQuotes) return value; // return un quoted value
                    return raw;
                }
            }
        }
    }

    /**
     * Helper to combine parent path with child key
     * @param parent The parent path
     * @param child The child node's key
     * @return The combined dot separated path
     */
    private String combinePath(String parent, String child){
        if(parent==null || parent.isEmpty()) return child;
        return parent + "." + child;
    }

    /**
     * Creates a temporary tree structure to then merge into the in-memory values that are pre-set at initialization of
     * a class that extends {@link DynamicConfig} or {@link io.legomaniac.fileutil.core.config.type.StaticConfig}
     * @return The temporary tree structure, or null if an error occurred.
     */
    @Nullable
    protected final SectionNode parseToTempTree(){
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            Deque<SectionNode> sectionStack = new ArrayDeque<>();
            SectionNode tempRoot = new RootNode();
            sectionStack.addLast(tempRoot); // Adds the root section to the current stack
            int index = 0; // The starting line index
            while(index < lines.size()){
                String raw = lines.get(index);
                index++; // Increments the index value
                String line = raw.strip();  // Removes trailing and leading spaces

                int indent = raw.indexOf(line);  // Retrieves the amount of leading spaces
                if(indent % 2 != 0){  // If the indentation is invalid, continue on
                    mu.console(fu.getPluginName(), "&eWarning: Invalid indentation in " + fileName + ": \"" + raw + "\"");
                    continue;
                }
                int depth = indent / 2;  // The depth of the current line

                // Root is depth 0, back tracking indentations when last in a subsection
                while(sectionStack.size() > depth + 1) sectionStack.pollLast();
                SectionNode currentSection = sectionStack.peekLast();
                if(currentSection==null) continue;

                if(line.endsWith(":")){  // Section
                    String sectionName = line.substring(0, line.length() - 1).trim();

                    // Build full path
                    String parentPath = currentSection.getPath();
                    if(parentPath==null || parentPath.isEmpty()) parentPath = "";
                    String fullPath = parentPath.isEmpty() ? sectionName : parentPath + "." + sectionName;

                    // Extract final key
                    String key = sectionName.contains(".") ?
                            sectionName.substring(sectionName.lastIndexOf(".") + 1) :
                            sectionName;

                    // The next index to look forward for
                    int nextIndex = currentSection.getNextIndex();

                    // Peek next line to see if it's a list item
                    SectionNode section;

                    // Peek ahead for list items
                    if(index < lines.size() && lines.get(index).strip().startsWith("- ")){
                        // Create a new section that contains a list
                        ListSectionNode listSection = new ListSectionNode(nextIndex, fullPath, key);
                        while(index < lines.size()){  // Consume list items
                            String peekRaw = lines.get(index);
                            String peekLine = peekRaw.strip();

                            // Removes any comments after the value, as they are unnecessary and will create conflicts
                            if(peekLine.contains("#")) peekLine = peekLine.substring(0, peekLine.indexOf("#"));
                            int peekIndent = peekRaw.indexOf(peekLine);
                            if(peekIndent <= indent) break;
                            if(!peekLine.startsWith("- ")) break;

                            Object value = getValueFromString(peekLine.substring(2).trim());
                            listSection.addValueNode(new ListValueNode(value));
                            index++;
                        }
                        section = listSection;
                    } else {  // This is a normal section, so create it and continue on
                        section = new SectionNode(nextIndex, fullPath, key);
                    }
                    currentSection.add(section);
                    sectionStack.addLast(section);
                    continue;
                }

                if(line.isEmpty()){  // Adds a new blank line
                    SectionNode sec = sectionStack.peekLast();
                    if(sec!=null) sec.add(new BlankNode());
                    continue;
                }

                if(line.startsWith("#")){  // Adds a new comment line as is
                    SectionNode sec = sectionStack.peekLast();
                    if(sec!=null) sec.add(new CommentNode(raw));
                    continue;
                }

                // Key-value
                String[] split = line.split(":", 2);
                if(split.length !=2) continue;  // if the line doesn't have a key-value format

                // Key and value separated into two string variables
                String key = split[0].trim();
                String value = split[1].trim();

                value = stripEdgeQuotes(value); // Removes quotes for only in-memory

                // Remove inline Comments
                String inlineComment = null;
                int hashIndex = value.indexOf(" #");
                if(hashIndex != -1){
                    inlineComment = value.substring(hashIndex + 2).trim();
                    value = value.substring(0, hashIndex).trim(); // Remove the comment part
                }

                String parentPath = currentSection.getPath();  // Current parent section path
                if(parentPath==null || parentPath.isEmpty()) parentPath = "";
                String fullPath = parentPath.isEmpty() ? key : parentPath + "." + key;  // FUll path = parentPath + "." + key

                // Create value node with full path and key
                ValueNode valueNode = new ValueNode(fullPath, key, getValueFromString(value));
                if(inlineComment!=null) valueNode.setInlineComments(List.of(inlineComment));

                currentSection.add(valueNode);  // Adds the new node to the current section
            }
            return tempRoot;
        } catch (IOException ex){
            mu.console(fu.getPluginName(), "&cAn error occurred when attempting to read " + fileName + ".");
            return null;
        }
    }

    /**
     * Merges the config structure found inside the configuration file with the one stored in memory. If this is an instance
     * of {@link DynamicConfig}, then values will be merged if already in the structure, and any new {@link SectionNode}
     * or {@link ValueNode} will be added for persistence. If this is an instance of {@link io.legomaniac.fileutil.core.config.type.StaticConfig},
     * then only ValueNodes and sections that were present at initialization will be merged, and everything else will be
     * ignored.
     * @param source The temporary directory
     * @param target The root in-memory directory created by {@link ConfigFile}.defaultNodes().
     * @param dynamic Whether the config is dynamic or static
     */
    protected final void mergeSection(SectionNode source, SectionNode target, boolean dynamic){
        if(source==null) return;
        for(ConfigNode node : source.getChildren()){
            if(node instanceof SectionNode srcSection && !(srcSection instanceof ListSectionNode)){
                SectionNode tgtSection = target.getSection(srcSection.getKey());  // Check if target already has this section
                if(tgtSection != null){  // Section exists -> recurse
                    mergeSection(srcSection, tgtSection, dynamic);
                } else if(dynamic){  // Section missing -> add it
                    SectionNode section = new SectionNode(target.getNextIndex(), combinePath(target.getPath(), srcSection.getKey()),
                            srcSection.getKey());
                    target.add(section);
                    mergeSection(srcSection, section, true);  // recursively add children
                }
            } else if(node instanceof ListSectionNode srcList){
                ConfigNode existing = target.getChild(srcList.getKey());
                if(existing instanceof ListSectionNode tl){  // Merge list items
                    List<ListValueNode> values = srcList.getNodes();
                    if(values==null || values.isEmpty()) continue;
                    for(ListValueNode val : values){
                        if(!tl.containsNode(val.getValue()) && dynamic) tl.addValueNode(new ListValueNode(val.getValue()));
                    }
                } else if(dynamic){  // List missing -> create new one
                    ListSectionNode listNode = new ListSectionNode(target.getNextIndex(),
                            combinePath(target.getPath(), srcList.getKey()), srcList.getKey());
                    List<ListValueNode> nodes = srcList.getNodes();
                    if(nodes==null || nodes.isEmpty()) continue;
                    for(ListValueNode val : nodes) listNode.addValueNode(new ListValueNode(val.getValue()));
                    target.add(listNode);
                }
            } else if(node instanceof ValueNode srcValue){
                ValueNode tgtValue = (ValueNode) target.getChild(srcValue.getKey());
                if(tgtValue!=null){  // Value exists -> Overwrite value
                    tgtValue.setValue(srcValue.getValue());
                } else if(dynamic){  // Value missing -> add new
                    ValueNode valueNode = new ValueNode(target.getPath(), srcValue.getKey(), srcValue.getValue());
                    valueNode.setInlineComments(srcValue.getInlineComments());
                    target.add(valueNode);
                }
            } else if(node instanceof CommentNode srcComment){
                if(dynamic){
                    String comment = srcComment.getComment();
                    if(comment==null || comment.isEmpty()) continue;
                    CommentNode tgtNode = target.getComment(comment, srcComment.getIndex());
                    if(tgtNode==null) target.add(new CommentNode(comment), srcComment.getIndex());
                }
            } else if(node instanceof BlankNode blankNode){
                if(!dynamic) continue;
                int sourceIndex = blankNode.getIndex();
                BlankNode targetNode = target.getBlankNode(sourceIndex);
                if(targetNode==null) target.add(new BlankNode());
            }
        }
    }

    /**
     * Merges the temporary directory
     * @param tempRoot The on-disk config structure
     */
    public final void mergeTemporary(SectionNode tempRoot){
        boolean isDynamic = this instanceof DynamicConfig;
        mergeSection(tempRoot, this.root, isDynamic);
    }

    /**
     * Strips the surrounding quotes " and ' from the string when in use by a plugin
     * @param input The input string with quotations
     * @return The un-quoted string
     */
    protected final String stripEdgeQuotes(String input) {
        if(input==null || input.isEmpty()) return input;
        return input.replaceAll("^(\"|')|(\"|')$", ""
        );
    }

}
