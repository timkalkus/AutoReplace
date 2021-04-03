package com.github.timkalkus.autoreplace;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoReplaceMain extends JavaPlugin{

    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(new AutoReplaceListener(this), this);
    }

    @Override
    public void onDisable(){

    }
}
