package engine;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

public class MouseInput {

    private Vector2f currentPos;
    private Vector2f previousPos;
    private Vector2f displVec;
    private boolean inWindow;
    private boolean leftButtonPressed;
    private boolean rightButtonPressed;
    private long windowHandle;

    public MouseInput(long windowHandle) {
        this.previousPos = new Vector2f(-1, -1);
        this.currentPos = new Vector2f();
        this.displVec = new Vector2f();
        this.leftButtonPressed = false;
        this.rightButtonPressed = false;
        this.inWindow = false;
        this.windowHandle = windowHandle;

        glfwSetCursorPosCallback(windowHandle, (handle, xpos, ypos) -> {
            this.currentPos.x = (float) xpos;
            this.currentPos.y = (float) ypos;
        });
        glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> {
            this.inWindow = entered;
        });
        glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mode) -> {
            this.leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            this.rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
        glfwSetScrollCallback(windowHandle, (handle, xoffset, yoffset) -> {
            this.scrollDelta = (float) yoffset;
        });
    }

    private float scrollDelta;

    public float getScrollDelta() {
        float delta = this.scrollDelta;
        this.scrollDelta = 0; // consume delta
        return delta;
    }

    public Vector2f getCurrentPos() {
        return this.currentPos;
    }

    public Vector2f getDisplVec() {
        return this.displVec;
    }

    public boolean isLeftButtonPressed() {
        return this.leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        boolean pressed = this.rightButtonPressed;
        this.rightButtonPressed = false; // Consume the press so it only fires once per click
        return pressed;
    }

    public void input() {
        this.displVec.x = 0;
        this.displVec.y = 0;

        if (this.inWindow) {
            double deltax = this.currentPos.x - this.previousPos.x;
            double deltay = this.currentPos.y - this.previousPos.y;

            if (deltax != 0) {
                this.displVec.y = (float) deltax;
            }
            if (deltay != 0) {
                this.displVec.x = (float) deltay;
            }
        }

        // Recenter mouse
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(this.windowHandle, w, h);
        glfwSetCursorPos(this.windowHandle, w[0] / 2.0, h[0] / 2.0);

        this.previousPos.set(w[0] / 2.0f, h[0] / 2.0f);
    }

}
