package engine.scene;

import engine.graph.Model;

import java.util.*;

public class Scene {

    private Map<String, Model> modelMap;
    private Projection projection;

    public Scene(int width, int height) {
        this.modelMap = new HashMap<>();
        this.projection = new Projection(width, height);
    }

    public void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        model.getEntitiesList().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public void cleanup() {
        modelMap.values().forEach((model) -> {
            model.cleanup();
        });
    }

    public Map<String, Model> getModelMap() {
        return modelMap;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public void resize(int width, int height) {
        this.projection.updateProjMatrix(width, height);
    }

}