package engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

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
    }

    public Vector2f getCurrentPos() {
        return this.currentPos;
    }

    public Vector2f getDisplVec() {
        return this.displVec;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public void input() {
        displVec.x = 0;
        displVec.y = 0;

        if (inWindow) {
            double deltax = currentPos.x - previousPos.x;
            double deltay = currentPos.y - previousPos.y;

            if (deltax != 0)
                displVec.y = (float) deltax;
            if (deltay != 0)
                displVec.x = (float) deltay;
        }

        // Recenter mouse
        int[] w = new int[1];
        int[] h = new int[1];
        glfwGetWindowSize(this.windowHandle, w, h);
        glfwSetCursorPos(this.windowHandle, w[0] / 2.0, h[0] / 2.0);

        previousPos.set(w[0] / 2.0f, h[0] / 2.0f);
    }

}
