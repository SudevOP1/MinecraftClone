package game;

import engine.Engine;
import engine.Window;
import engine.graph.Render;
import engine.scene.Scene;
import engine.scene.Camera;
import engine.block.Block;
import engine.block.BlockRegistry;
import engine.block.BlockType;
import engine.IAppLogic;
import engine.MouseInput;
import data_structures.Vector3s;

import org.joml.*;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private List<Block> blocks;
    private Map<Vector3s, Block> blockMap;
    private boolean f3Pressed = false;
    private boolean f2Pressed = false;

    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVEMENT_SPEED = 0.005f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("MinecraftClone", new Window.WindowOptions(), main, 0, 2, 0);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        blocks = new ArrayList<>();
        blockMap = new HashMap<>();

        this.initBlocks(scene);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, Render render) {

        float move = diffTimeMillis * MOVEMENT_SPEED;
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
                -(float) java.lang.Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
                -(float) java.lang.Math.toRadians(displVec.y * MOUSE_SENSITIVITY),
                0);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
    }

    @Override
    public void cleanup() {
        // nothing to be done yet
    }

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
                    p -> blockMap.containsKey(p));

            blocks.add(block);
            blockMap.put(pos, block);
        }
    }

    private void initBlocks(Scene scene) {
        Map<Vector3s, BlockType> blocks = new HashMap<>();

        // for (int x = 2; x < 12; x++) {
        // for (int z = 0; z < 10; z++) {
        // blocks.put(new Vector3s(-x, 0, z), BlockRegistry.get("grass_block"));
        // blocks.put(new Vector3s(x, 0, z), BlockRegistry.get("cobblestone"));
        // }
        // }

        String[] blockNames = {
                "grass_block",
                "cobblestone",
                "stone",
                "oak_log",
                "oak_plank",
                "oak_leaves"
        };

        for (int x = 0; x < blockNames.length; x++) {
            for (int z = 0; z < 5; z++) {
                BlockType blockType = BlockRegistry.get(blockNames[x]);
                if (blockType != null) {
                    blocks.put(new Vector3s(2 * x, 0, z), blockType);
                } else {
                    System.err.println("[WARNING] BlockType not found: " + blockNames[x]);
                }
            }
        }

        generateBlocks(scene, blocks);
    }

}
