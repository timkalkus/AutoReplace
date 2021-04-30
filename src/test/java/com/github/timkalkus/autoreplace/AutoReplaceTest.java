package com.github.timkalkus.autoreplace;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.diogonunes.jcolor.Ansi;
import com.diogonunes.jcolor.Attribute;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class AutoReplaceTest {
    private ServerMock server;

    /**
     * Initialized Bukkit-Server-Mock and saves instance of AutoReplaceMain locally
     */
    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        MockBukkit.load(AutoReplaceMain.class);
        server.getLogger().setLevel(Level.WARNING);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Checks that non-enchanted tools will be replaced when broken
     */
    @Test
    public void replaceNonEnchantedTool() {
        // create player and add one damaged and one undamaged item
        Player player = server.addPlayer();
        ItemStack item0 = getDamageable(Material.IRON_PICKAXE, 1, false);
        ItemStack item1 = getDamageable(Material.IRON_PICKAXE, -1, false);
        player.getInventory().setItem(0, item0);
        item0 = player.getInventory().getItem(0);
        player.getInventory().setItem(1, item1);
        // execute event
        executeItemDamageEvent(player, item0);
        // check if item was replaced
        assertTrue(getRestDurability(player.getInventory().getItem(0)) > 5, "replacement item was not moved");
        assertEquals(1, Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).
                filter(items -> !AutoReplaceListener.isNullOrAir(items)).count(), "amount of items in inventory does not match expectations");
    }

    /**
     * Checks that non-enchanted tools won't be moved with low durability left
     */
    @Test
    public void keepNonEnchantedTool() {
        // create player and add one damaged and one undamaged item
        Player player = server.addPlayer();
        ItemStack item0 = getDamageable(Material.IRON_PICKAXE, 5, false);
        ItemStack item1 = getDamageable(Material.IRON_PICKAXE, -1, false);
        player.getInventory().setItem(0, item0);
        item0 = player.getInventory().getItem(0);
        player.getInventory().setItem(1, item1);
        // execute event
        executeItemDamageEvent(player, item0);
        // check if item was replaced
        assertTrue(getRestDurability(player.getInventory().getItem(0)) < 5, "damaged item was incorrectly moved");
        assertTrue(getRestDurability(player.getInventory().getItem(1)) > 5, "replacement item was incorrectly moved");
        assertEquals(2, Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).
                filter(items -> !AutoReplaceListener.isNullOrAir(items)).count(), "amount of items in inventory does not match expectations");
    }

    /**
     * Checks that enchanted tools will be replaced with low durability left
     */
    @Test
    public void swapEnchantedTool() {
        // create player and add one damaged and one undamaged item
        Player player = server.addPlayer();
        ItemStack item0 = getDamageable(Material.IRON_PICKAXE, 5, true);
        ItemStack item1 = getDamageable(Material.IRON_PICKAXE, -1, true);
        player.getInventory().setItem(0, item0);
        item0 = player.getInventory().getItem(0);
        player.getInventory().setItem(1, item1);
        // execute event
        executeItemDamageEvent(player, item0);
        // check if item was replaced
        assertNotSame(item0, player.getInventory().getItem(0), "damaged item was not moved in inventory");
        assertTrue(getRestDurability(player.getInventory().getItem(0)) > 5, "damaged item was not moved");
        assertTrue(getRestDurability(player.getInventory().getItem(1)) < 5, "replacement item was not moved");
        assertEquals(2, Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).
                filter(items -> !AutoReplaceListener.isNullOrAir(items)).count(), "amount of items in inventory does not match expectations");
    }

    /**
     * Checks that non-enchanted tools will be moved with low durability left, if no replacement is available
     */
    @Test
    public void saveEnchantedTool() {
        // create player and add single item
        Player player = server.addPlayer();
        ItemStack item = getDamageable(Material.IRON_PICKAXE, 5, true);
        // places clone in inventory
        player.getInventory().setItem(0, item);
        // redirects reference to cloned item in inventory
        item = player.getInventory().getItem(0);
        assertNotNull(item);
        // execute event
        executeItemDamageEvent(player, item);
        // check if item was moved
        assertTrue(AutoReplaceListener.isNullOrAir(player.getInventory().getItem(0)), "Item was not moved in inventory");
        assertTrue(player.getInventory().contains(item), "Item was not found in inventory");
    }

    @Test
    public void refillStack() {
        // create player and add single item
        Player player = server.addPlayer();
        ItemStack item = new ItemStack(Material.STONE, 1);
        // places clone in inventory
        player.getInventory().setItem(0, item);
        // redirects reference to cloned item in inventory
        item = player.getInventory().getItem(0);
        player.getInventory().setItem(1, new ItemStack(Material.STONE, 64));
        int itemCount = amountInInventory(player.getInventory(), Material.STONE);
        assertNotNull(item);
        // execute event
        executeItemUsedEvent(player, item);
        // check if item was moved
        assertFalse(AutoReplaceListener.isNullOrAir(player.getInventory().getItem(0)), "Stack is empty, but should be refilled");
        assertEquals(itemCount - 1, amountInInventory(player.getInventory(), Material.STONE));
    }

    /**
     * Imitates vanilla ItemDamageEvent behaviour by calling the event and increasing the damage if the event is not canceled
     *
     * @param player initiator of the event
     * @param item   item of the event, has to be damagable
     */
    private void executeItemDamageEvent(Player player, ItemStack item) {
        assertNotNull(player);
        assertNotNull(item);
        assertTrue(item.hasItemMeta(), "Item missing ItemMeta");
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta);
        assertTrue(meta instanceof Damageable);
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, item, 1);
        server.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(damageable.getDamage() + 1);
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setType(Material.AIR);
            }
        }
        item.setItemMeta(meta);
        server.getScheduler().performOneTick();
    }

    /**
     * Imitates vanilla PlayerInteractEvent behaviour by calling the event and reducing the amount by 1 if the event is not canceled
     *
     * @param player initiator of the event
     * @param item   item of the event
     */
    private void executeItemUsedEvent(Player player, ItemStack item) {
        assertNotNull(player);
        assertNotNull(item);
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, item, null, BlockFace.EAST, null);
        server.getPluginManager().callEvent(event);
        if (!event.useItemInHand().equals(Event.Result.DENY) || !event.useInteractedBlock().equals(Event.Result.DENY)) {
            item.setAmount(item.getAmount() - 1);
            item.setType(Material.AIR);
        }
        server.getScheduler().performOneTick();
    }

    /**
     * Creates a ItemStack with the chosen material, remaining durability and optional enchantment
     *
     * @param material            chosen material, has to be damageable
     * @param remainingDurability how much durability should be left, -1 for no damage
     * @param isEnchanted         whether the tool should have an enchantment
     * @return modified ItemStack
     */
    @SuppressWarnings("SameParameterValue")
    private ItemStack getDamageable(Material material, int remainingDurability, boolean isEnchanted) {
        ItemStack item = new ItemStack(material);
        assertTrue(item.hasItemMeta(), "Item missing ItemMeta");
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta, "ItemMeta is null");
        if (isEnchanted) {
            meta.addEnchant(Enchantment.MENDING, 0, true);
        }
        assertTrue(meta instanceof Damageable);
        if (remainingDurability > 0) {
            ((Damageable) meta).setDamage(material.getMaxDurability() - remainingDurability);
        }
        item.setItemMeta(meta);
        return item;
    }

    private int getRestDurability(ItemStack item) {
        assertNotNull(item);
        assertTrue(item.hasItemMeta(), "Item missing ItemMeta");
        ItemMeta meta = item.getItemMeta();
        assertNotNull(meta, "ItemMeta is null");
        assertTrue(meta instanceof Damageable);
        return item.getType().getMaxDurability() - ((Damageable) meta).getDamage();
    }

    /**
     * calculates the amount of items with the specified material
     */
    @SuppressWarnings("SameParameterValue")
    private int amountInInventory(Inventory inventory, Material material) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType().equals(material)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Print the input inventory e.g. as quick manual check
     */
    @SuppressWarnings("unused")
    private void printInventory(Inventory inventory) {
        List<String> invString = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            invString.add(itemStackToString(item));
        }
        System.out.println(invString);
    }

    /**
     * generate String with essential informations for quick manual checks
     *
     * @param item input item
     * @return essential information as String
     */
    private String itemStackToString(ItemStack item) {
        if (item == null) {
            return "null";
        }
        String returnString = item.getAmount() + "x" + item.getType().name();
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            assertNotNull(meta);
            if (meta.hasEnchants()) {
                returnString += "*";
            }
            if (meta instanceof Damageable) {
                returnString += " " + (item.getType().getMaxDurability() - ((Damageable) meta).getDamage()) +
                        "/" + item.getType().getMaxDurability();
            }
        }
        returnString += " " + Integer.toHexString(item.hashCode());
        int r = item.hashCode() % 256;
        int g = item.hashCode() / 16 % 256;
        int b = item.hashCode() / 256 % 256;
        return Ansi.colorize(returnString, Attribute.TEXT_COLOR(Math.abs(r), Math.abs(g), Math.abs(b)));
        //return returnString;
    }
}
