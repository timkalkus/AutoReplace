package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class AutoReplaceMain extends JavaPlugin{
    private FileConfiguration config;
    private boolean toolEnabledByDefault;
    private boolean itemEnabledByDefault;

    protected Map<UUID,Boolean> playerItemSetting= new HashMap<>();
    protected Map<UUID,Boolean> playerToolSetting= new HashMap<>();

    protected static final String PLAYER_PLACEHOLDER = "<player>";
    protected static final String ALL_PLACEHOLDER = "all";
    protected static final String ENABLE = "enable";
    protected static final String DISABLE = "disable";
    protected static final String DEFAULT = "default";
    protected static final String TOOL = "tool";
    protected static final String ITEM = "item";
    protected static final String SAVE = "save";
    protected static final String RELOAD = "reload";

    private final String toolSettingName = "ToolReplacementEnabledByDefault";
    private final String itemSettingName = "ItemReplacementEnabledByDefault";
    private final String toolPlayerMap = "ToolSettingsForPlayers";
    private final String itemPlayerMap = "ItemSettingsForPlayers";

    //protected final String arCommand = "autoreplace.command";
    protected final String arReload = "autoreplace.reload";
    protected final String arSave = "autoreplace.save";
    protected final String arToolOwn = "autoreplace.tool.change.own";
    protected final String arToolAll = "autoreplace.tool.change.all";
    protected final String arToolActivatedTrue = "autoreplace.tool.default.true";
    protected final String arToolActivatedFalse = "autoreplace.tool.default.false";
    protected final String arToolForce = "autoreplace.tool.forcedefault";
    protected final String arItemOwn = "autoreplace.item.change.own";
    protected final String arItemAll = "autoreplace.item.change.all";
    protected final String arItemActivatedTrue = "autoreplace.item.default.true";
    protected final String arItemActivatedFalse = "autoreplace.item.default.false";
    protected final String arItemForce = "autoreplace.item.forcedefault";
    protected static final Logger LOG = Bukkit.getLogger();



    @Override
    public void onEnable() {
        reloadConfigFile();
        getServer().getPluginManager().registerEvents(new AutoReplaceListener(this), this);
        AutoReplaceCommandManager autoReplaceCommandManager = new AutoReplaceCommandManager(this);
        try {
            Objects.requireNonNull(this.getCommand("autoreplace")).setExecutor(autoReplaceCommandManager);
            Objects.requireNonNull(this.getCommand("autoreplace")).setTabCompleter(autoReplaceCommandManager);
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
        config.createSection(toolPlayerMap, getToolHashMap());
        config.createSection(itemPlayerMap, getItemHashMap());
        //config.set(toolPlayerMap,getToolHashMap());
        //config.set(itemPlayerMap,getItemHashMap());
        saveConfig();
    }

    private HashMap<String,Boolean> getToolHashMap(){
        HashMap<String,Boolean> returnMap = new HashMap<>();
        playerToolSetting.forEach((uuid, bool) -> returnMap.put(uuid.toString(),bool));
        return returnMap;
    }

    private void setToolHashMap(Map<String, Object> inputMap){
        Map<UUID,Boolean> playerToolSetting = new HashMap<>();
        inputMap.forEach((string,bool)->playerToolSetting.put(UUID.fromString(string),(Boolean) bool));
        this.playerToolSetting=playerToolSetting;
    }

    private HashMap<String,Boolean> getItemHashMap(){
        HashMap<String,Boolean> returnMap = new HashMap<>();
        playerItemSetting.forEach((uuid, bool) -> returnMap.put(uuid.toString(),bool));
        return returnMap;
    }

    private void setItemHashMap(Map<String, Object> inputMap){
        Map<UUID,Boolean> playerItemSetting = new HashMap<>();
        inputMap.forEach((string,bool)->playerItemSetting.put(UUID.fromString(string),(Boolean) bool));
        this.playerItemSetting=playerItemSetting;
    }

    public void reloadConfigFile() {

        reloadConfig();
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
        if (!config.isConfigurationSection(toolPlayerMap)){
            config.createSection(toolPlayerMap,playerToolSetting);
            //config.addDefault(toolPlayerMap,playerToolSetting);
            missingParameters = true;
        }
        if (!config.isConfigurationSection(itemPlayerMap)){
            config.createSection(itemPlayerMap,playerItemSetting);
            missingParameters = true;
        }
        config.options().copyDefaults(true);
        toolEnabledByDefault = config.getBoolean(toolSettingName);
        itemEnabledByDefault = config.getBoolean(itemSettingName);
        setToolHashMap(Objects.requireNonNull(config.getConfigurationSection(toolPlayerMap)).getValues(false));
        setItemHashMap(Objects.requireNonNull(config.getConfigurationSection(itemPlayerMap)).getValues(false));
        if (missingParameters){
            saveConfigFile();
        }
    }

    public void setPlayerTool(Player player, boolean asEnabled){
        playerToolSetting.put(player.getUniqueId(),asEnabled);
    }

    public void setPlayerTool(Player player){
        playerToolSetting.remove(player.getUniqueId());
    }

    public void setPlayerItem(Player player, boolean asEnabled){
        playerItemSetting.put(player.getUniqueId(),asEnabled);
    }

    public void setPlayerItem(Player player){
        playerItemSetting.remove(player.getUniqueId());
    }

    public boolean getPlayerToolEnabled(Player player){
        if(player.hasPermission(arToolForce)){
            if (player.hasPermission(arToolActivatedFalse)){
                return false;
            }
            if (player.hasPermission(arToolActivatedTrue)){
                return true;
            }
        }
        UUID uuid = player.getUniqueId();
        if (playerToolSetting.containsKey(uuid)){
            return playerToolSetting.get(uuid);
        }
        if (player.hasPermission(arToolActivatedFalse)){
            return false;
        }
        if (player.hasPermission(arToolActivatedTrue)){
            return true;
        }
        return toolEnabledByDefault;
    }

    public boolean getPlayerItemEnabled(Player player){
        if(player.hasPermission(arItemForce)){
            if (player.hasPermission(arItemActivatedFalse)){
                return false;
            }
            if (player.hasPermission(arItemActivatedTrue)){
                return true;
            }
        }
        UUID uuid = player.getUniqueId();
        if (playerItemSetting.containsKey(uuid)){
            return playerItemSetting.get(uuid);
        }
        if (player.hasPermission(arItemActivatedFalse)){
            return false;
        }
        if (player.hasPermission(arItemActivatedTrue)){
            return true;
        }
        return itemEnabledByDefault;
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
