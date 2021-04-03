package com.github.timkalkus.autoreplace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoReplaceMain extends JavaPlugin{
    FileConfiguration config = getConfig();

    @Override
    public void onEnable(){
        config.addDefault("EnabledByDefault",true);
        config.createSection("IndividualPlayerSettings");
        config.options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(new AutoReplaceListener(this), this);
    }

    @Override
    public void onDisable(){

    }


}
