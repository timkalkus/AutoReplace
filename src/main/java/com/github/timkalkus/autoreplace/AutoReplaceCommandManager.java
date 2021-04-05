package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class AutoReplaceCommandManager implements CommandExecutor, TabCompleter {
    private final AutoReplaceMain plugin;
    private final ChatColor co1 = ChatColor.WHITE;
    private final ChatColor co2 = ChatColor.GREEN;
    private final ChatColor co0 = ChatColor.RESET;

    protected AutoReplaceCommandManager(AutoReplaceMain plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        showUsage(sender,label);
        return true;
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
        List<String> argsList = new LinkedList<String>(Arrays.asList(args));
        if (sender.hasPermission(plugin.arToolAll) || sender.hasPermission(plugin.arItemAll)) {
            resultList.addAll(autocompleteAll(sender, argsList));
        }
        if (sender.hasPermission(plugin.arToolOwn) || sender.hasPermission(plugin.arItemOwn)) {
            resultList.addAll(autocompleteOwn(sender, argsList));
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
}
