package com.github.timkalkus.autoreplace;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

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

        if (sender instanceof Player){
            // Different settings for the player itself
            if (sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " (on|off|default)"+co0+"\n\t"+co1+"sets both tool and item settings at once.");
                sender.sendMessage(co2+"/" + label + "(tool|item) (on|off|default)"+co0+"\n\t"+co1+"sets either tool or item setting.");
            }//only item.own permission
            if (!sender.hasPermission(plugin.arToolOwn) && sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [item] (on|off|default)"+co0+"\n\t"+co1+"sets the item setting.");
            }//only tool.own permission
            if (sender.hasPermission(plugin.arToolOwn) && !sender.hasPermission(plugin.arItemOwn)) {
                sender.sendMessage(co2+"/" + label + " [tool] (on|off|default)"+co0+"\n\t"+co1+"sets the tool setting.");
            }
            // Different settings for the .all permission
            if (sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> (on|off|default)"+co0+"\n\t"+co1+"sets both tool and item settings at once for specified player.");
                sender.sendMessage(co2+"/" + label + " <playerName> (tool|item) (on|off|default)"+co0+"\n\t"+co1+"sets either tool or item setting of specified player.");
                sender.sendMessage(co2+"/" + label + " @all (on|off)"+co0+"\n\t"+co1+"sets both tool and item default settings.");
                sender.sendMessage(co2+"/" + label + " @all (tool|item) (on|off)"+co0+"\n\t"+co1+"sets either tool or item default setting.");
            }// only item.all permission
            if (sender.hasPermission(plugin.arItemAll) && !sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [item] (on|off|default)"+co0+"\n\t"+co1+"sets the item setting for specified player.");
                sender.sendMessage(co2+"/" + label + " @all [item] (on|off)"+co0+"\n\t"+co1+"sets item default setting.");
            }// only tool.all permission
            if (!sender.hasPermission(plugin.arItemAll) && sender.hasPermission(plugin.arToolAll)){
                sender.sendMessage(co2+"/" + label + " <playerName> [tool] (on|off|default)"+co0+"\n\t"+co1+"sets the tool setting for specified player.");
                sender.sendMessage(co2+"/" + label + " @all [tool] (on|off)"+co0+"\n\t"+co1+"sets tool default setting.");
            }

        } else { //console or command block
            sender.sendMessage(co2+"/" + label + " <playerName> (on|off|default)"+co0+"\n\t"+co1+"sets both tool and item settings at once for specified player.");
            sender.sendMessage(co2+"/" + label + " <playerName> (tool|item) (on|off|default)"+co0+"\n\t"+co1+"sets either tool or item setting of specified player.");
            sender.sendMessage(co2+"/" + label + " @all (on|off)"+co0+"\n\t"+co1+"sets both tool and item default settings.");
            sender.sendMessage(co2+"/" + label + " @all (tool|item) (on|off)"+co0+"\n\t"+co1+"sets either tool or item default setting.");
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
