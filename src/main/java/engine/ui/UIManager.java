package engine.ui;

import engine.Window;
import engine.world.World;
import engine.world.player.GameMode;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public class UIManager {
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private final String glslVersion = "#version 330 core";

    private final DebugUI debugUI;
    private final HotbarUI hotbarUI;
    private final GameModeUI gameModeUI;

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
        this.gameModeUI = new GameModeUI();
    }

    public void render(World world, Window window) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        // hotbar ui
        if (world.getGameMode() != GameMode.SPECTATOR) {
            hotbarUI.render(world, window);
        }

        // F1 gamemode selector
        if (world.isGameModeMenuOpen()) {
            gameModeUI.render(world, window);
        }

        // F3 debug UI
        if (world.isF3Pressed()) {
            debugUI.render(world);
        }

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void cleanup() {
        gameModeUI.cleanup();
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
