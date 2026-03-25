package engine.ui;

import org.joml.Vector3f;

import data_structures.Vector3s;
import engine.Window;
import engine.block.BlockType;
import engine.world.World;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class DebugGui {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final String glslVersion = "#version 330 core";

    public DebugGui(Window window) {
        ImGui.createContext();

        // loading font
        ImGui.getIO().getFonts().addFontFromFileTTF("src/main/resources/font/Minecraft.ttf", 20.0f);
        ImGui.getIO().setFontGlobalScale(1.5f); // font size

        imGuiGlfw.init(window.getWindowHandle(), true);
        imGuiGl3.init(glslVersion);
    }

    public void render(World world) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        int windowFlags = ImGuiWindowFlags.NoDecoration
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoFocusOnAppearing
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.NoBackground;

        ImGui.setNextWindowPos(10, 10);

        // Background color
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.0f, 0.0f, 0.0f, 0.01f);

        // Text color
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f);

        ImGui.begin("Debug", windowFlags);

        // Rendering white text without shadow for now
        Vector3f pos = world.camera.getPosition();
        Vector3f rot = world.camera.getRotation();

        drawTextWithBg(String.format("Player X, Y, Z: %.3f, %.3f, %.3f", pos.x, pos.y, pos.z));
        drawTextWithBg(String.format("Camera Pitch, Yaw, Roll: %.2f, %.2f, %.2f", rot.x, rot.y, rot.z));

        Vector3s targetBlock = world.getTargetBlock();
        if (targetBlock != null) {
            BlockType blockType = world.getBlockAt(targetBlock.x, targetBlock.y, targetBlock.z);
            String blockName = blockType != null ? blockType.name : "Unknown";
            drawTextWithBg(String.format("Target Block: %s (%d, %d, %d)", blockName, targetBlock.x, targetBlock.y, targetBlock.z));
        } else {
            drawTextWithBg("Target Block: None");
        }

        ImGui.end();
        ImGui.popStyleColor(2);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void drawTextWithBg(String text) {
        ImVec2 pos = ImGui.getCursorScreenPos();
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, text);

        float paddingX = 2.0f;
        float paddingY = 2.0f;

        ImGui.getWindowDrawList().addRectFilled(
                pos.x - paddingX, pos.y - paddingY,
                pos.x + size.x + paddingX, pos.y + size.y + paddingY,
                imgui.ImColor.intToColor(0, 0, 0, 50) // (0 - 255)
        );
        ImGui.text(text);
    }

    public void cleanup() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
