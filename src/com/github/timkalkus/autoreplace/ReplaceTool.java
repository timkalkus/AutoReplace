package com.github.timkalkus.autoreplace;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;

public class ReplaceTool {
    public static void findReplacement(ItemStack item, Player player){
        Iterable<ItemStack> inv = player.getInventory();
        List<ItemStack> possible_replacements = new ArrayList<>();
        for (ItemStack inv_item:inv) {
            try {
                if (inv_item!= null) {
                    if (isSimilar(inv_item,item) && inv_item.hasItemMeta()){
                        if (((Damageable) inv_item.getItemMeta()).getDamage()<10){
                            possible_replacements.add(inv_item);
                        }
                    }
                }
            }
            catch (Exception e){
                Bukkit.broadcastMessage("Exception " + e.getMessage() + " occured with " + inv_item.getType().name());
            }

        }
        if (possible_replacements.size()!=0) {
            switchItems(possible_replacements.get(0),item, player);
        }
    }

    private static void switchItems(ItemStack first, ItemStack second, Player player){
        int i1 = player.getInventory().first(first);
        int i2 = player.getInventory().first(second);
        player.getInventory().setItem(i1,second);
        player.getInventory().setItem(i2,first);
    }

    private static boolean isSimilar(ItemStack first,ItemStack second){
        if(first == null || second == null){
            return false;
        }
        return first.getType().name().equals(second.getType().name());
    }
}
