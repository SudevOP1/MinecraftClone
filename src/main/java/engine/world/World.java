package engine.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import data_structures.Vector2s;
import data_structures.Vector3s;
import engine.Engine;
import engine.IAppLogic;
import engine.MouseInput;
import engine.Window;
import engine.block.BlockRegistry;
import engine.block.BlockType;
import engine.graph.Render;
import engine.scene.Camera;
import engine.scene.Scene;
import engine.world.player.GameMode;
import engine.world.player.Inventory;
import game.Settings;
import utils.Debug;

public class World implements IAppLogic {

    public String name;
    public int seed;
    private GameMode gameMode = GameMode.CREATIVE;
    public Map<Vector2s, Chunk> chunks;
    public Camera camera;
    private Scene scene;
    private Inventory inventory;

    private long blockBreakingStartTime = 0;
    private long lastBlockBreakTime = 0;
    private Vector3s breakingTargetBlock;
    private engine.block.Block[] destroyOverlays = new engine.block.Block[10];
    private Vector3s targetBlock;

    private boolean showDebug = false;
    private boolean breakingBlock = false;
    private boolean f2Pressed = false;
    private boolean f3Pressed = false;
    private boolean f4Pressed = false;

    public World(int seed, String name) {
        this.chunks = new HashMap<>();
        this.seed = seed;
        this.name = name;
        this.showDebug = Debug.getEnabled();
        this.inventory = new Inventory();
    }

    public World(int seed) {
        this(seed, "New World");
    }

    public World(String name) {
        this(new Random().nextInt(), name);
    }

    public World() {
        this(new Random().nextInt(), "New World");
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        this.scene = scene;
        this.camera = scene.getCamera();

        // init chunks
        for (int x = -Settings.RENDER_DISTANCE; x < Settings.RENDER_DISTANCE; x++) {
            for (int y = -Settings.RENDER_DISTANCE; y < Settings.RENDER_DISTANCE; y++) {
                Vector2s chunkCoords = new Vector2s(x, y);
                Chunk newChunk = new Chunk(x, y, seed);
                this.chunks.put(chunkCoords, newChunk);
            }
        }

        // testing all block types
        String[] blockNames = BlockRegistry.keySet().toArray(new String[0]);
        Chunk chunk = this.chunks.get(new Vector2s(0, 0));

        int blockIdx = 0;
        for (String name : blockNames) {
            BlockType blockType = BlockRegistry.get(name);
            if (blockType.isSolid) {
                // Ensure local coords stay within CHUNK_WIDTH
                short localX = (short) (blockIdx % Settings.CHUNK_WIDTH);
                short localZ = (short) (blockIdx / Settings.CHUNK_WIDTH);
                chunk.placeBlock(localX, (short) 0, localZ, blockType);
                blockIdx++;
            }
        }

        // generate all chunks correctly once
        for (Chunk c : this.chunks.values()) {
            c.generate(scene);
        }
        // generate overlay block render objects for block breaking animation
        for (int i = 0; i < 10; i++) {
            this.destroyOverlays[i] = new engine.block.Block(scene, engine.block.BlockRegistry.get("destroy_stage_" + i), (short) 0, (short) -1000, (short) 0, pos -> null);
            this.destroyOverlays[i].setScale(1.02f);
        }

        // Testing inventory
        this.inventory.setItem(0, "grass_block", 64);
        this.inventory.setItem(1, "dirt_block", 64);
        this.inventory.setItem(2, "cobblestone", 64);
        this.inventory.setItem(3, "stone", 64);
        this.inventory.setItem(4, "oak_log", 64);
        this.inventory.setItem(5, "oak_plank", 64);
        this.inventory.setItem(6, "oak_leaves", 64);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, Render render) {

        float move = diffTimeMillis * Settings.MOVEMENT_SPEED;

        // WASD, space, shift movement
        if (window.isKeyPressed(GLFW_KEY_W)) {
            this.camera.moveForward(move);
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            this.camera.moveForward(-move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            this.camera.moveLeft(move);
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            this.camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            this.camera.moveUp(move);
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            this.camera.moveUp(-move);
        }

        // F2 to take screenshot
        if (window.isKeyPressed(GLFW_KEY_F2)) {
            if (!f2Pressed) {
                render.takeScreenshot(window);
                f2Pressed = true;
            }
        } else {
            f2Pressed = false;
        }

        // F3 to toggle debug display
        if (window.isKeyPressed(GLFW_KEY_F3)) {
            if (!f3Pressed) {
                this.showDebug = !this.showDebug;
                f3Pressed = true;
            }
        } else {
            f3Pressed = false;
        }

        // F4 to toggle wireframe mode
        if (window.isKeyPressed(GLFW_KEY_F4)) {
            if (!this.f4Pressed) {
                render.toggleWireframe();
                this.f4Pressed = true;
            }
        } else {
            this.f4Pressed = false;
        }

        // looking around using mouse
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        this.camera.addRotation(
                -(float) java.lang.Math.toRadians(displVec.x * Settings.MOUSE_SENSITIVITY),
                -(float) java.lang.Math.toRadians(displVec.y * Settings.MOUSE_SENSITIVITY),
                0);
        this.targetBlock = this.calculateTargetBlock();
        this.scene.setTargetBlock(this.targetBlock);

        // hide all overlays by default
        for (int i = 0; i < 10; i++) {
            if (this.destroyOverlays[i] != null) {
                this.destroyOverlays[i].setPosition((short) 0, (short) -1000, (short) 0);
            }
        }

        // scroll wheel to change selected slot
        float scroll = mouseInput.getScrollDelta();
        if (scroll != 0) {
            int currentSlot = this.getInventory().getSelectedSlot();
            int nextSlot = (currentSlot + (scroll > 0 ? -1 : 1)) % Settings.HOTBAR_CELL_COUNT;
            if (nextSlot < 0) {
                nextSlot += Settings.HOTBAR_CELL_COUNT;
            }
            this.getInventory().setSelectedSlot(nextSlot);
        }

        // block breaking
        long timeSinceLastBreak = System.currentTimeMillis() - this.lastBlockBreakTime;
        if (this.gameMode.canBreakBlocks() && mouseInput.isLeftButtonPressed() && this.targetBlock != null) {
            // Check cooldown only for creative mode (instant breaking) to prevent accidental chain-breaks
            if (this.gameMode.canBreakBlocksInstantly() && timeSinceLastBreak <= Settings.BREAK_COOLDOWN_MS) {
                return;
            }

            // reset breaking state if target changed
            if (this.breakingBlock && !this.targetBlock.equals(this.breakingTargetBlock)) {
                this.breakingBlock = false;
            }

            if (!this.breakingBlock) {
                this.breakingBlock = true;
                this.breakingTargetBlock = new Vector3s(this.targetBlock.x, this.targetBlock.y, this.targetBlock.z);

                if (this.gameMode.canBreakBlocksInstantly()) {
                    this.breakBlock(this.targetBlock);
                } else {
                    this.blockBreakingStartTime = System.currentTimeMillis();
                }
            } else if (!this.gameMode.canBreakBlocksInstantly()) {
                engine.block.BlockType breakingBlockType = getBlockAt(this.breakingTargetBlock.x, this.breakingTargetBlock.y, this.breakingTargetBlock.z);
                if (breakingBlockType != null && breakingBlockType.hardness >= 0) {
                    long elapsed = System.currentTimeMillis() - this.blockBreakingStartTime;
                    float totalTime = breakingBlockType.hardness * 1000f;

                    if (elapsed >= totalTime) {
                        this.breakBlock(this.breakingTargetBlock);
                        this.breakingBlock = false;
                    } else {
                        // show overlay
                        int stage = (int) ((elapsed / totalTime) * 10);
                        if (stage < 0) {
                            stage = 0;
                        }
                        if (stage > 9) {
                            stage = 9;
                        }

                        // Offset by -0.01 on all axes to center the 1.02 scaling around the block
                        float offset = -0.01f;
                        this.destroyOverlays[stage].setPosition(
                                (short) this.breakingTargetBlock.x,
                                (short) this.breakingTargetBlock.y,
                                (short) this.breakingTargetBlock.z
                        );
                        // Manually overriding the entity position to add the sub-block offset
                        this.destroyOverlays[stage].getEntity().setPosition(
                                this.breakingTargetBlock.x + offset,
                                this.breakingTargetBlock.y + offset,
                                this.breakingTargetBlock.z + offset
                        );
                        this.destroyOverlays[stage].getEntity().updateModelMatrix();
                    }
                }
            }
        } else {
            this.breakingBlock = false;
            this.breakingTargetBlock = null;
        }

        // block placing
        if (mouseInput.isRightButtonPressed()) {
        }

    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Vector3s getTargetBlock() {
        return this.targetBlock;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public boolean isF3Pressed() {
        return this.showDebug;
    }

    public engine.block.BlockType getBlockAt(int x, int y, int z) {
        int chunkX = (int) Math.floor((double) x / Settings.CHUNK_WIDTH);
        int chunkZ = (int) Math.floor((double) z / Settings.CHUNK_WIDTH);
        Chunk chunk = this.chunks.get(new Vector2s(chunkX, chunkZ));

        if (chunk == null) {
            return null;
        }

        int localX = x - chunkX * Settings.CHUNK_WIDTH;
        int localZ = z - chunkZ * Settings.CHUNK_WIDTH;

        // Ensure Y is within chunk's vertical bounds, and X/Z within chunk dimensions
        if (y < 0 || y >= Settings.CHUNK_WIDTH || localX < 0 || localX >= Settings.CHUNK_WIDTH || localZ < 0 || localZ >= Settings.CHUNK_WIDTH) {
            return null;
        }

        return chunk.getBlockType((short) localX, (short) y, (short) localZ);
    }

    private Vector3s calculateTargetBlock() {
        Vector3f pos = new Vector3f(this.camera.getPosition());
        Vector3f dir = this.camera.getForward();

        float step = 0.1f;
        for (float t = 0; t < Settings.MAX_BLOCK_REACH; t += step) {
            pos.add(dir.x * step, dir.y * step, dir.z * step);

            int bx = (int) Math.floor(pos.x);
            int by = (int) Math.floor(pos.y);
            int bz = (int) Math.floor(pos.z);

            if (getBlockAt(bx, by, bz) != null) {
                return new Vector3s(bx, by, bz);
            }
        }
        return null;
    }

    public void breakBlock(Vector3s blockCoords) {
        int chunkX = (int) Math.floor((double) blockCoords.x / Settings.CHUNK_WIDTH);
        int chunkZ = (int) Math.floor((double) blockCoords.z / Settings.CHUNK_WIDTH);
        Chunk chunk = this.chunks.get(new Vector2s(chunkX, chunkZ));

        if (chunk == null) {
            return;
        }

        int localX = blockCoords.x - chunkX * Settings.CHUNK_WIDTH;
        int localZ = blockCoords.z - chunkZ * Settings.CHUNK_WIDTH;

        chunk.removeBlock(this.scene, (short) localX, (short) blockCoords.y, (short) localZ);
        this.lastBlockBreakTime = System.currentTimeMillis();

        // Regenerate neighboring blocks' meshes for updated face culling
        this.regenerateNeighbors(blockCoords);
    }

    private void regenerateNeighbors(Vector3s blockCoords) {
        int[][] offsets = {{0, 0, 1}, {0, 0, -1}, {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};
        for (int[] off : offsets) {
            int nx = blockCoords.x + off[0];
            int ny = blockCoords.y + off[1];
            int nz = blockCoords.z + off[2];

            int chunkX = (int) Math.floor((double) nx / Settings.CHUNK_WIDTH);
            int chunkZ = (int) Math.floor((double) nz / Settings.CHUNK_WIDTH);
            Chunk chunk = this.chunks.get(new Vector2s(chunkX, chunkZ));
            if (chunk == null) {
                continue;
            }

            short localX = (short) (nx - chunkX * Settings.CHUNK_WIDTH);
            short localZ = (short) (nz - chunkZ * Settings.CHUNK_WIDTH);

            chunk.regenerateBlock(this.scene, localX, (short) ny, localZ, pos -> getBlockAt(pos.x, pos.y, pos.z));
        }
    }

    public void placeBlock(Vector3s blockCoords, BlockType blockType) {
        int chunkX = (int) Math.floor((double) blockCoords.x / Settings.CHUNK_WIDTH);
        int chunkZ = (int) Math.floor((double) blockCoords.z / Settings.CHUNK_WIDTH);
        Chunk chunk = this.chunks.get(new Vector2s(chunkX, chunkZ));

        if (chunk == null) {
            return;
        }

        int localX = blockCoords.x - chunkX * Settings.CHUNK_WIDTH;
        int localZ = blockCoords.z - chunkZ * Settings.CHUNK_WIDTH;

        chunk.placeBlock((short) localX, (short) blockCoords.y, (short) localZ, blockType);
    }

    @Override
    public void cleanup() {
    }

    // Creates and starts the game engine, beginning the game loop.
    public void run() {
        String windowName = "MinecraftClone: " + this.name;
        Engine gameEng = new Engine(windowName, new Window.WindowOptions(), this, Settings.SPAWN_X, Settings.SPAWN_Y, Settings.SPAWN_Z);
        gameEng.start();
    }

    public void save(String filepath) {
    }

}
