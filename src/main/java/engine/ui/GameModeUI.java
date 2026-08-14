package engine.ui;

import engine.Window;
import engine.graph.SvgLoader;
import engine.graph.Texture;
import engine.world.World;
import engine.world.player.GameMode;
import game.Settings;
import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

// The gamemode switcher shown while F1 is held. Its icons are SVGs rasterized
// on first use, so they stay sharp at whatever button size is configured.
public class GameModeUI {

    private final Texture[] icons = new Texture[GameMode.values().length];

    public GameModeUI() {
        // Managed by UIManager
    }

    public void render(World world, Window window) {
        GameMode[] modes = GameMode.values();
        int selected = Math.max(0, Math.min(modes.length - 1, world.getGameModeSelection()));

        int buttonSize = Settings.GAMEMODE_BUTTON_SIZE;
        int gap = Settings.GAMEMODE_BUTTON_GAP;
        int padding = Settings.GAMEMODE_PANEL_PADDING;
        float iconSize = buttonSize * Settings.GAMEMODE_ICON_SIZE;

        this.loadIcons(iconSize);

        int panelWidth = modes.length * buttonSize + (modes.length - 1) * gap + padding * 2;
        int panelHeight = buttonSize + padding * 2;
        float panelX = (window.getWidth() - panelWidth) / 2.0f;
        float panelY = (window.getHeight() - panelHeight) / 2.0f;

        int windowFlags = ImGuiWindowFlags.NoDecoration
                | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoFocusOnAppearing
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.NoInputs
                | ImGuiWindowFlags.NoBackground;

        // The window has to cover the label drawn under the panel as well, the draw
        // list is clipped to it.
        float labelHeight = Settings.GAMEMODE_LABEL_FONT_SIZE + Settings.GAMEMODE_LABEL_PADDING * 2;
        ImGui.setNextWindowPos(panelX - padding, panelY - padding);
        ImGui.setNextWindowSize(panelWidth + padding * 2, panelHeight + padding * 2 + labelHeight);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.begin("GameModeSelector", windowFlags);

        ImDrawList drawList = ImGui.getWindowDrawList();

        // Panel background
        drawList.addRectFilled(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                toImGuiColor(Settings.GAMEMODE_PANEL_COLOR));

        int iconColor = toImGuiColor(Settings.GAMEMODE_ICON_COLOR);

        for (int i = 0; i < modes.length; i++) {
            float x = panelX + padding + i * (buttonSize + gap);
            float y = panelY + padding;

            boolean isSelected = i == selected;
            int cellColor = isSelected ? Settings.GAMEMODE_BUTTON_SELECTED_COLOR : Settings.GAMEMODE_BUTTON_COLOR;
            drawList.addRectFilled(x, y, x + buttonSize, y + buttonSize, toImGuiColor(cellColor));

            if (isSelected) {
                drawList.addRect(x, y, x + buttonSize, y + buttonSize,
                        toImGuiColor(Settings.GAMEMODE_BUTTON_SELECTED_BORDER_COLOR), 0.0f, 0,
                        Settings.GAMEMODE_BUTTON_SELECTED_BORDER_SIZE);
            }

            Texture icon = this.icons[i];
            if (icon != null) {
                float iconX = x + (buttonSize - iconSize) / 2.0f;
                float iconY = y + (buttonSize - iconSize) / 2.0f;
                drawList.addImage(icon.getTextureId(), iconX, iconY, iconX + iconSize, iconY + iconSize,
                        0.0f, 0.0f, 1.0f, 1.0f, iconColor);
            }
        }

        // Name of the currently selected mode, centered under the panel
        drawLabel(drawList, modes[selected].getLabel(), panelX + panelWidth / 2.0f,
                panelY + panelHeight + Settings.GAMEMODE_LABEL_PADDING);

        ImGui.end();
        ImGui.popStyleColor();
    }

    // Rasterizes each icon once, at a multiple of its on screen size so it still
    // looks clean on a high DPI display.
    private void loadIcons(float iconSize) {
        int resolution = Math.round(iconSize * Settings.GAMEMODE_ICON_SUPERSAMPLE);
        GameMode[] modes = GameMode.values();

        for (int i = 0; i < modes.length; i++) {
            if (this.icons[i] != null) {
                continue;
            }
            String path = "icons/" + modes[i].name().toLowerCase() + "_icon.svg";
            this.icons[i] = SvgLoader.load(path, resolution);
        }
    }

    private void drawLabel(ImDrawList drawList, String text, float centerX, float y) {
        float fontSize = Settings.GAMEMODE_LABEL_FONT_SIZE;
        ImVec2 size = new ImVec2();
        ImGui.calcTextSize(size, text);

        // calcTextSize measures at the current font size, rescale it to the size the
        // label is actually drawn at.
        float scale = fontSize / ImGui.getFontSize();
        float textWidth = size.x * scale;

        drawList.addText(ImGui.getFont(), fontSize, centerX - textWidth / 2.0f, y,
                toImGuiColor(Settings.GAMEMODE_LABEL_COLOR), text);
    }

    // Settings stores colors as ARGB, ImGui's draw list wants a packed ABGR value
    private int toImGuiColor(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return ImColor.intToColor(r, g, b, a);
    }

    public void cleanup() {
        for (Texture icon : this.icons) {
            if (icon != null) {
                icon.cleanup();
            }
        }
    }

}
