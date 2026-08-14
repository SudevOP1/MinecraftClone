package engine.scene;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import engine.world.player.GameMode;
import game.Settings;

public class Camera {

    // Lets the camera ask whether a voxel blocks movement without the scene
    // package having to know how the world stores its blocks.
    @FunctionalInterface
    public interface SolidBlockChecker {
        boolean isSolid(int x, int y, int z);
    }

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

    private boolean isFlying = false;
    private boolean onGround = false;
    private SolidBlockChecker solidBlockChecker;
    private long lastJumpKeyPressNanos = 0;

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

    public void setSolidBlockChecker(SolidBlockChecker solidBlockChecker) {
        this.solidBlockChecker = solidBlockChecker;
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setFlying(boolean isFlying) {
        this.isFlying = isFlying;
        if (isFlying) {
            this.velocity.y = 0;
            this.onGround = false;
        }
    }

    // Called once per rising edge of the jump key. Two presses inside
    // DOUBLE_TAP_MAX_TIME toggle flight, the same way creative mode does.
    public void onJumpKeyPressed(boolean canToggleFly) {
        long now = System.nanoTime();
        if (canToggleFly
                && this.lastJumpKeyPressNanos != 0
                && (now - this.lastJumpKeyPressNanos) / 1_000_000_000.0f < Settings.DOUBLE_TAP_MAX_TIME) {
            this.setFlying(!this.isFlying);
            this.lastJumpKeyPressNanos = 0;
            return;
        }
        this.lastJumpKeyPressNanos = now;
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

    // Turns this frame's movement request into velocity, then integrates it into a
    // position, resolving collisions on the way. deltaTime is in seconds, so
    // movement is frame rate independent.
    public void updateMovement(GameMode gameMode, float deltaTime) {
        if (deltaTime > Settings.MAX_DELTA_TIME) {
            deltaTime = Settings.MAX_DELTA_TIME;
        }
        if (deltaTime <= 0) {
            this.moveInput.zero();
            return;
        }

        // Spectators always fly and pass through blocks, survival players never fly.
        // Only creative mode gets to choose.
        if (gameMode == GameMode.SPECTATOR) {
            this.isFlying = true;
        } else if (gameMode == GameMode.SURVIVAL) {
            this.isFlying = false;
        }
        boolean collide = gameMode == GameMode.SURVIVAL || gameMode == GameMode.CREATIVE;

        boolean jumping = this.moveInput.y > 0;
        boolean sneaking = this.moveInput.y < 0;
        this.buildRequestedDirection();
        this.moveInput.zero();

        if (this.isFlying) {
            this.applyFlyingPhysics(deltaTime);
        } else {
            this.applyWalkingPhysics(deltaTime, jumping, sneaking);
        }

        // Stop drifting forever once the velocity is small enough to be invisible
        if (this.velocity.lengthSquared() < 1e-6f) {
            this.velocity.zero();
            return;
        }

        this.applyMovement(deltaTime, collide);
        this.recalc();
    }

    // Builds the requested direction in world space. While flying it follows the
    // full look direction; while walking the look direction is flattened, so
    // staring at the sky does not slow the player down, and up/down comes from
    // jumping instead.
    private void buildRequestedDirection() {
        this.rquestedDir.zero();

        if (this.isFlying) {
            if (this.moveInput.z != 0) {
                this.rquestedDir.fma(this.moveInput.z, this.forward);
            }
            if (this.moveInput.x != 0) {
                this.rquestedDir.fma(this.moveInput.x, this.right);
            }
            this.rquestedDir.y += this.moveInput.y;
        } else {
            float flatLength = (float) java.lang.Math
                    .sqrt(this.forward.x * this.forward.x + this.forward.z * this.forward.z);
            if (this.moveInput.z != 0 && flatLength > 1e-6f) {
                this.rquestedDir.x += this.moveInput.z * this.forward.x / flatLength;
                this.rquestedDir.z += this.moveInput.z * this.forward.z / flatLength;
            }
            if (this.moveInput.x != 0) {
                this.rquestedDir.x += this.moveInput.x * this.right.x;
                this.rquestedDir.z += this.moveInput.x * this.right.z;
            }
        }

        if (this.rquestedDir.lengthSquared() > 0) {
            this.rquestedDir.normalize();
        }
    }

    // Accelerate, then bleed off speed exponentially so the decay rate does not
    // depend on how many frames were rendered this second.
    private void applyFlyingPhysics(float deltaTime) {
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
    }

    // Horizontal movement is accelerated and damped, vertical movement is a jump
    // impulse plus constant gravity. Air control is deliberately weak so a jump
    // mostly keeps the momentum it started with.
    private void applyWalkingPhysics(float deltaTime, boolean jumping, boolean sneaking) {
        float acceleration = this.onGround
                ? Settings.WALK_ACCELERATION
                : Settings.WALK_ACCELERATION * Settings.AIR_CONTROL;
        this.velocity.x += this.rquestedDir.x * acceleration * deltaTime;
        this.velocity.z += this.rquestedDir.z * acceleration * deltaTime;

        float damping = this.onGround ? Settings.GROUND_DAMPING : Settings.AIR_DAMPING;
        float decay = (float) java.lang.Math.pow(damping, deltaTime);
        this.velocity.x *= decay;
        this.velocity.z *= decay;

        float maxSpeed = sneaking
                ? Settings.MAX_WALK_SPEED * Settings.SNEAK_SPEED_MULTIPLIER
                : Settings.MAX_WALK_SPEED;
        float horizontalSpeed = (float) java.lang.Math
                .sqrt(this.velocity.x * this.velocity.x + this.velocity.z * this.velocity.z);
        if (horizontalSpeed > maxSpeed) {
            float scale = maxSpeed / horizontalSpeed;
            this.velocity.x *= scale;
            this.velocity.z *= scale;
        }

        if (jumping && this.onGround) {
            this.velocity.y = Settings.JUMP_VELOCITY;
            this.onGround = false;
        }

        this.velocity.y -= Settings.GRAVITY * deltaTime;
        if (this.velocity.y < -Settings.TERMINAL_VELOCITY) {
            this.velocity.y = -Settings.TERMINAL_VELOCITY;
        }
    }

    // Moves one axis at a time so that sliding along a wall keeps the movement
    // along the other two axes instead of cancelling the whole step.
    private void applyMovement(float deltaTime, boolean collide) {
        float dx = this.velocity.x * deltaTime;
        float dy = this.velocity.y * deltaTime;
        float dz = this.velocity.z * deltaTime;

        if (!collide || this.solidBlockChecker == null) {
            this.position.add(dx, dy, dz);
            this.onGround = false;
            return;
        }

        this.onGround = false;
        this.moveY(dy);
        this.moveX(dx);
        this.moveZ(dz);
    }

    private void moveY(float amount) {
        if (amount == 0) {
            return;
        }
        this.position.y += amount;
        if (!this.collidesAtCurrentPosition()) {
            return;
        }

        if (amount > 0) {
            // Head hit a ceiling: put the top of the player just below that block.
            int blockY = (int) java.lang.Math.floor(this.getFeetY() + Settings.PLAYER_HEIGHT);
            this.position.y = blockY - Settings.PLAYER_HEIGHT + Settings.PLAYER_EYE_HEIGHT
                    - Settings.COLLISION_EPSILON;
        } else {
            // Feet hit a floor: stand on top of that block.
            int blockY = (int) java.lang.Math.floor(this.getFeetY());
            this.position.y = blockY + 1 + Settings.PLAYER_EYE_HEIGHT + Settings.COLLISION_EPSILON;
            this.onGround = true;
            // Touching down cancels creative flight, like vanilla does
            this.isFlying = false;
        }
        this.velocity.y = 0;
    }

    private void moveX(float amount) {
        if (amount == 0) {
            return;
        }
        this.position.x += amount;
        if (!this.collidesAtCurrentPosition()) {
            return;
        }

        float halfWidth = Settings.PLAYER_WIDTH / 2f;
        if (amount > 0) {
            int blockX = (int) java.lang.Math.floor(this.position.x + halfWidth);
            this.position.x = blockX - halfWidth - Settings.COLLISION_EPSILON;
        } else {
            int blockX = (int) java.lang.Math.floor(this.position.x - halfWidth);
            this.position.x = blockX + 1 + halfWidth + Settings.COLLISION_EPSILON;
        }
        this.velocity.x = 0;
    }

    private void moveZ(float amount) {
        if (amount == 0) {
            return;
        }
        this.position.z += amount;
        if (!this.collidesAtCurrentPosition()) {
            return;
        }

        float halfWidth = Settings.PLAYER_WIDTH / 2f;
        if (amount > 0) {
            int blockZ = (int) java.lang.Math.floor(this.position.z + halfWidth);
            this.position.z = blockZ - halfWidth - Settings.COLLISION_EPSILON;
        } else {
            int blockZ = (int) java.lang.Math.floor(this.position.z - halfWidth);
            this.position.z = blockZ + 1 + halfWidth + Settings.COLLISION_EPSILON;
        }
        this.velocity.z = 0;
    }

    // Returns true if the player's bounding box overlaps the voxel at these
    // coordinates.
    public boolean intersectsBlock(int x, int y, int z) {
        float halfWidth = Settings.PLAYER_WIDTH / 2f;
        float feetY = this.getFeetY();

        return this.position.x - halfWidth < x + 1
                && this.position.x + halfWidth > x
                && feetY < y + 1
                && feetY + Settings.PLAYER_HEIGHT > y
                && this.position.z - halfWidth < z + 1
                && this.position.z + halfWidth > z;
    }

    private float getFeetY() {
        return this.position.y - Settings.PLAYER_EYE_HEIGHT;
    }

    // True if the player's axis aligned bounding box overlaps any solid block.
    // The ceil(max) - 1 bounds keep a box that ends exactly on a block boundary
    // from picking up the block on the far side of it.
    private boolean collidesAtCurrentPosition() {
        if (this.solidBlockChecker == null) {
            return false;
        }

        float halfWidth = Settings.PLAYER_WIDTH / 2f;
        float feetY = this.getFeetY();

        int minX = (int) java.lang.Math.floor(this.position.x - halfWidth);
        int maxX = (int) java.lang.Math.ceil(this.position.x + halfWidth) - 1;
        int minY = (int) java.lang.Math.floor(feetY);
        int maxY = (int) java.lang.Math.ceil(feetY + Settings.PLAYER_HEIGHT) - 1;
        int minZ = (int) java.lang.Math.floor(this.position.z - halfWidth);
        int maxZ = (int) java.lang.Math.ceil(this.position.z + halfWidth) - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (this.solidBlockChecker.isSolid(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
