package engine.scene;

import java.util.HashMap;
import java.util.Map;

import engine.graph.Model;
import engine.graph.TextureCache;

public class Scene {

    private Map<String, Model> modelMap;
    private Projection projection;
    private TextureCache textureCache;
    private Camera camera;
    private data_structures.Vector3s targetBlock;

    public Scene(int width, int height) {
        this.modelMap = new HashMap<>();
        this.projection = new Projection(width, height);
        this.textureCache = new TextureCache();
        camera = new Camera();
    }

    public Scene(int width, int height, float x, float y, float z) {
        this.modelMap = new HashMap<>();
        this.projection = new Projection(width, height);
        this.textureCache = new TextureCache();
        camera = new Camera(x, y, z);
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

    public void removeEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model != null) {
            model.getEntitiesList().remove(entity);
        }
    }

    public void removeModel(String modelId) {
        Model model = modelMap.remove(modelId);
        if (model != null) {
            model.cleanup();
        }
    }

    public Map<String, Model> getModelMap() {
        return this.modelMap;
    }

    public Projection getProjection() {
        return this.projection;
    }

    public void setTargetBlock(data_structures.Vector3s targetBlock) {
        this.targetBlock = targetBlock;
    }

    public data_structures.Vector3s getTargetBlock() {
        return this.targetBlock;
    }

    public TextureCache getTextureCache() {
        return this.textureCache;
    }

    public Camera getCamera() {
        return this.camera;
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
