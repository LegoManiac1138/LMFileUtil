# LMFileUtil

---

## Information

<p>
LMFileUtil is a lightweight library for handling the creation, saving, and loading of configuration files in use
with Spigot plugins. 

Instead of using Spigot's internal implementation of YAMLConfiguration and ConfigurationSection, which do not save or 
persist comments, this library will accurately place and persist comment lines if present. This is done by iterating
through each line, parsing every configuration file as if it's a file structure.
</p>


---

## Installation
- 1: Clone the repository using `git clone https://github.com/LegoManiac1138/LMFileUtil` and compile or download the version 
specific to your Spigot version.
- 2: Add `LMFileUtil-{version-name}`, where `version-name` is the spigot version as a dependency in your pom.xml file. This
will ensure that you're able to work with the library. Also make sure to set the scope as "compile" so it will be included
in your plugin.jar.
---

## Creating a New Configuration File Instance
<p>
First extend either <b>DynamicConfig</b> or <b>StaticConfig</b> depending on your use case. 

<h3>StaticConfig:</h3> 
Configs where the values can be modified by the owner, but ultimately every section and comment 
stays in its place. This is great for the main configuration file where everything must stay in its proper position and
only take in modified values.

<h3>DynamicConfig:</h3>
Configs where key-pairs and section are either present or currently unknown. This is useful for when wanting to add
user-configurable values such as kits, channels, custom items, etc. Every node from the config file itself will be compared
to the default in-memory values. If the node doesn't currently exist, it will be added to the root path at the current 
section. This ensures proper ordering of every section and end node.
</p>

---

## Adding a New Node to Your Config File
<p>
Every config file must override "defaultNodes()", which is where all default values will be setup. When the config file is 
then loaded, the node structure will be created from what is present, then on-disk will be checked.

<b>createCommentNode(String):</b> Creates a new comment line for notes

<b>createBlankNode:</b> Creates a blank line represented by a new line. This is for separating different sections from
one another.

<b>createValueNode(String, String, Object):</b> Creates a new node containing a key-value pair. The first argument is the absolute path of the node,
the second argument is the "key", and the last argument is object value. Supported data types `Boolean`, `Integer`, `Long`, 
`BigDecimal`, `Double`, `Float`, `Enum`, and `String`. 

<b>createSection(String):</b> Creates a new section node which may or may not hold other nodes or sections inside it. This
will create any parent section that is missing if multiple sections are inside the path, such as `settings.channels.channel`.
</p>
<br>

Sample Class:
```java
public final class MainConfig extends StaticConfig {
    
    private static final String fileName = "config.yml";
    
    public MainConfig(@NotNull File parentFile){
        super(parentFile, new File(parentFile, fileName));
    }

    @Override
    public void defaultNodes(){
        createCommentNode("#==========================#");
        createCommentNode("#        config.yml        #");
        createCommentNode("#==========================#");
        createBlankNode();
        createValueNode("version", "version", 1);
        createBlankNode();
        // Settings section
        SectionNode mainSettings = createSection("settings");
        if(mainSettings!=null){
            mainSettings.add(new ValueNode("settings.chat_filter", "chat_filter", false));
            mainSettings.add(new ValueNode("settings.chat_color", "chat_color", "&f"));
            // Channels inner section
            SectionNode channels = createSection("settings.channels");
            if(channels!=null){
                channels.add(new ValueNode("settings.channels.global", "global", true));
                channels.add(new ValueNode("settings.channels.staff", "staff", true));
                channels.add(new ValueNode("settings.channels.world", "world", false));
            }
        }
    }
    
    
}
```
---
