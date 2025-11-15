package engine.graph;

import engine.Window;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import static org.lwjgl.opengl.GL11.*;

public class Render {

    private SceneRender sceneRender;

    public Render() {
        GL.createCapabilities();
        sceneRender = new SceneRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
    }

    public void render(Window window, Scene scene) {
        glViewport(0, 0, window.getWidth(), window.getHeight());
        sceneRender.render(scene);
    }

}
