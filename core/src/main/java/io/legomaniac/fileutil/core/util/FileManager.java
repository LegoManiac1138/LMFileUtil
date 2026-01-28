package io.legomaniac.fileutil.core.util;

import io.legomaniac.fileutil.core.LMFileUtil;
import io.legomaniac.fileutil.core.config.ConfigFile;

import java.io.File;
import java.util.HashSet;

/**
 * Manager for handling everything related to file storage and configuration
 */
public final class FileManager {

    private final HashSet<ConfigFile> configs = new HashSet<>();

    public FileManager(File pluginDir){
        if(pluginDir==null) return;
        if(!pluginDir.exists()){
            boolean createdSource = pluginDir.mkdirs();
            if(!createdSource){
                LMFileUtil.getInst().getMessageUtil().console(pluginDir.getName(), "&cUnable to create " + pluginDir.getName() + " directory.");
            }
        }
    }

    /**
     * Creates a new file from plugin jar or loads the current yml file
     * @param configFile New instance of a Configuration File
     */
    public void addConfig(ConfigFile configFile){
        if(configFile!=null) configs.add(configFile);
    }

    /**
     * Loads every configuration file into memory
     */
    public void loadConfigs(){
        if(configs.isEmpty()) return;
        for(ConfigFile cf : configs) cf.load();
    }

    /**
     * Retrieves a Config by the file name
     * @param fileName The file name with the file extension
     * @return An instance of the Config file if found, null if not present or loaded
     */
    public ConfigFile getConfig(String fileName){
        return configs.stream().filter(cf -> cf.getFileName().equals(fileName)).findFirst().orElse(null);
    }

    /**
     * Reloads a specific configuration file by a file name
     * @param fileName The file name plus file extension
     */
    public void reloadConfig(String fileName){
        ConfigFile configFile = getConfig(fileName);
        if(configFile==null) return;
        configFile.reload();
    }

    /**
     * Saves all currently loaded configuration files if the parent folder exists
     */
    public void saveConfigs(){
        if(configs.isEmpty()) return;
        configs.forEach(ConfigFile::save);
    }

}