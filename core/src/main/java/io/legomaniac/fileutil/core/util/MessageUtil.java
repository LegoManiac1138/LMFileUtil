package io.legomaniac.fileutil.core.util;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility Class to handle printing and sending messages, along with formatting strings
 */
public final class MessageUtil {

    private static final String PREFIX = "&8[&eLMFileUtil&8] ";

    /**
     * Formats a string containing valid chat colors
     * @param msg Message string
     * @return Formatted string
     */
    public String color(String msg){
        Pattern pattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
        Matcher match = pattern.matcher(msg);
        StringBuilder buffer = new StringBuilder();
        while(match.find()){
            String hex = match.group(1);
            match.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hex).toString());
        }
        match.appendTail(buffer);
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * Prints a formatted message to the console
     * @param pluginName The name of the plugin
     * @param message Message to send
     */
    public void console(String pluginName, String message){
        Bukkit.getConsoleSender().sendMessage(color(PREFIX + "&6(" + pluginName + ") &r" + message));
    }

}