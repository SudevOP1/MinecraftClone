package engine.item;

public class ItemType {

    public String codename;
    public String name;
    public int icon;

    // for GSON
    public ItemType() {
    }

    @Override
    public String toString() {
        return "ItemType{name='" + name + "', codename='" + codename + "'}";
    }

}
