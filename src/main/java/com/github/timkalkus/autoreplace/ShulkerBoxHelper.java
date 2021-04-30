package com.github.timkalkus.autoreplace;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

@SuppressWarnings("unused")
public class ShulkerBoxHelper {
    private ItemStack shulker;
    private BlockStateMeta bsm;
    private ShulkerBox box;

    public ShulkerBoxHelper() {
        this(new ItemStack(Material.WHITE_SHULKER_BOX));
    }

    public ShulkerBoxHelper(ItemStack shulker) throws AssertionError {
        setShulkerBox(shulker);
    }

    public void setShulkerBox(ItemStack shulker) throws AssertionError {
        this.shulker = shulker;
        this.bsm = (BlockStateMeta) shulker.getItemMeta();
        assert bsm != null;
        this.box = (ShulkerBox) bsm.getBlockState();
    }

    public void setColor(Material material) throws AssertionError {
        assert material.name().contains("SHULKER_BOX");
        box.setType(material);
    }

    public Inventory getInventory() {
        return box.getInventory();
    }

    public ItemStack getUpdatedShulkerItem() {
        bsm.setBlockState(box);
        shulker.setItemMeta(bsm);
        box.update();
        return shulker;
    }
}
