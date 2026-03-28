package engine.item;

public class ItemType {

    public String codename;
    public String name;
    public int icon;
    public int stackSize;

    // for GSON
    public ItemType() {
    }

    @Override
    public String toString() {
        return "ItemType{name='" + name + "', codename='" + codename + "'}";
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public String getCodename() {
        return this.codename;
    }

    public String getName() {
        return this.name;
    }

    public int getIcon() {
        return this.icon - 1; // icon values are 1-indexed in items_data.json
    }

}
