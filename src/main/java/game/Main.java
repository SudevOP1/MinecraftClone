package game;

import engine.Engine;
import engine.Window;
import engine.graph.Mesh;
import engine.graph.Render;
import engine.scene.Scene;

import engine.IAppLogic;

public class Main implements IAppLogic {

    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("MinecraftClone", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        // nothing to be done yet
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        float[] positions = new float[] {
                0.0f, 0.5f, -1.0f,
                -0.5f, -0.5f, -1.0f,
                0.5f, -0.5f, -1.0f
        };
        Mesh mesh = new Mesh(positions, 3);
        scene.addMesh("triangle", mesh);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis) {
        // nothing to be done yet
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        // nothing to be done yet
    }

}
