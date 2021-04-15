# AutoReplace
Minecraft/Spigot Plugin to auto-replace broken tools and emptied items

#About the plugin
AutoReplace is a small plugin to enhance the vanilla playing experience by automatically replacing nearly* broken tools and depleted item-stacks. For this it does not only search the inventory itself but also any shulker box contained in the players inventory.
_*it only saves enchanted tools so that they can be repaired and used again, without an enchantment the tools get replaced when they break_

The plugin is currently in a **early development stage**. There are probably bugs, so **use at your own risk!** But in the case you encounter one: I would be very thankful for any bug report :)

# Installation Guide
* Place `AutoReplace.jar` into your `/plugins` folder of your spigot server
* Done. There is nothing else for you to do :D Unless you want to set some of the new permissions for your players.

# Configuration Guide
If you just want all your users to have the AutoReplace functionality, you don't really need to change anything: everybody can en- and disable the replacement functionality on his own and OPs can additionally set both the global default and the settings for other players.

For any further configuration (to give/remove functionality to specific groups/players) you can use any permission plugin of your choice. Most permissions should be self explanatory, but the functionality around the 'default' permission may need some explanation:
To check if a player has the replace functionality enabled, the plugin first checks for any configurations for the specific player (can be accessed in the config.yml), if there is none/default set, it then checks if the player has the `*.default.*` permission set. If this is not the case the plugin will use the default server-wide setting (can also be found in the config).
So if you want to force a group of players to (not) use the replace functionality you can additionally set the `*.forcedefault` permission, then the plugin will check first the `*.default.*` permission to determine if the user can use the replace functionality.

# Commands
* `/autoreplace [tool|item] (enable|disable|default)` change your own settings
* `/autoreplace <playerName> [tool|item] (enable|disable|default)` change settings for specified player
* `/autoreplace all [tool|item] (enable|disable)` change default settings for all players
* `/autoreplace (reload|save)` reload/save the config file
`/autoreplace` can be replaced with the abbreviation `/ar` for all commands listed above.
# Permissions
* `autoreplace.command` basic permission to get access to commands; default: all
* `autoreplace.reload` permission to use reload functionality; default: op
* `autoreplace.save` permission to use save functionality; default: op
##  Tool-specific permissions:
* `autoreplace.tool.change.own` permission to change your own tool settings; default: all
* `autoreplace.tool.change.all` permission to change default tool settings and the individual settings of other players; default: op
* `autoreplace.tool.change.*` grands both permissions listed above; default: op
* `autoreplace.tool.default.true` sets default tool setting to enabled; default: none
* `autoreplace.tool.default.false` sets default tool setting to disabled; default: none
* `autoreplace.tool.forcedefault` forces an overwrite so that the player has to use the permission based default settings (only use with caution, e.g. when you what to block someone from using autoreplace); default: none
##  Item-specific permissions:
* `autoreplace.item.change.own` permission to change your own item settings; default: all
* `autoreplace.item.change.all` permission to change default item settings and the individual settings of other players; default: op
* `autoreplace.item.change.*` grands both permissions listed above; default: op
* `autoreplace.item.default.true` sets default item setting to enabled; default: none
* `autoreplace.item.default.false` sets default item setting to disabled; default: none
* `autoreplace.item.forcedefault` forces an overwrite so that the player has to use the permission based default settings (only use with caution, e.g. when you what to block someone from using autoreplace); default: none
