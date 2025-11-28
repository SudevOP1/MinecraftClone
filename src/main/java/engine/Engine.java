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
            resize();
            return null;
        });
        this.targetFps = opts.fps;
        this.targetUps = opts.ups;
        this.appLogic = appLogic;
        this.render = new Render();
        this.scene = new Scene(window.getWidth(), window.getHeight());
        this.appLogic.init(window, scene, render);
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
        float timeU = 1000.0f / targetUps; // maximum elapsed time between updates
        float timeR = targetFps > 0 ? 1000.0f / targetFps : 0; // maximum elapsed time between render calls

        long updateTime = initialTime;
        while (running && !this.window.windowShouldClose()) {
            this.window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            if (this.targetFps <= 0 || deltaFps >= 1) {
                this.window.getMouseInput().input();
                this.appLogic.input(this.window, this.scene, now - initialTime);
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

        cleanup(); // to free the resources
    }

    private void cleanup() {
        appLogic.cleanup();
        scene.cleanup();
        render.cleanup();
        window.cleanup();
    }

    private void resize() {
        this.scene.resize(window.getWidth(), window.getHeight());
    }

}
