package engine.graph;

import engine.Window;
import engine.scene.Scene;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.lwjgl.opengl.GL11.*;

public class Render {

    private SceneRender sceneRender;
    private boolean wireframeMode = false;

    public Render() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        sceneRender = new SceneRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        // set polygon mode based on wireframe flag
        if (wireframeMode) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        sceneRender.render(scene);
    }

    public void toggleWireframe() {
        wireframeMode = !wireframeMode;
    }

    public boolean isWireframeMode() {
        return wireframeMode;
    }

    public void takeScreenshot(Window window) {
        int width = window.getWidth();
        int height = window.getHeight();

        // allocate buffer on heap instead of stack
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);

        try {
            // read pixels from framebuffer
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            // create BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // convert ByteBuffer to BufferedImage
            // openGL origin is bottom-left, so we flip vertically
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int i = (x + (height - 1 - y) * width) * 4;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    int a = buffer.get(i + 3) & 0xFF;

                    image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }

            // create ss directory if it doesn't exist
            File directory = new File("ss");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String filename = "screenshots/screenshot_" + timestamp + ".png";
            File outputFile = new File(filename);

            // save image
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("[DEBUG] Screenshot saved: " + filename);

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save screenshot: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // free the heap-allocated buffer
            MemoryUtil.memFree(buffer);
        }
    }

}
