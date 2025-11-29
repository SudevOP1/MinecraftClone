package engine.scene;

import org.joml.*;

public class Camera {
    private Vector3f position;
    private Vector3f rotation;
    private Matrix4f viewMatrix = new Matrix4f();

    // computed direction vectors
    private Vector3f forward = new Vector3f();
    private Vector3f right = new Vector3f();
    private Vector3f up = new Vector3f(0, 1, 0);

    public Camera() {
        this(0, 0, 0);
    }

    public Camera(float x, float y, float z) {
        this(x, y, z, 0, 0, (float) java.lang.Math.PI);
    }

    public Camera(float x, float y, float z, float pitch, float yaw, float roll) {
        this.position = new Vector3f(x, y, z);
        this.rotation = new Vector3f(pitch, yaw, roll);
        this.recalc();
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Matrix4f getViewMatrix() {
        return this.viewMatrix;
    }

    public Vector3f getRotation() {
        return this.rotation;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.recalc();
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.rotation.set(pitch, yaw, roll);
        this.clampRotation();
        this.recalc();
    }

    public void addRotation(float dp, float dy, float dr) {
        this.rotation.add(dp, dy, dr);
        this.clampRotation();
        this.recalc();
    }

    private void clampRotation() {
        // Clamp pitch to prevent gimbal lock
        if (this.rotation.x > java.lang.Math.toRadians(89)) {
            this.rotation.x = (float) java.lang.Math.toRadians(89);
        }
        if (this.rotation.x < java.lang.Math.toRadians(-89)) {
            this.rotation.x = (float) java.lang.Math.toRadians(-89);
        }
    }

    private void recalc() {
        float pitch = this.rotation.x;
        float yaw = this.rotation.y;
        float roll = this.rotation.z;

        // Compute forward vector from pitch + yaw
        this.forward.set(
                (float) (java.lang.Math.cos(pitch) * java.lang.Math.sin(yaw)),
                (float) java.lang.Math.sin(pitch),
                (float) (java.lang.Math.cos(pitch) * java.lang.Math.cos(yaw))).normalize();

        // Compute right vector (perpendicular to forward in XZ plane initially)
        this.right.set(this.forward.z, 0, -this.forward.x).normalize();

        // Compute initial up vector (perpendicular to both forward and right)
        this.up.set(this.right).cross(this.forward).normalize();

        // Apply roll rotation around the forward axis
        if (roll != 0) {
            // Rotate right and up vectors around the forward axis
            float cosRoll = (float) java.lang.Math.cos(roll);
            float sinRoll = (float) java.lang.Math.sin(roll);

            Vector3f tempRight = new Vector3f(this.right);
            Vector3f tempUp = new Vector3f(this.up);

            this.right.set(
                    tempRight.x * cosRoll - tempUp.x * sinRoll,
                    tempRight.y * cosRoll - tempUp.y * sinRoll,
                    tempRight.z * cosRoll - tempUp.z * sinRoll).normalize();

            this.up.set(
                    tempRight.x * sinRoll + tempUp.x * cosRoll,
                    tempRight.y * sinRoll + tempUp.y * cosRoll,
                    tempRight.z * sinRoll + tempUp.z * cosRoll).normalize();
        }

        // Build view matrix = lookAt
        this.viewMatrix.identity().lookAt(
                this.position,
                new Vector3f(this.position).add(this.forward),
                this.up);
    }

    public void moveForward(float amt) {
        this.position.add(this.forward.x * amt, this.forward.y * amt, this.forward.z * amt);
        this.recalc();
    }

    public void moveLeft(float amt) {
        this.position.sub(this.right.x * amt, this.right.y * amt, this.right.z * amt);
        this.recalc();
    }

    public void moveRight(float amt) {
        this.position.add(this.right.x * amt, this.right.y * amt, this.right.z * amt);
        this.recalc();
    }

    public void moveUp(float amt) {
        this.position.add(0, amt, 0);
        this.recalc();
    }
}