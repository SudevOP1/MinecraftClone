package engine.world;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
import engine.block.Block;
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
    public Map<Vector2s, Chunk> generatedChunks;
    public Map<Vector2s, Chunk> chunks;
    public Camera camera;
    private Scene scene;
    private Inventory inventory;

    private long blockBreakingStartTime = 0;
    private long lastBlockBreakTime = 0;
    private Vector3s breakingTargetBlock;
    private engine.block.Block[] destroyOverlays = new engine.block.Block[10];
    private Vector3s targetBlock;
    private Vector3s coordsToPlaceBlock;

    private boolean showDebug = false;
    private boolean breakingBlock = false;
    private boolean f2Pressed = false;
    private boolean f3Pressed = false;
    private boolean f4Pressed = false;

    public World(int seed, String name) {
        this.generatedChunks = new HashMap<>();
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

        this.updateChunks(true);

        // set camera to topmost non-air block in the spawn chunk
        Chunk spawnChunk = this.generatedChunks.get(new Vector2s(0, 0));
        Vector3f spawnPos = spawnChunk.getSpawnableBlockCoords();
        this.camera.setPosition(spawnPos);

        // generate overlay block render objects for block breaking animation
        for (int i = 0; i < 10; i++) {
            this.destroyOverlays[i] = new Block(
                    scene,
                    BlockRegistry.get("destroy_stage_" + i),
                    (short) 0,
                    (short) -1000,
                    (short) 0,
                    pos -> null);
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

        // Looking around using mouse
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        this.camera.addRotation(
                -(float) java.lang.Math.toRadians(displVec.x * Settings.MOUSE_SENSITIVITY),
                -(float) java.lang.Math.toRadians(displVec.y * Settings.MOUSE_SENSITIVITY),
                0);
        this.calculateTargetBlock();
        this.scene.setTargetBlock(this.targetBlock);

        // Hide all overlays by default
        for (int i = 0; i < 10; i++) {
            if (this.destroyOverlays[i] != null) {
                this.destroyOverlays[i].setPosition((short) 0, (short) -1000, (short) 0);
            }
        }

        // Scroll wheel to change selected slot
        float scroll = mouseInput.getScrollDelta();
        if (scroll != 0) {
            int currentSlot = this.getInventory().getSelectedSlot();
            int nextSlot = (currentSlot + (scroll > 0 ? -1 : 1)) % Settings.HOTBAR_CELL_COUNT;
            if (nextSlot < 0) {
                nextSlot += Settings.HOTBAR_CELL_COUNT;
            }
            this.getInventory().setSelectedSlot(nextSlot);
        }

        // Block breaking
        long timeSinceLastBreak = System.currentTimeMillis() - this.lastBlockBreakTime;
        if (this.gameMode.canBreakBlocks() && mouseInput.isLeftButtonPressed() && this.targetBlock != null) {
            // Check cooldown only for creative mode (instant breaking) to prevent
            // accidental chain-breaks
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
                engine.block.BlockType breakingBlockType = getBlockAt(
                        this.breakingTargetBlock.x,
                        this.breakingTargetBlock.y,
                        this.breakingTargetBlock.z);
                if (breakingBlockType != null && breakingBlockType.hardness >= 0) {
                    long elapsed = System.currentTimeMillis() - this.blockBreakingStartTime;
                    float totalTime = breakingBlockType.hardness * 1000f;

                    if (elapsed >= totalTime) {
                        this.breakBlock(this.breakingTargetBlock);
                        this.breakingBlock = false;
                    } else {
                        // Show overlay
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
                                (short) this.breakingTargetBlock.z);
                        // Manually overriding the entity position to add the sub-block offset
                        this.destroyOverlays[stage].getEntity().setPosition(
                                this.breakingTargetBlock.x + offset,
                                this.breakingTargetBlock.y + offset,
                                this.breakingTargetBlock.z + offset);
                        this.destroyOverlays[stage].getEntity().updateModelMatrix();
                    }
                }
            }
        } else {
            this.breakingBlock = false;
            this.breakingTargetBlock = null;
        }

        // Block placing
        if (this.gameMode.canPlaceBlocks()
                && mouseInput.isRightButtonPressed()
                && this.targetBlock != null
                && this.inventory.getSelectedItemType() != null) {

            // Place Block
            BlockType blockType = this.inventory.getSelectedBlockType();

            if (this.coordsToPlaceBlock != null && blockType != null) {
                this.placeBlock(this.coordsToPlaceBlock, blockType);
                this.rebuildChunksAround(this.coordsToPlaceBlock);

                // Decrement item count if survival
                if (this.gameMode == GameMode.SURVIVAL) {
                    this.inventory.decrementItemCount(this.inventory.getSelectedSlot());
                }
            }
        }

    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        this.updateChunks(false);
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
        if (y < 0 || y >= Settings.CHUNK_HEIGHT) {
            return null;
        }

        int chunkX = Math.floorDiv(x, Settings.CHUNK_WIDTH);
        int chunkZ = Math.floorDiv(z, Settings.CHUNK_WIDTH);
        Chunk chunk = this.generatedChunks.get(new Vector2s(chunkX, chunkZ));

        if (chunk == null) {
            return null;
        }

        return chunk.getBlockType(
                Math.floorMod(x, Settings.CHUNK_WIDTH),
                y,
                Math.floorMod(z, Settings.CHUNK_WIDTH));
    }

    public void breakBlock(Vector3s blockCoords) {
        if (!this.setBlockAt(blockCoords, null)) {
            return;
        }

        this.lastBlockBreakTime = System.currentTimeMillis();

        // Rebuild the affected chunk meshes for updated face culling
        this.rebuildChunksAround(blockCoords);
    }

    public void placeBlock(Vector3s blockCoords, BlockType blockType) {
        this.setBlockAt(blockCoords, blockType);
    }

    // Writes a block into its owning chunk. Returns false if the chunk isn't
    // generated or the coordinate is outside the world's vertical range.
    private boolean setBlockAt(Vector3s blockCoords, BlockType blockType) {
        if (blockCoords.y < 0 || blockCoords.y >= Settings.CHUNK_HEIGHT) {
            return false;
        }

        int chunkX = Math.floorDiv(blockCoords.x, Settings.CHUNK_WIDTH);
        int chunkZ = Math.floorDiv(blockCoords.z, Settings.CHUNK_WIDTH);
        Chunk chunk = this.generatedChunks.get(new Vector2s(chunkX, chunkZ));

        if (chunk == null) {
            return false;
        }

        chunk.setBlockType(
                Math.floorMod(blockCoords.x, Settings.CHUNK_WIDTH),
                blockCoords.y,
                Math.floorMod(blockCoords.z, Settings.CHUNK_WIDTH),
                blockType);
        return true;
    }

    // Uses DDA Voxel Traversal (Digital Differential Analyzer) to find the target
    // block
    private void calculateTargetBlock() {
        Vector3f origin = new Vector3f(this.camera.getPosition());
        Vector3f dir = this.camera.getForward();

        // Current (camera) voxel coordinates
        int bx = (int) Math.floor(origin.x);
        int by = (int) Math.floor(origin.y);
        int bz = (int) Math.floor(origin.z);

        // Direction to step in each axis
        int stepX = dir.x >= 0 ? 1 : -1;
        int stepY = dir.y >= 0 ? 1 : -1;
        int stepZ = dir.z >= 0 ? 1 : -1;

        // How far along the ray (in t) to travel for one full voxel in each axis
        float tDeltaX = (dir.x == 0) ? Float.MAX_VALUE : Math.abs(1.0f / dir.x);
        float tDeltaY = (dir.y == 0) ? Float.MAX_VALUE : Math.abs(1.0f / dir.y);
        float tDeltaZ = (dir.z == 0) ? Float.MAX_VALUE : Math.abs(1.0f / dir.z);

        // Distance from origin to the first voxel boundary in each axis
        float tMaxX = (dir.x == 0) ? Float.MAX_VALUE : ((dir.x > 0 ? (bx + 1 - origin.x) : (origin.x - bx)) * tDeltaX);
        float tMaxY = (dir.y == 0) ? Float.MAX_VALUE : ((dir.y > 0 ? (by + 1 - origin.y) : (origin.y - by)) * tDeltaY);
        float tMaxZ = (dir.z == 0) ? Float.MAX_VALUE : ((dir.z > 0 ? (bz + 1 - origin.z) : (origin.z - bz)) * tDeltaZ);

        // Track the last empty (air) block position along the ray
        Vector3s lastEmpty = null;

        // Check the starting block first
        if (getBlockAt(bx, by, bz) != null) {
            this.targetBlock = new Vector3s(bx, by, bz);
            this.coordsToPlaceBlock = null;
            return;
        } else {
            lastEmpty = new Vector3s(bx, by, bz);
        }

        // DDA traversal - always step to the nearest voxel boundary
        float maxReachSq = Settings.MAX_BLOCK_REACH * Settings.MAX_BLOCK_REACH;
        while (true) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    bx += stepX;
                    if (tMaxX * tMaxX > maxReachSq) {
                        break;
                    }
                    tMaxX += tDeltaX;
                } else {
                    bz += stepZ;
                    if (tMaxZ * tMaxZ > maxReachSq) {
                        break;
                    }
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    by += stepY;
                    if (tMaxY * tMaxY > maxReachSq) {
                        break;
                    }
                    tMaxY += tDeltaY;
                } else {
                    bz += stepZ;
                    if (tMaxZ * tMaxZ > maxReachSq) {
                        break;
                    }
                    tMaxZ += tDeltaZ;
                }
            }

            if (getBlockAt(bx, by, bz) != null) {
                this.targetBlock = new Vector3s(bx, by, bz);
                this.coordsToPlaceBlock = lastEmpty;
                return;
            } else {
                lastEmpty = new Vector3s(bx, by, bz);
            }
        }
        this.targetBlock = null;
        this.coordsToPlaceBlock = null;
    }

    // Rebuilds the merged mesh of every chunk a block edit can affect: the chunk
    // owning the block, plus any neighbor chunk it shares a border with.
    private void rebuildChunksAround(Vector3s blockCoords) {
        int chunkX = Math.floorDiv(blockCoords.x, Settings.CHUNK_WIDTH);
        int chunkZ = Math.floorDiv(blockCoords.z, Settings.CHUNK_WIDTH);
        int localX = Math.floorMod(blockCoords.x, Settings.CHUNK_WIDTH);
        int localZ = Math.floorMod(blockCoords.z, Settings.CHUNK_WIDTH);

        this.rebuildChunkModel(chunkX, chunkZ);
        if (localX == 0) {
            this.rebuildChunkModel(chunkX - 1, chunkZ);
        }
        if (localX == Settings.CHUNK_WIDTH - 1) {
            this.rebuildChunkModel(chunkX + 1, chunkZ);
        }
        if (localZ == 0) {
            this.rebuildChunkModel(chunkX, chunkZ - 1);
        }
        if (localZ == Settings.CHUNK_WIDTH - 1) {
            this.rebuildChunkModel(chunkX, chunkZ + 1);
        }
    }

    private void rebuildChunkModel(int chunkX, int chunkZ) {
        Chunk chunk = this.chunks.get(new Vector2s(chunkX, chunkZ));
        if (chunk == null) {
            return; // not currently rendered, it will be meshed on load
        }
        this.buildChunkModel(chunk, chunkX, chunkZ);
    }

    private void buildChunkModel(Chunk chunk, int chunkX, int chunkZ) {
        chunk.buildModel(
                this.scene,
                this.getGeneratedChunk(chunkX - 1, chunkZ),
                this.getGeneratedChunk(chunkX + 1, chunkZ),
                this.getGeneratedChunk(chunkX, chunkZ - 1),
                this.getGeneratedChunk(chunkX, chunkZ + 1));
    }

    private Chunk getGeneratedChunk(int chunkX, int chunkZ) {
        return this.generatedChunks.get(new Vector2s(chunkX, chunkZ));
    }

    // Returns the chunk at these coords with its block data filled in, creating
    // it if needed. Does not touch the scene.
    private Chunk ensureChunkData(int chunkX, int chunkZ) {
        Vector2s coords = new Vector2s(chunkX, chunkZ);
        Chunk chunk = this.generatedChunks.get(coords);
        if (chunk == null) {
            chunk = new Chunk(chunkX, chunkZ, this.seed);
            this.generatedChunks.put(coords, chunk);
        }
        chunk.generateData(this.seed);
        return chunk;
    }

    private void updateChunks(boolean isInit) {
        int playerChunkX = (int) Math.floor(this.camera.getPosition().x / Settings.CHUNK_WIDTH);
        int playerChunkZ = (int) Math.floor(this.camera.getPosition().z / Settings.CHUNK_WIDTH);

        List<Vector2s> neededChunks = new java.util.ArrayList<>();
        Set<Vector2s> neededSet = new java.util.HashSet<>();

        for (int x = playerChunkX - Settings.RENDER_DISTANCE; x <= playerChunkX + Settings.RENDER_DISTANCE; x++) {
            for (int z = playerChunkZ - Settings.RENDER_DISTANCE; z <= playerChunkZ + Settings.RENDER_DISTANCE; z++) {
                Vector2s coords = new Vector2s(x, z);
                neededChunks.add(coords);
                neededSet.add(coords);
            }
        }

        // Unload out-of-range chunks. Hash lookup instead of List.contains, which
        // made this O(chunks * renderDistance^2).
        Iterator<Map.Entry<Vector2s, Chunk>> loaded = this.chunks.entrySet().iterator();
        while (loaded.hasNext()) {
            Map.Entry<Vector2s, Chunk> entry = loaded.next();
            if (!neededSet.contains(entry.getKey())) {
                entry.getValue().removeModel(this.scene);
                loaded.remove();
            }
        }

        neededChunks.sort((a, b) -> {
            int distA = (a.x - playerChunkX) * (a.x - playerChunkX) + (a.y - playerChunkZ) * (a.y - playerChunkZ);
            int distB = (b.x - playerChunkX) * (b.x - playerChunkX) + (b.y - playerChunkZ) * (b.y - playerChunkZ);
            return Integer.compare(distA, distB);
        });

        long startTime = System.nanoTime();
        long maxTime = 10_000_000L; // 10ms

        for (Vector2s chunkCoords : neededChunks) {
            if (this.chunks.containsKey(chunkCoords)) {
                continue;
            }

            Chunk chunk = this.ensureChunkData(chunkCoords.x, chunkCoords.y);

            // Neighbor data must exist before meshing, otherwise every chunk
            // border is meshed as if it faced open air and stays that way.
            this.ensureChunkData(chunkCoords.x - 1, chunkCoords.y);
            this.ensureChunkData(chunkCoords.x + 1, chunkCoords.y);
            this.ensureChunkData(chunkCoords.x, chunkCoords.y - 1);
            this.ensureChunkData(chunkCoords.x, chunkCoords.y + 1);

            this.buildChunkModel(chunk, chunkCoords.x, chunkCoords.y);
            this.chunks.put(chunkCoords, chunk);

            if (!isInit && (System.nanoTime() - startTime > maxTime)) {
                break;
            }
        }
    }

    @Override
    public void cleanup() {
    }

    // Creates and starts the game engine, beginning the game loop.
    public void run() {
        String windowName = "MinecraftClone: " + this.name;
        Engine gameEng = new Engine(windowName, new Window.WindowOptions(), this, Settings.DEFAULT_SPAWN_X,
                Settings.DEFAULT_SPAWN_Y,
                Settings.DEFAULT_SPAWN_Z);
        gameEng.start();
    }

    public void save(String filepath) {
    }

}
