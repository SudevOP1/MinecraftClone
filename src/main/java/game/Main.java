package game;

import engine.Engine;
import engine.Window;
import engine.graph.Render;
import engine.scene.Scene;
import engine.scene.Camera;
import engine.block.Block;
import engine.IAppLogic;
import engine.MouseInput;

import org.joml.*;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private List<Block> blocks;

    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVEMENT_SPEED = 0.002f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("MinecraftClone", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        blocks = new ArrayList<>();
        for (short i = 0; i < 10; i++) {
            for (short j = 0; j < 10; j++) {
                blocks.add(new Block(scene, "models/grass_block.png", i, (short) 0, j));
            }
        }
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {

        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();

        // Vector3f pos = camera.getPosition();
        // Vector2f rot = camera.getRotation();
        // System.out.printf("[CAM] x=%f, y=%f, z=%f, rx=%f, ry=%f\n", pos.z, pos.y,
        // pos.z, rot.x, rot.y);

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

        // Mouse look
        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        camera.addRotation(
                -(float) java.lang.Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
                -(float) java.lang.Math.toRadians(displVec.y * MOUSE_SENSITIVITY),
                0);
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        // // rotatig the block
        // rotation += 0.5;
        // if (rotation > 360) {
        // rotation = rotation % 360;
        // }
        // cubeEntity.setRotation(1, 1, 1, (float) java.lang.Math.toRadians(rotation));
        // cubeEntity.updateModelMatrix();
    }

    @Override
    public void cleanup() {
        // nothing to be done yet
    }

}
