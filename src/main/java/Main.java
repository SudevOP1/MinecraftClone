import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

public class Main {

    public static void main(String[] args) {

        // initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("GLFW failed to initialize!");
        }

        // create window
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(primaryMonitor);
        long window = GLFW.glfwCreateWindow(vidMode.width(), vidMode.height(), "Minecraft", primaryMonitor, 0);
        if (window == 0) {
            throw new RuntimeException("failed to create window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // main loop
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();

            GL11.glClearColor(0.0f, 0.545f, 0.8196f, 1.0f); // #008bd1

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GLFW.glfwSwapBuffers(window);
        }

        GLFW.glfwTerminate();
    }
}
