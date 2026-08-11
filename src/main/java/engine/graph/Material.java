package engine.graph;

import java.util.*;

public class Material {

    private List<Mesh> meshList;
    private String texturePath;
    private boolean transparent;
    private boolean alphaCutout;

    public Material() {
        meshList = new ArrayList<>();
        transparent = false; // default
        alphaCutout = false; // default
    }

    public List<Mesh> getMeshList() {
        return this.meshList;
    }

    public String getTexturePath() {
        return this.texturePath;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    // Cutout materials (leaves) are drawn in the opaque pass with fully
    // transparent texels discarded in the fragment shader. That makes them
    // order-independent, so batching them into one mesh per chunk cannot break
    // their sorting the way alpha blending would.
    public boolean isAlphaCutout() {
        return alphaCutout;
    }

    public void setAlphaCutout(boolean alphaCutout) {
        this.alphaCutout = alphaCutout;
    }

    public void cleanup() {
        this.meshList.forEach((mesh) -> {
            mesh.cleanup();
        });
    }

}
