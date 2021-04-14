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

public class AutoReplaceCommandManager implements CommandExecutor, TabCompleter {
    public static final String PLAYER_PLACEHOLDER = "<player>";
    public static final String ALL_PLACEHOLDER = "@all";
    private final AutoReplaceMain plugin;
    private final ChatColor co1 = ChatColor.WHITE;
    private final ChatColor co2 = ChatColor.GREEN;
    private final ChatColor co0 = ChatColor.RESET;
    private List<CommandElement> commandsPlayer;
    private List<CommandElement> commandsConsole;

    protected AutoReplaceCommandManager(AutoReplaceMain plugin){
        this.plugin = plugin;
        // build command tree
        // ar on|off|default
        List<String> selfPermissions = Arrays.asList(plugin.arItemOwn,plugin.arToolOwn);
        List<String> selfToolPermission = Arrays.asList(plugin.arToolOwn);
        List<String> selfItemPermission = Arrays.asList(plugin.arItemOwn);

        List<String> otherPermissions = Arrays.asList(plugin.arItemAll,plugin.arToolAll);
        List<String> otherToolPermission = Arrays.asList(plugin.arToolAll);
        List<String> otherItemPermission = Arrays.asList(plugin.arItemAll);
        // on|off|default for self
        CommandElement selfOn = new CommandElement("on",selfPermissions,false,null,new CommandExecutorHelper(null,null,null,"on"),this::executeCommandExecutorHelper);
        CommandElement selfOff = new CommandElement("off",selfPermissions,false,null,new CommandExecutorHelper(null,null,null,"off"),this::executeCommandExecutorHelper);
        CommandElement selfDefault = new CommandElement("default",selfPermissions,false,null,new CommandExecutorHelper(null,null,null,"default"),this::executeCommandExecutorHelper);
        List<CommandElement> selfOnOffDefault = Arrays.asList(selfOn,selfOff,selfDefault);
        // tool|item for self
        CommandElement selfTool = new CommandElement("tool",selfToolPermission,false,selfOnOffDefault,new CommandExecutorHelper(null,null,"tool",null),this::executeCommandExecutorHelper);
        CommandElement selfItem = new CommandElement("item",selfItemPermission,false,selfOnOffDefault,new CommandExecutorHelper(null,null,"item",null),this::executeCommandExecutorHelper);
        // on|off|default for other
        CommandElement otherOn = new CommandElement("on",otherPermissions,false,null,new CommandExecutorHelper(null,null,null,"on"),this::executeCommandExecutorHelper);
        CommandElement otherOff = new CommandElement("off",otherPermissions,false,null,new CommandExecutorHelper(null,null,null,"off"),this::executeCommandExecutorHelper);
        CommandElement otherDefault = new CommandElement("default",otherPermissions,false,null,new CommandExecutorHelper(null,null,null,"default"),this::executeCommandExecutorHelper);
        List<CommandElement> otherOnOffDefault = Arrays.asList(otherOn,otherOff,otherDefault);
        List<CommandElement> allOnOff = Arrays.asList(otherOn,otherOff);
        // tool|item for other
        CommandElement otherTool = new CommandElement("tool",otherToolPermission,false,otherOnOffDefault,new CommandExecutorHelper(null,null,"tool",null),this::executeCommandExecutorHelper);
        CommandElement otherItem = new CommandElement("item",otherItemPermission,false,otherOnOffDefault,new CommandExecutorHelper(null,null,"item",null),this::executeCommandExecutorHelper);
        List<CommandElement> otherToolItemOnOffDefault = Arrays.asList(otherTool,otherItem,otherOn,otherOff,otherDefault);
        // player
        CommandElement otherPlayer = new CommandElement(PLAYER_PLACEHOLDER,otherPermissions,false,otherToolItemOnOffDefault,new CommandExecutorHelper(null,PLAYER_PLACEHOLDER,null,null),this::executeCommandExecutorHelper);
        // tool|item for all
        CommandElement allTool = new CommandElement("tool",otherToolPermission,false,allOnOff,new CommandExecutorHelper(null,null,"tool",null),this::executeCommandExecutorHelper);
        CommandElement allItem = new CommandElement("item",otherItemPermission,false,allOnOff,new CommandExecutorHelper(null,null,"item",null),this::executeCommandExecutorHelper);
        List<CommandElement> allToolItemOnOff = Arrays.asList(allTool,allItem,otherOn,otherOff);
        // all
        CommandElement allPlayer = new CommandElement(ALL_PLACEHOLDER,otherPermissions,false,allToolItemOnOff,new CommandExecutorHelper(null,ALL_PLACEHOLDER,null,null),this::executeCommandExecutorHelper);
        commandsPlayer = Arrays.asList(selfOn,selfOff,selfDefault,selfTool,selfItem,otherPlayer,allPlayer);
    }

    private void executeCommandExecutorHelper(CommandExecutorHelper ceh){
        Bukkit.broadcastMessage("ceh equals: " + ceh.onOffDefault + ", " + ceh.target + ", " + ceh.toolItem);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!executeCommand(sender, command, label, args)) {
            showUsage(sender, label);
        }
        return true;
    }

    private boolean executeCommand(CommandSender sender, Command command, String label, String[] args){
        if (args.length==0){
            return false;
        }
        for (CommandElement commandElement: commandsPlayer){
            if (commandElement.executeCommand(sender,new ArrayList<String>(Arrays.asList(args)),new CommandExecutorHelper(sender,null,null,null))){
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> resultList = new ArrayList<String>();
        for (CommandElement commandElement: commandsPlayer){
            resultList.addAll(commandElement.autoCompleteCommand(sender,new ArrayList<String>(Arrays.asList(args))));
        }
        return resultList;
    }

    private void showUsage(CommandSender sender, String label){
        // "/<command> [<playerName>|@all] [tool|item] [on|off|default]"
        label = "autoreplace";
        if (sender instanceof Player){
            // Different settings for the player itself
            if (sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " (on|off|default)"+co0+"\n - "+co1+"sets both your tool and item settings at once.");
                sender.sendMessage(co2+"/" + label + " (tool|item) (on|off|default)"+co0+"\n - "+co1+"sets either your tool or item setting.");
            }//only item.own permission
            if (!sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [item] (on|off|default)"+co0+"\n - "+co1+"sets the item setting.");
            }//only tool.own permission
            if (sender.hasPermission(plugin.arToolOwn) && !sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [tool] (on|off|default)"+co0+"\n - "+co1+"sets the tool setting.");
            }
            // Different settings for the .all permission
            if (sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> (on|off|default)"+co0+"\n - "+co1+"sets both tool and item settings at once for specified player.");
                sender.sendMessage(co2+"/" + label + " <playerName> (tool|item) (on|off|default)"+co0+"\n - "+co1+"sets either tool or item setting of specified player.");
                sender.sendMessage(co2+"/" + label + " @all (on|off)"+co0+"\n - "+co1+"sets both tool and item default settings.");
                sender.sendMessage(co2+"/" + label + " @all (tool|item) (on|off)"+co0+"\n - "+co1+"sets either tool or item default setting.");
            }// only item.all permission
            if (sender.hasPermission(plugin.arItemAll) && !sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [item] (on|off|default)"+co0+"\n - "+co1+"sets the item setting for specified player.");
                sender.sendMessage(co2+"/" + label + " @all [item] (on|off)"+co0+"\n - "+co1+"sets item default setting.");
            }// only tool.all permission
            if (!sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [tool] (on|off|default)"+co0+"\n - "+co1+"sets the tool setting for specified player.");
                sender.sendMessage(co2+"/" + label + " @all [tool] (on|off)"+co0+"\n - "+co1+"sets tool default setting.");
            }

        } else { //console or command block
            sender.sendMessage(co2+"/" + label + " <playerName> (on|off|default)"+co0+"\n - "+co1+"sets both tool and item settings at once for specified player.");
            sender.sendMessage(co2+"/" + label + " <playerName> (tool|item) (on|off|default)"+co0+"\n - "+co1+"sets either tool or item setting of specified player.");
            sender.sendMessage(co2+"/" + label + " @all (on|off)"+co0+"\n - "+co1+"sets both tool and item default settings.");
            sender.sendMessage(co2+"/" + label + " @all (tool|item) (on|off)"+co0+"\n - "+co1+"sets either tool or item default setting.");
        }

    }


    private List<String> autocompleteAll(CommandSender sender, List<String> args){
        if (args.size()==0){
            return Collections.emptyList();
        }
        String currentArgs = args.remove(0);
        if (args.size()==0){
            List<String> resultList = new ArrayList<String>();
            resultList.addAll(getSimilarStrings("@all",currentArgs));
            resultList.addAll(autocompleteName(currentArgs));
            return resultList;
        }
        boolean tool = sender.hasPermission(plugin.arToolAll);
        boolean item = sender.hasPermission(plugin.arItemAll);
        if (currentArgs.equals("@all")) {
            return autocompleteSelection(sender, args, tool, item, false);
        }
        if (Bukkit.getPlayer(currentArgs)!=null){
            return autocompleteSelection(sender, args, tool, item, true);
        }
        return Collections.emptyList();
    }

    private List<String> autocompleteSelection(CommandSender sender, List<String> args, boolean tool, boolean item, boolean withDefault) {
        if (args.size()==0){
            return Collections.emptyList();
        }
        String currentArgs = args.remove(0);
        if (args.size()==0){
            List<String> resultList = new ArrayList<String>(getSimilarStrings(Arrays.asList("on", "off"), currentArgs));
            if (tool){
                resultList.addAll(getSimilarStrings("tool",currentArgs));
            }
            if (item){
                resultList.addAll(getSimilarStrings("item",currentArgs));
            }
            return resultList;
        }
        if (currentArgs.equals("on")||currentArgs.equals("off")||currentArgs.equals("default")){
            return Collections.emptyList();
        }
        if (item && currentArgs.equals("item")){
            return autocompleteSelection(sender, args, false, false, withDefault);
        }
        if (tool && currentArgs.equals("tool")){
            return autocompleteSelection(sender, args, false, false, withDefault);
        }
        return Collections.emptyList();
    }

    private List<String> getSimilarStrings(String template,String prefix){
        List<String> resultList = new ArrayList<String>();
        if (template.startsWith(prefix)){
            resultList.add(template);
        }
        return resultList;
    }

    private List<String> getSimilarStrings(List<String> templates,String prefix){
        List<String> resultList = new ArrayList<String>();
        for (String template:templates) {
            if (template.startsWith(prefix)) {
                resultList.add(template);
            }
        }
        return resultList;
    }

    private List<String> autocompleteOwn(CommandSender sender, List<String> args){
        if (args.size()==0){
            return Collections.emptyList();
        }
        boolean tool = sender.hasPermission(plugin.arToolOwn);
        boolean item = sender.hasPermission(plugin.arItemOwn);
        return autocompleteSelection(sender,args,tool,item,true);
    }

    private List<String> autocompleteName(String args){
        List<String> result = new ArrayList<String>();
        for (Player player:Bukkit.matchPlayer(args)){
            result.add(player.getDisplayName());
        }
        return result;
    }

    protected class CommandElement {
        private List<CommandElement> children;
        private String command;
        private List<String> permissions;
        private boolean allPermissionsNeeded;
        private Consumer<CommandExecutorHelper> targetMethod;
        private CommandExecutorHelper commandExecutorHelper;

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
                return new ArrayList<String>();
            }
            List<String> result = new ArrayList<String>();
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
                    result.addAll(child.autoCompleteCommand(sender,new ArrayList<String>(args)));
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
                    if (child.executeCommand(sender, new ArrayList<String>(args), commandExecutorHelper)) {
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

        void addChild(CommandElement child){
            children.add(child);
        }

        void setCommand(String command){
            this.command=command;
        }

        void addPermission(String permission){
            permissions.add(permission);
        }

        void setAllPermissionsNeeded(boolean allPermissionsNeeded){
            this.allPermissionsNeeded=allPermissionsNeeded;
        }
    }

    private class CommandExecutorHelper { //helper-class to
        CommandSender player; // player who is executing the command
        String target; // Playername or @all
        String toolItem; // tool, item or both
        String onOffDefault; // on, off, default

        CommandExecutorHelper(CommandSender player, String target, String toolItem, String onOffDefault){
            this.player = player;
            this.target = target;
            this.toolItem = toolItem;
            this.onOffDefault = onOffDefault;
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
        }
    }
}
