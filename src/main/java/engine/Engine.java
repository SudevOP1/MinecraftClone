package engine;

import engine.scene.Scene;
import engine.graph.Render;

public class Engine {

    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private boolean running;
    private Scene scene;
    private int targetFps;
    private int targetUps;

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic) {
        this.window = new Window(windowTitle, opts, () -> {
            this.resize();
            return null;
        });
        this.targetFps = opts.fps;
        this.targetUps = opts.ups;
        this.appLogic = appLogic;
        this.render = new Render();
        this.scene = new Scene(this.window.getWidth(), this.window.getHeight());
        this.appLogic.init(this.window, scene, render);
        this.running = true;
    }

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic, float x, float y, float z) {
        this.window = new Window(windowTitle, opts, () -> {
            this.resize();
            return null;
        });
        this.targetFps = opts.fps;
        this.targetUps = opts.ups;
        this.appLogic = appLogic;
        this.render = new Render();
        this.scene = new Scene(this.window.getWidth(), this.window.getHeight(), x, y, z);
        this.appLogic.init(this.window, scene, render);
        this.running = true;
    }

    public void start() {
        this.running = true;
        this.run();
    }

    public void stop() {
        this.running = false;
    }

    private void run() {

        long initialTime = System.currentTimeMillis();
        float deltaUpdate = 0;
        float deltaFps = 0;
        float timeU = 1000.0f / this.targetUps; // maximum elapsed time between updates
        float timeR = this.targetFps > 0 ? 1000.0f / this.targetFps : 0; // maximum elapsed time between render calls

        long updateTime = initialTime;
        while (this.running && !this.window.windowShouldClose()) {
            this.window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (this.targetFps <= 0 || deltaFps >= 1) {
                this.window.getMouseInput().input();
                this.appLogic.input(this.window, this.scene, now - initialTime, this.render);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                this.appLogic.update(this.window, this.scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (this.targetFps <= 0 || deltaFps >= 1) {
                this.render.render(this.window, this.scene);
                deltaFps--;
                this.window.update();
            }
            initialTime = now;
        }

        this.cleanup(); // to free the resources
    }

    private void cleanup() {
        this.appLogic.cleanup();
        this.scene.cleanup();
        this.render.cleanup();
        this.window.cleanup();
    }

    private void resize() {
        this.scene.resize(this.window.getWidth(), this.window.getHeight());
    }

}
