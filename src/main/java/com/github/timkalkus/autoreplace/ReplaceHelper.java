package com.github.timkalkus.autoreplace;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ReplaceHelper {

    private final Player player;
    private final Inventory inventory;
    private final ItemStack item;
    private final Integer itemSlot;

    private ShulkerBoxHelper shulkerBox = null;
    private int shulkerBoxLocation = -1;
    private int replacementItemSlot = -1;

    public ReplaceHelper(Player player, ItemStack item, int itemSlot) {
        this.player = player;
        this.inventory = player.getInventory();
        this.item = item;
        this.itemSlot = itemSlot;
    }

    /*public ReplaceHelper(Player player, ItemStack item, int handSlot){
        this.player = player;
        this.inventory = player.getInventory();
        this.item = item;
        this.itemSlot = null;
        this.handSlot = handSlot;
    }*/

    public void replace() {
        findReplacement();
        if (replacementItemSlot == -1) {
            return;
        }
        if (shulkerBox != null) {
            player.getInventory().setItem(itemSlot, shulkerBox.getInventory().getItem(replacementItemSlot));
            shulkerBox.getInventory().clear(replacementItemSlot);
            player.getInventory().setItem(shulkerBoxLocation, shulkerBox.getUpdatedShulkerItem());
        } else {
            player.getInventory().setItem(itemSlot, player.getInventory().getItem(replacementItemSlot));
            player.getInventory().clear(replacementItemSlot);
        }
        player.updateInventory();
    }

    public void swapTool() {
        findReplacement();
        if (replacementItemSlot == -1) {
            saveTool();
            return;
        }
        ItemStack replacementItem;
        if (shulkerBox != null) {
            replacementItem = shulkerBox.getInventory().getItem(replacementItemSlot);
            shulkerBox.getInventory().setItem(replacementItemSlot, item);
            player.getInventory().setItem(itemSlot, replacementItem);
            player.getInventory().setItem(shulkerBoxLocation, shulkerBox.getUpdatedShulkerItem());
        } else {
            replacementItem = player.getInventory().getItem(replacementItemSlot);
            player.getInventory().setItem(replacementItemSlot, item);
            player.getInventory().setItem(itemSlot, replacementItem); // here nullpoint exception occures for armour slots
        }
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
        player.updateInventory();
    }

    public void saveTool() {
        int saveSlot = inventory.firstEmpty();
        if (saveSlot != -1) {
            inventory.setItem(saveSlot, item);
            inventory.setItem(itemSlot, null);
        } else { // only works for in-hand items
            ItemStack offHand = player.getInventory().getItemInOffHand();
            player.getInventory().setItemInOffHand(inventory.getItem(itemSlot));
            player.getInventory().setItemInMainHand(offHand);
        }
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
        player.updateInventory();
    }

    private void findReplacement() {
        // Searches inventory for first possible replacement
        ItemStack[] invContent = inventory.getContents();
        // Search first in shulker boxes
        for (int i = 0; i < invContent.length; i++) {
            if (isShulker(invContent[i])) {
                ShulkerBoxHelper sbh = new ShulkerBoxHelper(invContent[i]);
                ItemStack[] sbContent = sbh.getInventory().getContents();
                for (int j = 0; j < sbContent.length; j++) {
                    if (isPossibleReplacement(sbContent[j])) {
                        this.shulkerBox = sbh;
                        this.shulkerBoxLocation = i;
                        this.replacementItemSlot = j;
                        return;
                    }
                }
            }
        }
        // then in actual inventory
        for (int i = 0; i < invContent.length; i++) {
            if (itemSlot != null && i == itemSlot) {
                continue;
            }
            if (isPossibleReplacement(invContent[i])) {
                this.replacementItemSlot = i;
                return;
            }
        }
    }

    private boolean isShulker(ItemStack item) {
        if (item == null)
            return false;
        try {
            return item.getType().name().contains("SHULKER_BOX");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isPossibleReplacement(ItemStack item) {
        if (item == null)
            return false;
        try {
            if (!item.getType().equals(this.item.getType()))
                return false;
            // don't replace non-enchanted tools with enchanted ones
            if (item.getEnchantments().isEmpty() != this.item.getEnchantments().isEmpty())
                return false;
            if (item.hasItemMeta() && item.getItemMeta() instanceof Damageable && item.getType().getMaxDurability() != 0)
                return ((Damageable) item.getItemMeta()).getDamage() * 1.0 / item.getType().getMaxDurability() < .5;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
