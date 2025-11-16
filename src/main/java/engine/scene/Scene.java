package engine.scene;

import engine.graph.Model;
import engine.graph.TextureCache;

import java.util.*;

public class Scene {

    private Map<String, Model> modelMap;
    private Projection projection;
    private TextureCache textureCache;

    public Scene(int width, int height) {
        this.modelMap = new HashMap<>();
        this.projection = new Projection(width, height);
        this.textureCache = new TextureCache();
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
        this.modelMap.put(model.getId(), model);
    }

    public Map<String, Model> getModelMap() {
        return this.modelMap;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public TextureCache getTextureCache() {
        return this.textureCache;
    }

    public void resize(int width, int height) {
        this.projection.updateProjMatrix(width, height);
    }

    public void cleanup() {
        this.modelMap.values().forEach((model) -> {
            model.cleanup();
        });
    }

}