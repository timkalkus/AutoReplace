package com.github.timkalkus.autoreplace;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AutoReplaceTest<MyPlugin> {
    private ServerMock server;
    private AutoReplaceMain pluginMain;

    /**
     * Initialized Bukkit-Server-Mock and saves instance of AutoReplaceMain locally
     */
    @BeforeEach
    public void setUp()
    {
        server = MockBukkit.mock();
        pluginMain = MockBukkit.load(AutoReplaceMain.class);
    }

    @AfterEach
    public void tearDown()
    {
        MockBukkit.unmock();
    }

    /**
     * Checks that non-enchanted tools will be replaced when broken
     */
    @Test
    public void replaceNonEnchantedTool(){
        // TODO implement
    }

    /**
     * Checks that non-enchanted tools won't be moved with low durability left
     */
    @Test
    public void keepNonEnchantedTool(){
        // TODO implement
    }

    /**
     * Checks that enchanted tools will be replaced with low durability left
     */
    @Test
    public void swapEnchantedTool(){
        // TODO implement
    }

    /**
     * Checks that non-enchanted tools will be moved with low durability left, if no replacement is available
     */
    @Test
    public void saveEnchantedTool(){
        // create player and add single item
        Player player = server.addPlayer();
        ItemStack item = getDamageable(Material.IRON_PICKAXE,5,true);
        player.getInventory().setItem(0,item);
        // execute event
        executeItemDamageEvent(player, item);
        // check if item was moved
        assertNotSame(item, player.getInventory().getItem(0),"Item was not moved in inventory");
        assertTrue(player.getInventory().contains(item),"Item was not found in inventory");
    }

    /**
     * Imitates vanilla ItemDamageEvent behaviour by calling the event and increasing the damage if the event is not canceled
     * @param player initiator of the event
     * @param item item of the event, has to be damagable
     */
    private void executeItemDamageEvent(Player player, ItemStack item) {
        assertTrue(item.hasItemMeta(),"Item missing ItemMeta");
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta instanceof Damageable);
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, item, 1);
        server.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(damageable.getDamage()+1);
        }
        server.getScheduler().performOneTick();
    }

    /**
     * Imitates vanilla PlayerInteractEvent behaviour by calling the event and reducing the amount by 1 if the event is not canceled
     * @param player initiator of the event
     * @param item item of the event
     */
    private void executeItemUsedEvent(Player player, ItemStack item) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, null, BlockFace.EAST, null);
        server.getPluginManager().callEvent(event);
        if (!event.useItemInHand().equals(Event.Result.DENY) || !event.useInteractedBlock().equals(Event.Result.DENY)) {
            item.setAmount(item.getAmount()-1);
        }
        server.getScheduler().performOneTick();
    }

    /**
     * Creates a ItemStack with the chosen material, remaining durability and optional enchantment
     * @param material chosen material, has to be damageable
     * @param remainingDurability how much durability should be left, -1 for no damage
     * @param isEnchanted whether the tool should have an enchantment
     * @return modified ItemStack
     */
    private ItemStack getDamageable(Material material, int remainingDurability, boolean isEnchanted){
        ItemStack item = new ItemStack(material);
        assertTrue(item.hasItemMeta(),"Item missing ItemMeta");
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta, "ItemMeta is null");
        if (isEnchanted) {
            meta.addEnchant(Enchantment.MENDING,0,true);
        }
        assertTrue(meta instanceof Damageable);
        if (remainingDurability>0){
            ((Damageable) meta).setDamage(material.getMaxDurability()-remainingDurability);
        }
        item.setItemMeta(meta);
        return item;
    }
}
