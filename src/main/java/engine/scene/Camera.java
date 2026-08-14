package engine.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import game.Settings;

public class Camera {

    private Vector3f position;
    private Vector3f rotation;
    private Vector3f velocity = new Vector3f();
    private Matrix4f viewMatrix = new Matrix4f();

    private Vector3f moveInput = new Vector3f();
    private Vector3f rquestedDir = new Vector3f();

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

    public Vector3f getForward() {
        return this.forward;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.recalc();
    }

    public void setPosition(Vector3f pos) {
        this.setPosition(pos.x, pos.y, pos.z);
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

    public Vector3f getVelocity() {
        return this.velocity;
    }

    // The move methods only record what the player asked for this frame.
    // updateMovement() turns that request into acceleration and moves the camera.

    public void moveForward() {
        this.moveInput.z += 1;
    }

    public void moveBackward() {
        this.moveInput.z -= 1;
    }

    public void moveLeft() {
        this.moveInput.x -= 1;
    }

    public void moveRight() {
        this.moveInput.x += 1;
    }

    public void moveUp() {
        this.moveInput.y += 1;
    }

    public void moveDown() {
        this.moveInput.y -= 1;
    }

    // Accelerates towards the requested direction, applies drag, then integrates
    // the position. deltaTime is in seconds, so movement is frame rate independent.
    public void updateMovement(float deltaTime) {
        if (deltaTime > Settings.MAX_DELTA_TIME) {
            deltaTime = Settings.MAX_DELTA_TIME;
        }
        if (deltaTime <= 0) {
            this.moveInput.zero();
            return;
        }

        // Build the requested direction in world space. Forward/right follow where the
        // camera is looking, up/down are always along the world Y axis.
        this.rquestedDir.zero();
        if (this.moveInput.z != 0) {
            this.rquestedDir.fma(this.moveInput.z, this.forward);
        }
        if (this.moveInput.x != 0) {
            this.rquestedDir.fma(this.moveInput.x, this.right);
        }
        if (this.moveInput.y != 0) {
            this.rquestedDir.y += this.moveInput.y;
        }
        if (this.rquestedDir.lengthSquared() > 0) {
            this.rquestedDir.normalize();
        }
        this.moveInput.zero();

        // Accelerate, then bleed off speed exponentially so the decay rate does not
        // depend on how many frames were rendered this second.
        this.velocity.fma(Settings.MOVE_ACCELERATION * deltaTime, this.rquestedDir);
        this.velocity.mul((float) java.lang.Math.pow(Settings.MOVE_DAMPING, deltaTime));

        // Clamp horizontal and vertical speed separately, like creative flight does
        float horizontalSpeed = (float) java.lang.Math
                .sqrt(this.velocity.x * this.velocity.x + this.velocity.z * this.velocity.z);
        if (horizontalSpeed > Settings.MAX_HORIZONTAL_SPEED) {
            float scale = Settings.MAX_HORIZONTAL_SPEED / horizontalSpeed;
            this.velocity.x *= scale;
            this.velocity.z *= scale;
        }
        if (this.velocity.y > Settings.MAX_VERTICAL_SPEED) {
            this.velocity.y = Settings.MAX_VERTICAL_SPEED;
        }
        if (this.velocity.y < -Settings.MAX_VERTICAL_SPEED) {
            this.velocity.y = -Settings.MAX_VERTICAL_SPEED;
        }

        // Stop drifting forever once the velocity is small enough to be invisible
        if (this.velocity.lengthSquared() < 1e-6f) {
            this.velocity.zero();
            return;
        }

        this.position.fma(deltaTime, this.velocity);
        this.recalc();
    }

}
