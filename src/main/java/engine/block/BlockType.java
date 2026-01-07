package engine.block;

public class BlockType {

    public String name;
    public String codename;

    // texture indices for each face (indexed starting from 1)
    public int texture1; // top
    public int texture2; // bottom
    public int texture3; // front
    public int texture4; // back
    public int texture5; // left
    public int texture6; // right

    // texture rotations (1=0°, 2=90°, 3=180°, 4=270°)
    public int textureRotation1; // top
    public int textureRotation2; // bottom
    public int textureRotation3; // front
    public int textureRotation4; // back
    public int textureRotation5; // left
    public int textureRotation6; // right

    // transparency
    public boolean hasTransparency;

    // for GSON
    public BlockType() {
    }

    public int[] getTextureIndices() {
        return new int[] { texture3, texture1, texture6, texture5, texture2, texture4 };
    }

    public int[] getTextureRotations() {
        return new int[] {
                textureRotation3,
                textureRotation1,
                textureRotation6,
                textureRotation5,
                textureRotation2,
                textureRotation4
        };
    }

    @Override
    public String toString() {
        return "BlockType{name='" + name + "', codename='" + codename + "'}";
    }
}
