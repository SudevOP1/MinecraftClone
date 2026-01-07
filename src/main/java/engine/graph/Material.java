package engine.graph;

import java.util.*;

public class Material {

    private List<Mesh> meshList;
    private String texturePath;
    private boolean transparent;

    public Material() {
        meshList = new ArrayList<>();
        transparent = false; // default
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

    public void cleanup() {
        this.meshList.forEach((mesh) -> {
            mesh.cleanup();
        });
    }

}
