package io.legomaniac.fileutil.core;

import io.legomaniac.fileutil.core.util.FileManager;
import io.legomaniac.fileutil.core.util.MessageUtil;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * <p>
 * A library for managing configuration files for Minecraft plugins that use {@link org.bukkit.plugin.java.JavaPlugin} as
 * their extension. Instantiate it from the main onEnable() section before loading any custom ConfigFile, otherwise they
 * will be unable to find {@link FileManager}.
 * </p><br><p>
 * The structure for every {@link io.legomaniac.fileutil.core.config.ConfigFile} follows a typical file structure, with
 * different types of nodes at every level.
 * </p><br>
 * <p>RootNode:</p>
 * <p>- ValueNode</p>
 * <p>- ValueNode</p>
 * <p>- SectionNode:</p><br>
 * <p>
 * If a config file <b>must have</b> a particular structure, use {@link io.legomaniac.fileutil.core.config.type.StaticConfig},
 * otherwise use {@link io.legomaniac.fileutil.core.config.type.DynamicConfig}.</p>
 */
public final class LMFileUtil {

    @Getter
    private static LMFileUtil inst;

    @Getter
    private final Plugin sourcePlugin;
    @Getter
    private final String pluginName;
    @Getter
    private final MessageUtil messageUtil;
    @Getter
    private FileManager fileManager;

    public LMFileUtil(Plugin sourcePlugin){
        this.sourcePlugin = sourcePlugin;
        this.pluginName = sourcePlugin!=null ? sourcePlugin.getName() : "Unknown";
        inst = this;
        messageUtil = new MessageUtil();
        File pluginDir = sourcePlugin==null ? null : sourcePlugin.getDataFolder();
        fileManager = new FileManager(pluginDir);
    }

    /**
     * Force unloading and saving of all current configuration files to disk
     */
    public void unload(){
        fileManager.saveConfigs();
        fileManager = null;
    }

}
