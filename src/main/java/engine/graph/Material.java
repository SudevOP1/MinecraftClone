package engine.graph;

import java.util.*;

public class Material {

    private List<Mesh> meshList;
    private String texturePath;

    public Material() {
        meshList = new ArrayList<>();
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

    public void cleanup() {
        this.meshList.forEach((mesh) -> {
            mesh.cleanup();
        });
    }

}
