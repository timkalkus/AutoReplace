package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

import static com.github.timkalkus.autoreplace.AutoReplaceMain.*;

public class AutoReplaceCommandManager implements CommandExecutor, TabCompleter {


    private final AutoReplaceMain plugin;
    private final ChatColor co1 = ChatColor.WHITE;
    private final ChatColor co2 = ChatColor.GREEN;
    private final ChatColor co0 = ChatColor.RESET;
    private final List<CommandElement> commandsPlayer;
    private final List<CommandElement> commandsConsole;

    protected AutoReplaceCommandManager(AutoReplaceMain plugin){
        this.plugin = plugin;
        // build command tree
        // ar on|off|default
        List<String> selfPermissions = Arrays.asList(plugin.arItemOwn,plugin.arToolOwn);
        List<String> selfToolPermission = Collections.singletonList(plugin.arToolOwn);
        List<String> selfItemPermission = Collections.singletonList(plugin.arItemOwn);

        List<String> otherPermissions = Arrays.asList(plugin.arItemAll,plugin.arToolAll);
        List<String> otherToolPermission = Collections.singletonList(plugin.arToolAll);
        List<String> otherItemPermission = Collections.singletonList(plugin.arItemAll);
        // on|off|default for self
        CommandElement selfOn = new CommandElement(ENABLE,selfPermissions,false,null,new CommandExecutorHelper(null,null,null, ENABLE),this::executeCommandExecutorHelper);
        CommandElement selfOff = new CommandElement(DISABLE,selfPermissions,false,null,new CommandExecutorHelper(null,null,null, DISABLE),this::executeCommandExecutorHelper);
        CommandElement selfDefault = new CommandElement(DEFAULT,selfPermissions,false,null,new CommandExecutorHelper(null,null,null,DEFAULT),this::executeCommandExecutorHelper);
        List<CommandElement> selfOnOffDefault = Arrays.asList(selfOn,selfOff,selfDefault);
        // tool|item for self
        CommandElement selfTool = new CommandElement(TOOL,selfToolPermission,false,selfOnOffDefault,new CommandExecutorHelper(null,null,TOOL,null),this::executeCommandExecutorHelper);
        CommandElement selfItem = new CommandElement(ITEM,selfItemPermission,false,selfOnOffDefault,new CommandExecutorHelper(null,null,ITEM,null),this::executeCommandExecutorHelper);
        // on|off|default for other
        CommandElement otherOn = new CommandElement(ENABLE,otherPermissions,false,null,new CommandExecutorHelper(null,null,null, ENABLE),this::executeCommandExecutorHelper);
        CommandElement otherOff = new CommandElement(DISABLE,otherPermissions,false,null,new CommandExecutorHelper(null,null,null, DISABLE),this::executeCommandExecutorHelper);
        CommandElement otherDefault = new CommandElement(DEFAULT,otherPermissions,false,null,new CommandExecutorHelper(null,null,null,DEFAULT),this::executeCommandExecutorHelper);
        List<CommandElement> otherOnOffDefault = Arrays.asList(otherOn,otherOff,otherDefault);
        List<CommandElement> allOnOff = Arrays.asList(otherOn,otherOff);
        // tool|item for other
        CommandElement otherTool = new CommandElement(TOOL,otherToolPermission,false,otherOnOffDefault,new CommandExecutorHelper(null,null,TOOL,null),this::executeCommandExecutorHelper);
        CommandElement otherItem = new CommandElement(ITEM,otherItemPermission,false,otherOnOffDefault,new CommandExecutorHelper(null,null,ITEM,null),this::executeCommandExecutorHelper);
        List<CommandElement> otherToolItemOnOffDefault = Arrays.asList(otherTool,otherItem,otherOn,otherOff,otherDefault);
        // player
        CommandElement otherPlayer = new CommandElement(PLAYER_PLACEHOLDER,otherPermissions,false,otherToolItemOnOffDefault,new CommandExecutorHelper(null,PLAYER_PLACEHOLDER,null,null),this::executeCommandExecutorHelper);
        // tool|item for all
        CommandElement allTool = new CommandElement(TOOL,otherToolPermission,false,allOnOff,new CommandExecutorHelper(null,null,TOOL,null),this::executeCommandExecutorHelper);
        CommandElement allItem = new CommandElement(ITEM,otherItemPermission,false,allOnOff,new CommandExecutorHelper(null,null,ITEM,null),this::executeCommandExecutorHelper);
        List<CommandElement> allToolItemOnOff = Arrays.asList(allTool,allItem,otherOn,otherOff);
        // all
        CommandElement allPlayer = new CommandElement(ALL_PLACEHOLDER,otherPermissions,false,allToolItemOnOff,new CommandExecutorHelper(null,ALL_PLACEHOLDER,null,null),this::executeCommandExecutorHelper);
        // save/reload
        List<String> savePermissions = Collections.singletonList(plugin.arSave);
        List<String> reloadPermissions = Collections.singletonList(plugin.arReload);
        CommandElement save = new CommandElement(SAVE,savePermissions,false,null,new CommandExecutorHelper(SAVE),this::executeCommandExecutorHelper);
        CommandElement reload = new CommandElement(RELOAD,reloadPermissions,false,null,new CommandExecutorHelper(RELOAD),this::executeCommandExecutorHelper);
        commandsPlayer = Arrays.asList(selfOn,selfOff,selfDefault,selfTool,selfItem,otherPlayer,allPlayer,save,reload);
        commandsConsole = Arrays.asList(otherOn,otherOff,allTool,allItem,otherPlayer,allPlayer,save,reload);
    }

    private void executeCommandExecutorHelper(CommandExecutorHelper ceh){
        LOG.fine(ceh.player.getName() + " executed 'autoreplace' command with: " + "target " + ceh.target +
                ", " + TOOL + "|" + ITEM + " " + ceh.toolItem + ", " + ENABLE + "|" + DISABLE + "|" + DEFAULT + "" + ceh.onOffDefault + ", save|reload " + ceh.saveReload);
        if (ceh.saveReload!=null){
            if (ceh.saveReload.equals(SAVE)){
                plugin.saveConfigFile();
                return;
            }
            if (ceh.saveReload.equals(RELOAD)){
                plugin.reloadConfigFile();
                return;
            }
        }
        if (ALL_PLACEHOLDER.equals(ceh.target) || (!(ceh.player instanceof Player) && ceh.target==null)){
            // @all
            boolean boolBoth = ceh.toolItem==null;
            boolean boolItem = (boolBoth || ITEM.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arItemAll);
            boolean boolTool = (boolBoth || TOOL.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arToolAll);
            if (ceh.onOffDefault==null){
                // display current default value
                if (boolItem){
                    ceh.player.sendMessage("default " + ITEM + " replacement is " + (plugin.getItemEnabledByDefault()?"enabled":"disabled"));
                }
                if (boolTool){
                    ceh.player.sendMessage("default " + TOOL + " replacement is " + (plugin.getToolEnabledByDefault()?"enabled":"disabled"));
                }
            } else {
                // set current default value
                if (boolItem){
                    plugin.setItemEnabledByDefault(ENABLE.equals(ceh.onOffDefault));
                }
                if (boolTool){
                    plugin.setToolEnabledByDefault(ENABLE.equals(ceh.onOffDefault));
                }
            }
            return;
        } // for the player himself
        if (ceh.target==null) {// && ceh.player instanceof Player){
            Player player = (Player) ceh.player;
            boolean boolBoth = ceh.toolItem==null;
            boolean boolItem = (boolBoth || ITEM.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arItemOwn);
            boolean boolTool = (boolBoth || TOOL.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arToolOwn);
            if (ceh.onOffDefault==null){
                // display current default value
                if (boolItem){
                    ceh.player.sendMessage("your " + ITEM + " replacement is " + (plugin.getPlayerItemEnabled(player)?"enabled":"disabled"));
                }
                if (boolTool){
                    ceh.player.sendMessage("your " + TOOL + " replacement is " + (plugin.getPlayerToolEnabled(player)?"enabled":"disabled"));
                }
            } else {
                // set current default value
                if (boolItem){
                    if (ceh.onOffDefault.equals(DEFAULT)) {
                        plugin.setPlayerItem(player);
                    } else {
                        plugin.setPlayerItem(player, ENABLE.equals(ceh.onOffDefault));
                    }
                }
                if (boolTool){
                    if (ceh.onOffDefault.equals(DEFAULT)) {
                        plugin.setPlayerTool(player);
                    } else {
                        plugin.setPlayerTool(player, ENABLE.equals(ceh.onOffDefault));
                    }
                }
            }
            return;
        } // for a specific player
        Player player = Bukkit.getPlayer(ceh.target);
        if (player!=null){
            boolean boolBoth = ceh.toolItem==null;
            boolean boolItem = (boolBoth || ITEM.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arItemAll);
            boolean boolTool = (boolBoth || TOOL.equals(ceh.toolItem)) && ceh.player.hasPermission(plugin.arToolAll);
            if (ceh.onOffDefault==null){
                // display current default value
                if (boolItem){
                    ceh.player.sendMessage(player.getDisplayName() + "'s " + ITEM + " replacement is " + (plugin.getPlayerItemEnabled(player)?"enabled":"disabled"));
                }
                if (boolTool){
                    ceh.player.sendMessage(player.getDisplayName() + "'s " + TOOL + " replacement is " + (plugin.getPlayerToolEnabled(player)?"enabled":"disabled"));
                }
            } else {
                // set current default value
                if (boolItem){
                    if (ceh.onOffDefault.equals(DEFAULT)) {
                        plugin.setPlayerItem(player);
                    } else {
                        plugin.setPlayerItem(player, ENABLE.equals(ceh.onOffDefault));
                    }
                }
                if (boolTool){
                    if (ceh.onOffDefault.equals(DEFAULT)) {
                        plugin.setPlayerTool(player);
                    } else {
                        plugin.setPlayerTool(player, ENABLE.equals(ceh.onOffDefault));
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!executeCommand(sender, args)) {
            showUsage(sender, label);
        }
        return true;
    }

    private boolean executeCommand(CommandSender sender, String[] args){
        if (args.length==0){
            return false;
        }
        for (CommandElement commandElement: (sender instanceof Player)?commandsPlayer:commandsConsole){
            if (commandElement.executeCommand(sender, new ArrayList<>(Arrays.asList(args)),
                    new CommandExecutorHelper(sender,null,null,null))){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> resultList = new ArrayList<>();
        for (CommandElement commandElement: (sender instanceof Player)?commandsPlayer:commandsConsole){
            resultList.addAll(commandElement.autoCompleteCommand(sender, new ArrayList<>(Arrays.asList(args))));
        }
        return resultList;
    }

    private void showUsage(CommandSender sender, String label){
        // "/<command> [<playerName>|@all] [tool|item] [enable|disable|default]"
        //label = "autoreplace";
        if (sender instanceof Player){
            // Different settings for the player itself
            if (sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets both your tool and item settings at once.");
                sender.sendMessage(co2+"/" + label + " (" + TOOL + "|" + ITEM + ") (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets either your tool or item setting.");
            }//only item.own permission
            if (!sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [" + ITEM + "] (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets the item setting.");
            }//only tool.own permission
            if (sender.hasPermission(plugin.arToolOwn) && !sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [" + TOOL + "] (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets the tool setting.");
            }
            // Different settings for the .all permission
            if (sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets both tool and item settings at once for specified player.");
                sender.sendMessage(co2+"/" + label + " <playerName> (" + TOOL + "|" + ITEM + ") (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets either tool or item setting of specified player.");
                sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets both tool and item default settings.");
                sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " (" + TOOL + "|" + ITEM + ") (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets either tool or item default setting.");
            }// only item.all permission
            if (sender.hasPermission(plugin.arItemAll) && !sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [" + ITEM + "] (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets the item setting for specified player.");
                sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " [" + ITEM + "] (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets item default setting.");
            }// only tool.all permission
            if (!sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [" + TOOL + "] (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets the tool setting for specified player.");
                sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " [" + TOOL + "] (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets tool default setting.");
            }
            if (sender.hasPermission(plugin.arReload)){
                sender.sendMessage(co2+"/" + label + " reload"+co0+"\n - "+co1+"load config file.");
            }
            if (sender.hasPermission(plugin.arSave)){
                sender.sendMessage(co2+"/" + label + " save"+co0+"\n - "+co1+"saves current settings to config file.");
            }

        } else { //console or command block
            sender.sendMessage(co2+"/" + label + " <playerName> (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets both tool and item settings at once for specified player.");
            sender.sendMessage(co2+"/" + label + " <playerName> (" + TOOL + "|" + ITEM + ") (" + ENABLE + "|" + DISABLE + "|" + DEFAULT + ")"+co0+"\n - "+co1+"sets either tool or item setting of specified player.");
            sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets both tool and item default settings.");
            sender.sendMessage(co2+"/" + label + " " + ALL_PLACEHOLDER + " (" + TOOL + "|" + ITEM + ") (" + ENABLE + "|" + DISABLE + ")"+co0+"\n - "+co1+"sets either tool or item default setting.");
            sender.sendMessage(co2+"/" + label + " save"+co0+"\n - "+co1+"saves current settings to config file.");
            sender.sendMessage(co2+"/" + label + " reload"+co0+"\n - "+co1+"load config file.");
        }

    }


    protected class CommandElement {
        private final List<CommandElement> children;
        private final String command;
        private final List<String> permissions;
        private final boolean allPermissionsNeeded;
        private final Consumer<CommandExecutorHelper> targetMethod;
        private final CommandExecutorHelper commandExecutorHelper;

        /**
         * Initializes a new CommandElement.
         * @param command defines String of command, can also be {@code <player>} placeholder
         * @param permissions List of which permissions are necessary
         * @param allPermissionsNeeded whether you need at least one or all listed permissions
         * @param children List of follow-up commands
         * @param commandExecutorHelper helper-object to write command information into
         * @param targetMethod method to execute
         */
        protected CommandElement(String command, List<String> permissions, boolean allPermissionsNeeded, List<CommandElement> children, CommandExecutorHelper commandExecutorHelper, Consumer<CommandExecutorHelper> targetMethod){
            this.command = command;
            this.children = children;
            this.permissions = permissions;
            this.allPermissionsNeeded = allPermissionsNeeded;
            this.commandExecutorHelper = commandExecutorHelper;
            this.targetMethod = targetMethod;
        }

        protected List<String> autoCompleteCommand(CommandSender sender, List<String> args){
            if (args.isEmpty() || !isValid(args.get(0)) || !hasNeededPermissions(sender)){
                return new ArrayList<>();
            }
            List<String> result = new ArrayList<>();
            if (args.size()==1){
                if (command.equals(PLAYER_PLACEHOLDER)){
                    for (Player player:Bukkit.matchPlayer(args.get(0))){
                        result.add(player.getDisplayName());
                    }
                } else {
                    result.add(command);
                }
                return result;
            }
            if (equalsCommand(args.get(0)) && args.size()>1 && children!=null && children.size()!=0){
                args.remove(0);
                for (CommandElement child:children){
                    result.addAll(child.autoCompleteCommand(sender, new ArrayList<>(args)));
                }
            }
            return result;
        }

        protected boolean executeCommand(CommandSender sender, List<String> args, CommandExecutorHelper commandExecutorHelper){
            if (args.isEmpty() || !equalsCommand(args.get(0)) || !hasNeededPermissions(sender)){
                return false;
            }
            if (command.equals(PLAYER_PLACEHOLDER)){
                commandExecutorHelper.overwrite(new CommandExecutorHelper(null,args.get(0),null,null));
            } else {
                commandExecutorHelper.overwrite(this.commandExecutorHelper);
            }

            if (args.size()==1){
                targetMethod.accept(commandExecutorHelper);
                return true;
            }
            args.remove(0);
            if (children!=null) {
                for (CommandElement child : children) {
                    if (child.executeCommand(sender, new ArrayList<>(args), commandExecutorHelper)) {
                        return true;
                    }
                }
            }
            return false;
        }


        private boolean hasNeededPermissions(CommandSender sender){
            if (permissions.isEmpty()){
                return true;
            }
            if (allPermissionsNeeded) {
                for (String perm : permissions) {
                    if (!sender.hasPermission(perm)){
                        return false;
                    }
                }
                return true;
            } else {
                for (String perm : permissions) {
                    if (sender.hasPermission(perm)){
                        return true;
                    }
                }
                return false;
            }
        }

        private boolean isValid(String input){ //
            if (command.equals(PLAYER_PLACEHOLDER)){
                return !Bukkit.matchPlayer(input).isEmpty();
            }
            return command.startsWith(input);
        }

        private boolean equalsCommand(String input){
            if (command.equals(PLAYER_PLACEHOLDER)){
                return Bukkit.getPlayer(input)!=null;
            }
            return command.equals(input);
        }
    }

    private class CommandExecutorHelper { //helper-class to
        CommandSender player = null; // player who is executing the command
        String target = null; // PlayerName or @all
        String toolItem = null; // tool, item or both
        String onOffDefault = null; // on, off, default
        String saveReload = null;

        CommandExecutorHelper(CommandSender player, String target, String toolItem, String onOffDefault){
            this.player = player;
            this.target = target;
            this.toolItem = toolItem;
            this.onOffDefault = onOffDefault;
        }
        CommandExecutorHelper(String saveReload){
            this.saveReload = saveReload;

        }

        protected void overwrite(CommandExecutorHelper commandExecutorHelper){
            if (commandExecutorHelper ==null){
                return;
            }
            if (commandExecutorHelper.player!=null){
                player = commandExecutorHelper.player;
            }
            if (commandExecutorHelper.target!=null){
                target = commandExecutorHelper.target;
            }
            if (commandExecutorHelper.toolItem!=null){
                toolItem = commandExecutorHelper.toolItem;
            }
            if (commandExecutorHelper.onOffDefault!=null){
                onOffDefault = commandExecutorHelper.onOffDefault;
            }
            if (commandExecutorHelper.saveReload!=null){
                saveReload = commandExecutorHelper.saveReload;
            }
        }
    }
}
