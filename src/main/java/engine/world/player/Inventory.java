package engine.world.player;

import engine.item.ItemRegistery;
import engine.item.ItemType;

public class Inventory {

    public static final int SIZE = 9 * 4;
    public static final int HOTBAR_SIZE = 9;
    // first 9 slots for hotbar
    // last 27 slots for main inventory

    private ItemType[] items;
    private int selectedSlot;

    public Inventory() {
        this.items = new ItemType[SIZE];
        this.selectedSlot = 0;
    }

    public ItemType get(int slot) {
        return this.items[slot];
    }

    public void set(int slot, ItemType item) {
        this.items[slot] = item;
    }

    public void set(int slot, String codename) {
        this.items[slot] = ItemRegistery.get(codename);
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public ItemType getHotbarItem() {
        return this.items[this.selectedSlot];
    }

    public void setHotbarItem(ItemType item) {
        this.items[this.selectedSlot] = item;
    }

    public void clearHotbar() {
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            this.items[i] = null;
        }
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            this.items[i] = null;
        }
    }

}
