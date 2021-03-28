package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        if (!event.getItem().hasItemMeta())
            return;
        if (!(event.getItem().getItemMeta() instanceof Damageable))
            return;
        Bukkit.broadcastMessage("damageable tool found");
        ItemStack itemClone = event.getItem().clone();
        Damageable tool = (Damageable) event.getItem().getItemMeta();
        ItemStack item = event.getItem();
        markItem(item);
        event.getPlayer().updateInventory();
        int itemSlot = event.getPlayer().getInventory().first(item);
        unmarkItem(item);
        event.getPlayer().updateInventory();
        if (event.getItem().getType().getMaxDurability()-tool.getDamage()<5) {
            Bukkit.broadcastMessage("starting delayEvent");
            BukkitRunnable delayEvent = new DelayEvent(event, itemClone, itemSlot);
            delayEvent.runTask(plugin);
        }
    }

    public static void markItem(ItemStack item){
        ItemMeta imeta = item.getItemMeta();
        List<String> lore;
        assert imeta != null;
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
        assert imeta != null;
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
        private int itemSlot;

        public DelayEvent(PlayerItemDamageEvent event, ItemStack item, int itemSlot){
            this.event = event;
            this.item = item;
            this.itemSlot = itemSlot;
        }

        @Override
        public void run() {
            Bukkit.broadcastMessage("executing delayEvent");
            Bukkit.broadcastMessage(event.getItem().getType().name());
            Bukkit.broadcastMessage(item.getType().name());
            if (event.getItem().getType() != item.getType()){
                Bukkit.broadcastMessage("ReplaceBrokenTool");
                ReplaceTool rt = new ReplaceTool(event.getPlayer(), item, itemSlot);
                rt.replaceBrokenTool();
                return;
            }
            if (!event.getItem().getEnchantments().isEmpty()) {
                Bukkit.broadcastMessage("SwapTool");
                ReplaceTool rt = new ReplaceTool(event.getPlayer(), item, itemSlot);
                rt.swapTool();
                return;
            }
        }
    }
}
