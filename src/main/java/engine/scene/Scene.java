package engine.scene;

import engine.graph.Mesh;

import java.util.*;

public class Scene {

    private Map<String, Mesh> meshMap;

    public Scene() {
        meshMap = new HashMap<>();
    }

    public void addMesh(String meshID, Mesh mesh) {
        meshMap.put(meshID, mesh);
    }

    public void cleanup() {
        meshMap.values().forEach((mesh) -> {
            mesh.cleanup();
        });
    }

    public Map<String, Mesh> getMeshMap() {
        return meshMap;
    }
}