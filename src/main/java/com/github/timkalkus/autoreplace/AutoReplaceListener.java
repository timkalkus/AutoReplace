package com.github.timkalkus.autoreplace;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoReplaceListener implements Listener{

    private final AutoReplaceMain plugin;
    private static final String privateKey = "AutoReplaceKey";

    public AutoReplaceListener(AutoReplaceMain plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void itemDamaged(PlayerItemDamageEvent event){
        if (!event.getItem().hasItemMeta())
            return;
        if (!(event.getItem().getItemMeta() instanceof Damageable))
            return;
        if (!plugin.getPlayerToolEnabled(event.getPlayer()))
            return;
        ItemStack itemClone = event.getItem().clone();
        Damageable tool = (Damageable) event.getItem().getItemMeta();
        ItemStack item = event.getItem();
        markItem(item);
        event.getPlayer().updateInventory();
        int itemSlot = event.getPlayer().getInventory().first(item);
        unmarkItem(item);
        event.getPlayer().updateInventory();
        if (event.getItem().getType().getMaxDurability()-tool.getDamage()<5) {
            BukkitRunnable delayEvent = new ToolDelayEvent(event, itemClone, itemSlot);
            delayEvent.runTask(plugin);
        }
    }

    @EventHandler
    public void itemPlaced(PlayerInteractEvent event) {
        if (!event.hasItem())
            return; // ignore all event-calls without an item
        if (Objects.requireNonNull(event.getItem()).getMaxStackSize()==1)
            return; // ignore when tool, bucket or other non-stackable item
        if (event.getItem().getAmount()>1)
            return; // ignore if initital stack size is bigger than 1
        if (!plugin.getPlayerItemEnabled(event.getPlayer()))
            return;
        //Bukkit.broadcastMessage("Hand: " + event.getHand().name() + ", TypeName: " + event.getItem().getType().name() + ", Amount:" + event.getItem().getAmount());
        BukkitRunnable itemDelayEvent = new ItemDelayEvent(event, event.getItem().clone(), event.getHand());
        itemDelayEvent.runTask(plugin);
    }

    public static void markItem(ItemStack item){
        ItemMeta imeta = item.getItemMeta();
        List<String> lore;
        assert imeta != null;
        if (imeta.hasLore())
            lore = imeta.getLore();
        else
            lore = new ArrayList<>();
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

    private class ItemDelayEvent extends BukkitRunnable{

        private final PlayerInteractEvent event;
        private final ItemStack item; // item before
        private final EquipmentSlot hand;

        private ItemDelayEvent(PlayerInteractEvent event, ItemStack item, EquipmentSlot hand) {
            this.event = event;
            this.item = item;
            this.hand = hand;
        }

        @Override
        public void run() {
            if (Objects.requireNonNull(event.getItem()).getType().equals(item.getType()))
                return; // (original) stack still there
            if (!event.getPlayer().getInventory().getItem(hand).getType().isAir())
                return; // item was replaced by other item than air, e.g. bucket was filled with water
            ReplaceHelper rt = new ReplaceHelper(event.getPlayer(), item, hand);
            rt.replace();
        }
    }

    private class ToolDelayEvent extends BukkitRunnable{
        private final PlayerItemDamageEvent event;
        private final ItemStack item;
        private final int itemSlot;

        public ToolDelayEvent(PlayerItemDamageEvent event, ItemStack item, int itemSlot){
            this.event = event;
            this.item = item;
            this.itemSlot = itemSlot;
        }

        @Override
        public void run() {
            if (event.getItem().getType() != item.getType()){
                ReplaceHelper rt = new ReplaceHelper(event.getPlayer(), item, itemSlot);
                rt.replace();
                return;
            }
            if (!event.getItem().getEnchantments().isEmpty()) {
                ReplaceHelper rt = new ReplaceHelper(event.getPlayer(), event.getItem(), itemSlot);
                rt.swapTool();
            }
        }
    }
}
