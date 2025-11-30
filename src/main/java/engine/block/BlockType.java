package engine.block;

public class BlockType {

    public String name;
    public String codename;
    public String texturePath;

    // for GSON
    public BlockType() {
    }

    public BlockType(String name, String codename) {
        this.name = name;
        this.codename = codename;
        this.texturePath = "models/" + codename + ".png";
    }

    public BlockType(String name, String codename, String texturePath) {
        this.name = name;
        this.codename = codename;
        this.texturePath = texturePath;
    }

    @Override
    public String toString() {
        return "BlockType{name='" + name + "', codename='" + codename + "', texturePath='" + texturePath + "'}";
    }
}