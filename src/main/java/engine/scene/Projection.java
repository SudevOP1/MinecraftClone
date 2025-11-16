package engine.scene;

import org.joml.Matrix4f;

public class Projection {

    private static final float FOV = (float) Math.toRadians(60.0f); // field of view angle in radians
    private static final float Z_FAR = (float) 1000.0f; // distance to the near plane
    private static final float Z_NEAR = (float) 0.01f; // distance to the far plane

    private Matrix4f projMatrix;

    public Projection(int width, int height) {
        this.projMatrix = new Matrix4f();
        this.updateProjMatrix(width, height);
    }

    public void updateProjMatrix(int width, int height) {
        this.projMatrix.setPerspective(FOV, (float) width / height, Z_NEAR, Z_FAR);
    }

    public Matrix4f getProjMatrix() {
        return this.projMatrix;
    }

}
