package io.legomaniac.fileutil.core.config.type;

import io.legomaniac.fileutil.core.config.ConfigFile;

import java.io.*;

/**
 * This is a configuration file where every node defined in the defaultValues() function are the only nodes that can exist,
 * and anything else is discarded when loaded from disk. This is for files that may contain configurable values by the user,
 * but the structure and path names should remain the same. Only matching sections with different values will be updated.
 */
public abstract class StaticConfig extends ConfigFile {

    public StaticConfig(File parentFile, File file){
        super(parentFile, file);
    }

}
