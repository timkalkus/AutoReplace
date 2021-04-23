package com.github.timkalkus.autoreplace;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AutoReplaceTest<MyPlugin> {
    private ServerMock server;
    private Plugin plugin;
    private AutoReplaceMain pluginMain;

    @BeforeEach
    public void setUp()
    {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AutoReplaceMain.class);
        pluginMain = (AutoReplaceMain) plugin;
    }

    @AfterEach
    public void tearDown()
    {
        MockBukkit.unmock();
    }

    @Test
    public void checkBasicMockBukkitFunctionality(){
        Player player = server.addPlayer();
        ItemStack item = new ItemStack(Material.IRON_PICKAXE);
        assertTrue(item.hasItemMeta(),"Item missing ItemMeta");
        //item.addUnsafeEnchantment(Enchantment.MENDING,0);
        //Damageable damageable = (Damageable) meta;
        //damageable.setDamage(100);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.addEnchant(Enchantment.MENDING,0,true);
        assertTrue(meta instanceof Damageable);
        ((Damageable) meta).setDamage(248);
        item.setItemMeta(meta.clone());
        player.getInventory().addItem(item);
        assertNotNull(player);
        item = player.getInventory().getItem(0);
        assert item != null;
        meta = item.getItemMeta();
        assertTrue(meta instanceof Damageable);
        System.out.println("l.59:" + ((Damageable) meta).getDamage());
        /*
        * not yet functional */
        System.out.println("l.62:" + server.getScheduler().getPendingTasks().size());
        PlayerItemDamageEvent event = new PlayerItemDamageEvent(player, item, 1);
        System.out.println("l.64:" + Arrays.toString(event.getHandlers().getRegisteredListeners()));
        server.getPluginManager().callEvent(event);
        System.out.println("l.66:" + server.getScheduler().getPendingTasks().size());
        server.getScheduler().performOneTick();
        System.out.println("l.68:" + server.getScheduler().getPendingTasks().size());
        server.getScheduler().performOneTick();
        System.out.println("l.70:" + server.getScheduler().getPendingTasks().size());
        //item = player.getInventory().getItem(0);
        /*
        assert item != null;
        meta = item.getItemMeta();
        assertTrue(meta instanceof Damageable);
        System.out.println("l.75:" + ((Damageable) meta).getDamage());
        //server.getPlayer(0).getInventory().setContents();
        System.out.println(Arrays.stream(player.getInventory().getContents()).filter(Objects::nonNull).count());
        for (ItemStack items:player.getInventory().getContents()){
            System.out.println(items.toString());
        }*/
    }
}
