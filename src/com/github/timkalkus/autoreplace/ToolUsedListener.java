package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.github.timkalkus.autoreplace.ReplaceTool;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

public class ToolUsedListener implements Listener{

    private final JavaPlugin plugin;

    public ToolUsedListener(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void itemDamaged(PlayerItemDamageEvent event){
        BukkitRunnable delayEvent = new DelayEvent(event);
        delayEvent.runTask(plugin);//runTaskLater(plugin,1L);

    }

    private class DelayEvent extends BukkitRunnable{
        private PlayerItemDamageEvent event;

        public DelayEvent(PlayerItemDamageEvent event){
            this.event = event;
        }

        @Override
        public void run() {
            Damageable tool = (Damageable) event.getItem().getItemMeta();
            assert tool != null;
            Bukkit.broadcastMessage(tool.getDamage() + "/" + String.valueOf(event.getItem().getType().getMaxDurability()));
            if (event.getItem().getType().getMaxDurability()-tool.getDamage()<10){
                ReplaceTool.findReplacement(event.getItem(),event.getPlayer());
            }
        }
    }
}
