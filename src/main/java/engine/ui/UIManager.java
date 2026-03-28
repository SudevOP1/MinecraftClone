package engine.ui;

import engine.Window;
import engine.world.World;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class UIManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final String glslVersion = "#version 330 core";

    private final DebugUI debugUI;
    private final HotbarUI hotbarUI;

    public UIManager(Window window) {
        ImGui.createContext();
        
        // Attempt to load font, but don't fail if it's missing
        try {
            ImGui.getIO().getFonts().addFontFromFileTTF("src/main/resources/font/Minecraft.ttf", 20.0f);
            ImGui.getIO().setFontGlobalScale(1.5f);
        } catch (Exception e) {
            System.err.println("Failed to load Minecraft font: " + e.getMessage());
        }

        imGuiGlfw.init(window.getWindowHandle(), true);
        imGuiGl3.init(glslVersion);

        // UI components
        this.debugUI = new DebugUI();
        this.hotbarUI = new HotbarUI();
    }

    public void render(World world, Window window) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        if (world.isF3Pressed()) {
            debugUI.render(world);
        }
        hotbarUI.render(world, window);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanup() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
