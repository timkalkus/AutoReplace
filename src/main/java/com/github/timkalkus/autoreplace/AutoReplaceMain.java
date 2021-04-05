package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AutoReplaceMain extends JavaPlugin{
    private FileConfiguration config;
    private boolean toolEnabledByDefault;
    private boolean itemEnabledByDefault;
    private final String toolSettingName = "ToolReplacementEnabledByDefault";
    private final String itemSettingName = "ItemReplacementEnabledByDefault";
    protected final String arCommand = "autoreplace.command";
    protected final String arReload = "autoreplace.reload";
    protected final String arToolOwn = "autoreplace.tool.change.own";
    protected final String arToolAll = "autoreplace.tool.change.all";
    protected final String arToolActivatedTrue = "autoreplace.tool.activated.true";
    protected final String arToolActivatedFalse = "autoreplace.tool.activated.false";
    protected final String arToolForce = "autoreplace.tool.forcedefault";
    protected final String arItemOwn = "autoreplace.item.change.own";
    protected final String arItemAll = "autoreplace.item.change.all";
    protected final String arItemActivatedTrue = "autoreplace.item.activated.true";
    protected final String arItemActivatedFalse = "autoreplace.item.activated.false";
    protected final String arItemForce = "autoreplace.item.forcedefault";
    protected final Logger LOG = Bukkit.getLogger();



    @Override
    public void onEnable() {
        reloadConfigFile();
        getServer().getPluginManager().registerEvents(new AutoReplaceListener(this), this);
        AutoReplaceCommandManager autoReplaceCommandManager = new AutoReplaceCommandManager(this);
        try {
            this.getCommand("autoreplace").setExecutor(autoReplaceCommandManager);
            this.getCommand("autoreplace").setTabCompleter(autoReplaceCommandManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        saveConfigFile();
    }

    public void saveConfigFile() {
        config.set(toolSettingName,toolEnabledByDefault);
        config.set(itemSettingName,itemEnabledByDefault);
        saveConfig();
    }

    public void reloadConfigFile() {
        config = getConfig();
        boolean missingParameters = false;
        if (!config.contains(toolSettingName)){
            config.addDefault(toolSettingName,true);
            missingParameters = true;
        }
        if (!config.contains(itemSettingName)){
            config.addDefault(itemSettingName,true);
            missingParameters = true;
        }
        config.options().copyDefaults(true);
        toolEnabledByDefault = config.getBoolean(toolSettingName);
        itemEnabledByDefault = config.getBoolean(itemSettingName);
        if (missingParameters){
            saveConfigFile();
        }
    }

    public void setToolEnabledByDefault(boolean toolEnabledByDefault) {
        this.toolEnabledByDefault = toolEnabledByDefault;
    }

    public boolean getToolEnabledByDefault(){
        return toolEnabledByDefault;
    }

    public void setItemEnabledByDefault(boolean itemEnabledByDefault) {
        this.itemEnabledByDefault = itemEnabledByDefault;
    }

    public boolean getItemEnabledByDefault(){
        return itemEnabledByDefault;
    }


}
