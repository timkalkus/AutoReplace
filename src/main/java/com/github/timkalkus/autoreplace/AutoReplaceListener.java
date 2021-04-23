package com.github.timkalkus.autoreplace;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
        int itemSlot = getItemSlot(event);
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
        BukkitRunnable itemDelayEvent = new ItemDelayEvent(event, event.getItem().clone(), getItemSlot(event));
        itemDelayEvent.runTask(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(player.hasPermission(plugin.arToolForce)) {
            forceTool(player);
        }
        if(player.hasPermission(plugin.arItemForce)){
            forceItem(player);
        }
    }

    private void forceItem(Player player) {
        if (player.hasPermission(plugin.arItemActivatedFalse)){
            plugin.setPlayerItem(player, false);
            return;
        }
        if (player.hasPermission(plugin.arItemActivatedTrue)){
            plugin.setPlayerItem(player, true);
            return;
        }
        plugin.setPlayerItem(player);
    }

    private void forceTool(Player player) {
        if (player.hasPermission(plugin.arToolActivatedFalse)){
            plugin.setPlayerTool(player, false);
            return;
        }
        if (player.hasPermission(plugin.arToolActivatedTrue)){
            plugin.setPlayerTool(player, true);
            return;
        }
        plugin.setPlayerTool(player);
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

    private int getItemSlot(PlayerItemDamageEvent event){
        ItemStack item = event.getItem();
        markItem(item);
        event.getPlayer().updateInventory();
        ItemStack[] inv = event.getPlayer().getInventory().getContents();
        int itemSlot = -1;
        for (int i = 0; i<inv.length; i++){
            if (item.equals(inv[i])){
                itemSlot = i;
                break;
            }
        }
        unmarkItem(item);
        event.getPlayer().updateInventory();
        return itemSlot;
    }

    private int getItemSlot(PlayerInteractEvent event){
        ItemStack item = event.getItem();
        markItem(item);
        event.getPlayer().updateInventory();
        ItemStack[] inv = event.getPlayer().getInventory().getContents();
        int itemSlot = -1;
        for (int i = 0; i<inv.length; i++){
            if (item.equals(inv[i])){
                itemSlot = i;
                break;
            }
        }
        unmarkItem(item);
        event.getPlayer().updateInventory();
        return itemSlot;
    }

    private boolean isNullOrAir(ItemStack item) {
        return item==null || isAir(item.getType());
    }

    private boolean isAir(Material material){
        return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
    }

    private class ItemDelayEvent extends BukkitRunnable{

        private final PlayerInteractEvent event;
        private final ItemStack item; // item before
        private final int handSlot;

        private ItemDelayEvent(PlayerInteractEvent event, ItemStack item, int handSlot) {
            this.event = event;
            this.item = item;
            this.handSlot = handSlot;
        }

        @Override
        public void run() {
            if (Objects.requireNonNull(event.getItem()).getType().equals(item.getType()))
                return; // (original) stack still there
            if (!isNullOrAir(event.getPlayer().getInventory().getItem(handSlot)))
                return; // item was replaced by other item than air, e.g. bucket was filled with water
            ReplaceHelper rt = new ReplaceHelper(event.getPlayer(), item, handSlot);
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
