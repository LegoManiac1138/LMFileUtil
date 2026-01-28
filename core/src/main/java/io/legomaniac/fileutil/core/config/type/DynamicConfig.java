package io.legomaniac.fileutil.core.config.type;

import io.legomaniac.fileutil.core.config.ConfigFile;

import java.io.File;

/**
 * A configuration file that can dynamically add {@link io.legomaniac.fileutil.core.config.node.ConfigNode}s that do not
 * currently exist in memory. This is useful for when a structure or subsection is unknown at compilation or when
 * user-created sections or values are to be used. This <b>SHOULD NOT </b> be used for the main configuration file. Instead,
 * use {@link StaticConfig}.
 */
public abstract class DynamicConfig extends ConfigFile {

    public DynamicConfig(File parentFile, File file){
        super(parentFile, file);
    }

}
