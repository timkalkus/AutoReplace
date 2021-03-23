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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class ToolUsedListener implements Listener{

    private final JavaPlugin plugin;
    private static final String privateKey = "AutoReplaceKey";

    public ToolUsedListener(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void itemDamaged(PlayerItemDamageEvent event){
        BukkitRunnable delayEvent = new DelayEvent(event, event.getItem().clone());
        delayEvent.runTask(plugin);//runTaskLater(plugin,1L);




        event.getPlayer().updateInventory();
        ItemStack[] inventory = event.getPlayer().getInventory().getStorageContents();
        //event.getPlayer().getInventory().
        Bukkit.broadcastMessage(String.valueOf(inventory.length));
        /*
        for (int i = 0; i < inventory.length; i++){
            ItemStack item = inventory[i];
            if (item != null) {
                if (item.getType().name().contains("SHULKER_BOX")){
                    Bukkit.broadcastMessage("Shulker_box found at slot " + i);
                    ShulkerBoxHelper sbh = new ShulkerBoxHelper(item);
                    sbh.getInventory().addItem(new ItemStack(Material.DIRT,64));
                    event.getPlayer().getInventory().setItem(i,sbh.getUpdatedShulkerItem());
                }
            }*/
        for (int i = 0; i < inventory.length; i++){
            ItemStack item = inventory[i];
            if (item != null) {
                if (event.getItem().equals(item)) {
                    Bukkit.broadcastMessage("Identical item found at slot " + i);
                }
            }
        }
    }

    public static void markItem(ItemStack item){
        ItemMeta imeta = item.getItemMeta();
        List<String> lore;
        if (imeta.hasLore())
            lore = imeta.getLore();
        else
            lore = new ArrayList<String>();
        assert lore != null;
        lore.add(privateKey);
        imeta.setLore(lore);
        item.setItemMeta(imeta);
    }

    public static void unmarkItem(ItemStack item){
        ItemMeta imeta = item.getItemMeta();
        List<String> lore;
        if (imeta.hasLore())
            lore = imeta.getLore();
        else
            return;
        assert lore != null;
        lore.remove(privateKey);
        imeta.setLore(lore);
        item.setItemMeta(imeta);
    }

    private class DelayEvent extends BukkitRunnable{
        private PlayerItemDamageEvent event;
        private ItemStack item;

        public DelayEvent(PlayerItemDamageEvent event, ItemStack item){
            this.event = event;
            this.item = item;
        }

        @Override
        public void run() {
            //if (event.getItem().getType().isAir())
            Damageable tool = (Damageable) event.getItem().getItemMeta();
            assert tool != null;
            Bukkit.broadcastMessage(tool.getDamage() + "/" + String.valueOf(event.getItem().getType().getMaxDurability()) + "event");
            Bukkit.broadcastMessage(((Damageable) item.getItemMeta()).getDamage() + "/" + String.valueOf(event.getItem().getType().getMaxDurability()) + "item");
            if (event.getItem().getType().getMaxDurability()-tool.getDamage()<10){
                ReplaceTool.findReplacement(event.getItem(),event.getPlayer());
            }
        }
    }
}
