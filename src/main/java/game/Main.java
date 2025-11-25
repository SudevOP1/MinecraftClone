package game;

import engine.Engine;
import engine.Window;
import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.Model;
import engine.graph.Render;
import engine.graph.Texture;
import engine.scene.Scene;
import engine.scene.Camera;
import engine.scene.Entity;
import engine.IAppLogic;
import engine.MouseInput;

import java.util.*;

import org.joml.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private Entity cubeEntity;
    private Vector4f displayInc = new Vector4f();
    private float rotation;

    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVEMENT_SPEED = 0.002f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("MinecraftClone", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        float[] positions = new float[] {
                // V0
                -0.5f, 0.5f, 0.5f,
                // V1
                -0.5f, -0.5f, 0.5f,
                // V2
                0.5f, -0.5f, 0.5f,
                // V3
                0.5f, 0.5f, 0.5f,
                // V4
                -0.5f, 0.5f, -0.5f,
                // V5
                0.5f, 0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,

                // For text coords in top face
                // V8: V4 repeated
                -0.5f, 0.5f, -0.5f,
                // V9: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V10: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V11: V3 repeated
                0.5f, 0.5f, 0.5f,

                // For text coords in right face
                // V12: V3 repeated
                0.5f, 0.5f, 0.5f,
                // V13: V2 repeated
                0.5f, -0.5f, 0.5f,

                // For text coords in left face
                // V14: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V15: V1 repeated
                -0.5f, -0.5f, 0.5f,

                // For text coords in bottom face
                // V16: V6 repeated
                -0.5f, -0.5f, -0.5f,
                // V17: V7 repeated
                0.5f, -0.5f, -0.5f,
                // V18: V1 repeated
                -0.5f, -0.5f, 0.5f,
                // V19: V2 repeated
                0.5f, -0.5f, 0.5f,
        };
        float[] textCoords = new float[] {
                0.0f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.5f, 0.0f,

                0.0f, 0.0f,
                0.5f, 0.0f,
                0.0f, 0.5f,
                0.5f, 0.5f,

                // For text coords in top face
                0.0f, 0.5f,
                0.5f, 0.5f,
                0.0f, 1.0f,
                0.5f, 1.0f,

                // For text coords in right face
                0.0f, 0.0f,
                0.0f, 0.5f,

                // For text coords in left face
                0.5f, 0.0f,
                0.5f, 0.5f,

                // For text coords in bottom face
                0.5f, 0.0f,
                1.0f, 0.0f,
                0.5f, 0.5f,
                1.0f, 0.5f,
        };
        int[] indices = new int[] {
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                8, 10, 11, 9, 8, 11,
                // Right face
                12, 13, 7, 5, 12, 7,
                // Left face
                14, 15, 6, 4, 14, 6,
                // Bottom face
                16, 18, 19, 17, 16, 19,
                // Back face
                4, 6, 7, 5, 4, 7,
        };

        Texture texture = scene.getTextureCache().createTexture("models/grass_block.png");
        Material material = new Material();
        material.setTexturePath(texture.getTexturePath());
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        Mesh mesh = new Mesh(positions, textCoords, indices);
        material.getMeshList().add(mesh);
        Model cubeModel = new Model("cube-model", materialList);
        scene.addModel(cubeModel);

        cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(0, 0, -2);
        scene.addEntity(cubeEntity);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {

        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
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

        MouseInput mouseInput = window.getMouseInput();
        Vector2f displVec = mouseInput.getDisplVec();
        camera.addRotation((float) java.lang.Math.toRadians(displVec.x * MOUSE_SENSITIVITY),
                (float) -java.lang.Math.toRadians(displVec.y * MOUSE_SENSITIVITY));

        displayInc.zero();
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            displayInc.y += 1;
        }
        if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            displayInc.y += -1;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT)) {
            displayInc.x += -1;
        }
        if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
            displayInc.x += 1;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            displayInc.z += -1;
        }
        if (window.isKeyPressed(GLFW_KEY_Q)) {
            displayInc.z += 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Z)) {
            displayInc.w += -1;
        }
        if (window.isKeyPressed(GLFW_KEY_X)) {
            displayInc.w += 1;
        }
        displayInc.mul(diffTimeMillis / 1000.0f);

        Vector3f entityPos = cubeEntity.getPosition();
        cubeEntity.setPosition(displayInc.x + entityPos.x, displayInc.y +
                entityPos.y, displayInc.z + entityPos.z);
        cubeEntity.setScale(cubeEntity.getScale() + displayInc.w);
        cubeEntity.updateModelMatrix();

    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        rotation += 0.5;
        if (rotation > 360) {
            rotation = rotation % 360;
        }
        cubeEntity.setRotation(1, 1, 1, (float) java.lang.Math.toRadians(rotation));
        cubeEntity.updateModelMatrix();
    }

    @Override
    public void cleanup() {
        // nothing to be done yet
    }

}
