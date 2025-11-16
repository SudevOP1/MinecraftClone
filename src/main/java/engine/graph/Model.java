package engine.graph;

import engine.scene.Entity;

import java.util.*;

public class Model {

    private final String id;
    private List<Entity> entitiesList;
    private List<Material> materialList;

    public Model(String id, List<Material> materialList) {
        this.id = id;
        this.materialList = materialList;
        this.entitiesList = new ArrayList<>();
    }

    public List<Entity> getEntitiesList() {
        return this.entitiesList;
    }

    public String getId() {
        return this.id;
    }

    public List<Material> getMaterialList() {
        return this.materialList;
    }

    public void cleanup() {
        this.materialList.forEach((material) -> {
            material.cleanup();
        });
    }

}
