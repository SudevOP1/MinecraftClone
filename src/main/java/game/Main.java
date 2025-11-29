package game;

import engine.Engine;
import engine.Window;
import engine.graph.Render;
import engine.scene.Scene;
import engine.scene.Camera;
import engine.block.Block;
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
    public static final float MOVEMENT_SPEED = 0.002f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("MinecraftClone", new Window.WindowOptions(), main, 0, 2, 0);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        blocks = new ArrayList<>();
        blockMap = new HashMap<>();

        // First pass: register all block positions
        List<Vector3s> positions = new ArrayList<>();
        for (short i = 0; i < 10; i++) {
            for (short j = 0; j < 10; j++) {
                for (short k = -9; k < 1; k++) {
                    Vector3s pos = new Vector3s(i, k, j);
                    positions.add(pos);
                    blockMap.put(pos, null); // Reserve the position
                }
            }
        }

        // Second pass: create blocks with neighbor checking
        for (Vector3s pos : positions) {
            Block block = new Block(scene, "models/grass_block.png", pos.x, pos.y, pos.z,
                    p -> blockMap.containsKey(p));
            blocks.add(block);
            blockMap.put(pos, block); // Update with actual block
        }
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

}
