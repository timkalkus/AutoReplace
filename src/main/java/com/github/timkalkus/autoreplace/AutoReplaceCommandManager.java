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
    private final AutoReplaceMain plugin;
    private final ChatColor co1 = ChatColor.WHITE;
    private final ChatColor co2 = ChatColor.GREEN;
    private final ChatColor co0 = ChatColor.RESET;

    protected AutoReplaceCommandManager(AutoReplaceMain plugin){
        this.plugin = plugin;
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
        switch (args[0]){
            case "@all": 
        }
        return false;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> resultList = new ArrayList<String>();
        if (sender.hasPermission(plugin.arToolAll) || sender.hasPermission(plugin.arItemAll)) {
            resultList.addAll(autocompleteAll(sender, new LinkedList<String>(Arrays.asList(args))));
        }
        if ((sender.hasPermission(plugin.arToolOwn) || sender.hasPermission(plugin.arItemOwn)) && sender instanceof Player) {
            resultList.addAll(autocompleteOwn(sender, new LinkedList<String>(Arrays.asList(args))));
        }
        return resultList;
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

    protected class commandElement {
        private List<commandElement> children;
        private String command;
        private List<String> permissions;
        private boolean allPermissionsNeeded;
        private Consumer<CommandExecuterHelper> targetMethod;
        private CommandExecuterHelper commandExecuterHelper;

        protected commandElement(String command, List<String> permissions,boolean allPermissionsNeeded, List<commandElement> children){
            this.command = command;
            this.children = children;
            this.permissions = permissions;
            this.allPermissionsNeeded = allPermissionsNeeded;
        }

        protected List<String> autoCompleteCommand(CommandSender sender, List<String> args, CommandExecuterHelper commandExecuterHelper){
            if (args.isEmpty() || !isValid(args.get(0)) || !hasNeededPermissions(sender)){
                return new ArrayList<String>();
            }
            List<String> result = new ArrayList<String>();
            if (args.size()==1){
                result.add(command);
                return result;
            }
            if (equalsCommand(args.get(0))){
                args.remove(0);
                for (commandElement child:children){
                    result.addAll(child.autoCompleteCommand(sender,new ArrayList<String>(args),commandExecuterHelper));
                }
            }
            return result;
        }

        protected boolean executeCommand(CommandSender sender, List<String> args, CommandExecuterHelper commandExecuterHelper){
            if (args.isEmpty() || !equalsCommand(args.get(0)) || !hasNeededPermissions(sender)){
                return false;
            }
            if (command.equals(PLAYER_PLACEHOLDER)){
                commandExecuterHelper.overwrite(new CommandExecuterHelper(null,args.get(0),null,null));
            } else {
                commandExecuterHelper.overwrite(this.commandExecuterHelper);
            }

            if (args.size()==1){
                targetMethod.accept(commandExecuterHelper);
                return true;
            }
            args.remove(0);
            for (commandElement child:children){
                if (child.executeCommand(sender,new ArrayList<String>(args), commandExecuterHelper)){
                    return true;
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

        protected commandElement(){
            this(null,new ArrayList<String>(),false,new ArrayList<commandElement>());
        }

        void addChild(commandElement child){
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

    private class CommandExecuterHelper { //helper-class to
        Player player; // player who is executing the command
        String target; // Playername or @all
        String toolItem; // tool, item or both
        String onOffDefault; // on, off, default

        CommandExecuterHelper(Player player, String target, String toolItem, String onOffDefault){
            this.player = player;
            this.target = target;
            this.toolItem = toolItem;
            this.onOffDefault = onOffDefault;
        }

        protected void overwrite(CommandExecuterHelper commandExecuterHelper){
            if (commandExecuterHelper==null){
                return;
            }
            if (commandExecuterHelper.player!=null){
                player = commandExecuterHelper.player;
            }
            if (commandExecuterHelper.target!=null){
                target = commandExecuterHelper.target;
            }
            if (commandExecuterHelper.toolItem!=null){
                toolItem = commandExecuterHelper.toolItem;
            }
            if (commandExecuterHelper.onOffDefault!=null){
                onOffDefault = commandExecuterHelper.onOffDefault;
            }
        }
    }
}
