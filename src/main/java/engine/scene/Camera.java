package engine.scene;

import org.joml.*;

public class Camera {
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector2f rotation = new Vector2f(0, 0);
    private Matrix4f viewMatrix = new Matrix4f();

    // computed direction vectors private
    Vector3f forward = new Vector3f();
    private Vector3f right = new Vector3f();
    private Vector3f up = new Vector3f(0, 1, 0);

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalc();
    }

    public void setRotation(float pitch, float yaw) {
        rotation.set(pitch, yaw);
        clampRotation();
        recalc();
    }

    public void addRotation(float dp, float dy) {
        rotation.add(dp, dy);
        clampRotation();
        recalc();
    }

    private void clampRotation() {
        if (rotation.x > java.lang.Math.toRadians(89)) {
            rotation.x = (float) java.lang.Math.toRadians(89);
        }
        if (rotation.x < java.lang.Math.toRadians(-89)) {
            rotation.x = (float) java.lang.Math.toRadians(-89);
        }
    }

    private void recalc() {

        // Compute forward vector from pitch + yaw
        forward.set((float) (java.lang.Math.cos(rotation.x) * java.lang.Math.sin(rotation.y)),
                (float) java.lang.Math.sin(-rotation.x),
                (float) (java.lang.Math.cos(rotation.x) * java.lang.Math.cos(rotation.y))).normalize();

        // Compute right vector
        right.set(forward.z, 0, -forward.x).normalize();

        // Up is recalculated as cross(forward, right)
        up.set(right).cross(forward).normalize();

        // Build view matrix = lookAt
        viewMatrix.identity().lookAt(position, new Vector3f(position).add(forward), up);
    }

    public void moveForward(float amt) {
        position.add(forward.x * amt, forward.y * amt, forward.z * amt);
        recalc();
    }

    public void moveLeft(float amt) {
        position.sub(right.x * amt, right.y * amt, right.z * amt);
        recalc();
    }

    public void moveRight(float amt) {
        position.add(right.x * amt, right.y * amt, right.z * amt);
        recalc();
    }

    public void moveUp(float amt) {
        position.add(0, amt, 0);
        recalc();
    }
}