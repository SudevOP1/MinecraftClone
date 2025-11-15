package game;

import engine.Engine;
import engine.Window;
import engine.Render;
import engine.Scene;

import utils.Helpers;
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
        // nothing to be done yet
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
