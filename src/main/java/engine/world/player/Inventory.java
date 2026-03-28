package engine.world.player;

import engine.block.BlockRegistry;
import engine.block.BlockType;
import engine.item.ItemRegistry;
import engine.item.ItemType;
import game.Settings;

public class Inventory {

    private ItemType[] items;
    private int selectedSlot;
    private int[] itemCounts;

    public Inventory() {
        this.items = new ItemType[Settings.HOTBAR_CELL_COUNT];
        this.selectedSlot = 0;
        this.itemCounts = new int[Settings.HOTBAR_CELL_COUNT];
    }

    public ItemType getItem(int slot) {
        return this.items[slot];
    }

    public void setItem(int slot, ItemType item, int count) {
        this.items[slot] = item;
        this.itemCounts[slot] = count;
    }

    public void setItem(int slot, ItemType item) {
        this.items[slot] = item;
    }

    public void setItem(int slot, String codename, int count) {
        this.items[slot] = ItemRegistry.get(codename);
        this.itemCounts[slot] = count;
    }

    public void setItem(int slot, String codename) {
        this.items[slot] = ItemRegistry.get(codename);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public ItemType getSelectedItemType() {
        return this.items[this.selectedSlot];
    }

    public void setSelectedItemType(ItemType item) {
        this.items[this.selectedSlot] = item;
    }

    public BlockType getSelectedBlockType() {
        if (this.items[this.selectedSlot] == null) {
            return null;
        }
        return BlockRegistry.get(this.items[this.selectedSlot].codename);
    }

    public void setSelectedBlockType(BlockType blockType) {
        this.items[this.selectedSlot] = ItemRegistry.get(blockType.codename);
    }

    public int getItemCount(int slot) {
        return this.itemCounts[slot];
    }

    public void setItemCount(int slot, int count) {
        this.itemCounts[slot] = count;
    }

    public void incrementItemCount(int slot) {
        this.itemCounts[slot]++;
    }

    public void decrementItemCount(int slot) {
        this.itemCounts[slot]--;
        if (this.itemCounts[slot] == 0) {
            this.items[slot] = null;
        }
    }

    public void clearInventory() {
        for (int i = 0; i < Settings.INVENTORY_SIZE; i++) {
            this.items[i] = null;
        }
    }

    public void clearHotbar() {
        for (int i = 0; i < Settings.HOTBAR_CELL_COUNT; i++) {
            this.items[i] = null;
        }
    }

    public void clearSlot(int slot) {
        this.items[slot] = null;
        this.itemCounts[slot] = 0;
    }

}
