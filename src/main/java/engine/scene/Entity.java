package engine.scene;

import org.joml.*;

public class Entity {

    private final String id;
    private final String modelId;
    private Matrix4f modelMatrix;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;

    public Entity(String id, String modelId) {
        this.id = id;
        this.modelId = modelId;
        this.modelMatrix = new Matrix4f();
        this.position = new Vector3f();
        this.rotation = new Quaternionf();
        this.scale = 1;
    }

    public String getId() {
        return this.id;
    }

    public String getModelId() {
        return this.modelId;
    }

    public Matrix4f getModelMatrix() {
        return this.modelMatrix;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Quaternionf getRotation() {
        return this.rotation;
    }

    public float getScale() {
        return this.scale;
    }

    public final void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setRotation(float x, float y, float z, float angle) {
        this.rotation.fromAxisAngleRad(x, y, z, angle);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void updateModelMatrix() {
        this.modelMatrix.translationRotateScale(position, rotation, scale);
    }
}