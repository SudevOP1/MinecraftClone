package engine;

import engine.scene.Scene;
import engine.graph.Render;

public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene, long diffTimeMillis, Render render);

    void update(Window window, Scene scene, long diffTimeMillis);
}
