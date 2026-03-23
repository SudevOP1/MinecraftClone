package engine.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
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
import engine.block.BlockType;
import engine.graph.Render;
import engine.scene.Camera;
import engine.scene.Scene;
import game.Settings;

public class World implements IAppLogic {

    String name;
    int seed;
    Map<Vector2s, Chunk> chunks;

    private List<Block> blocks;
    private Map<Vector3s, Block> blockMap;
    private boolean f3Pressed = false;
    private boolean f2Pressed = false;

    public World(int seed, String name) {
        this.chunks = new HashMap<>();
        this.seed = seed;
        this.name = name;
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
        blocks = new ArrayList<>();
        blockMap = new HashMap<>();

        // init chunks
        for (int x = -Settings.RENDER_DISTANCE; x < Settings.RENDER_DISTANCE; x++) {
            for (int y = -Settings.RENDER_DISTANCE; y < Settings.RENDER_DISTANCE; y++) {
                Vector2s chunkCoords = new Vector2s(x, y);
                Chunk newChunk = new Chunk(x, y, seed);
                this.chunks.put(chunkCoords, newChunk);
            }
        }

        // generate block render objects from chunk data
        for (Chunk chunk : this.chunks.values()) {
            this.generateBlocks(scene, chunk.getBlockTypes());
        }
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, Render render) {

        float move = diffTimeMillis * Settings.MOVEMENT_SPEED;
        Camera camera = scene.getCamera();

        // WASD, space, shift movement
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        }
        if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveForward(-move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        }
        if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(move);
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            camera.moveUp(-move);
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

        // F3 to toggle wireframe mode
        if (window.isKeyPressed(GLFW_KEY_F3)) {
            if (!f3Pressed) {
                render.toggleWireframe();
                f3Pressed = true;
            }
        } else {
            f3Pressed = false;
        }

        // looking around using mouse
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        camera.addRotation(
                -(float) java.lang.Math.toRadians(displVec.x * Settings.MOUSE_SENSITIVITY),
                -(float) java.lang.Math.toRadians(displVec.y * Settings.MOUSE_SENSITIVITY),
                0);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
    }

    @Override
    public void cleanup() {
    }

    // Creates Block render objects from a block-type map, performing face culling.
    private void generateBlocks(Scene scene, Map<Vector3s, BlockType> blockTypes) {

        // Reserve all positions first (for neighbor checks)
        for (Vector3s pos : blockTypes.keySet()) {
            blockMap.put(pos, null);
        }

        // Create blocks using their respective BlockTypes
        for (Map.Entry<Vector3s, BlockType> entry : blockTypes.entrySet()) {
            Vector3s pos = entry.getKey();
            BlockType type = entry.getValue();

            Block block = new Block(
                    scene,
                    type,
                    pos.x,
                    pos.y,
                    pos.z,
                    p -> blockTypes.get(p));

            blocks.add(block);
            blockMap.put(pos, block);
        }
    }

    // Creates and starts the game engine, beginning the game loop.
    public void run() {
        String windowName = "MinecraftClone: " + this.name;
        Engine gameEng = new Engine(windowName, new Window.WindowOptions(), this, 0, 2, 0);
        gameEng.start();
    }

    public void save(String filepath) {
    }

}
