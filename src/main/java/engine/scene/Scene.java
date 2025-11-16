package engine.scene;

import engine.graph.Mesh;

import java.util.*;

public class Scene {

    private Map<String, Mesh> meshMap;
    private Projection projection;

    public Scene(int width, int height) {
        meshMap = new HashMap<>();
        this.projection = new Projection(width, height);
    }

    public Projection getProjection() {
        return this.projection;
    }

    public void resize(int width, int height) {
        this.projection.updateProjMatrix(width, height);
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