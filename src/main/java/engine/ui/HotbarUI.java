package engine.ui;

import engine.Window;
import engine.graph.Texture;
import engine.item.ItemRegistry;
import engine.item.ItemType;
import engine.world.World;
import engine.world.player.Inventory;
import game.Settings;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public class HotbarUI {

    private Texture itemsAtlas;

    public HotbarUI() {
        // Managed by UIManager
    }

    public void render(World world, Window window) {
        // Lazy-load the items atlas texture
        if (itemsAtlas == null) {
            itemsAtlas = new Texture("items_atlas.png");
        }

        int windowFlags = ImGuiWindowFlags.NoDecoration
                | ImGuiWindowFlags.AlwaysAutoResize
                | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoFocusOnAppearing
                | ImGuiWindowFlags.NoNav
                | ImGuiWindowFlags.NoBackground;

        // Background color
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.0f, 0.0f, 0.0f, 0.0f);
        // Text color
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 1.0f, 1.0f, 1.0f);

        Inventory inventory = world.getInventory();
        int screenWidth = window.getWidth();
        int screenHeight = window.getHeight();

        int cellSize = Settings.HOTBAR_CELL_SIZE;
        int padding = Settings.HOTBAR_CELL_PADDING;
        int border = Settings.HOTBAR_CELL_BORDER_SIZE;
        int totalCellSize = cellSize + (padding + border) * 2;
        int totalWidth = Settings.HOTBAR_CELL_COUNT * totalCellSize;

        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - totalCellSize - Settings.HOTBAR_BOTTOM_PADDING;

        // Use a small buffer to prevent clipping of the leftmost/rightmost borders
        ImGui.setNextWindowPos(startX - 10, startY - 10);
        ImGui.setNextWindowSize(totalWidth + 20, totalCellSize + 20);
        ImGui.begin("Hotbar", windowFlags);

        int atlasColumns = ItemRegistry.getAtlasColumns();
        int atlasRows = ItemRegistry.getAtlasRows();
        int atlasTextureId = itemsAtlas.getTextureId();

        // Draw hotbar cells first
        for (int i = 0; i < Settings.HOTBAR_CELL_COUNT; i++) {
            int x = startX + i * totalCellSize + padding + border;
            int y = startY + padding + border;

            // Draw cell background
            ImGui.getWindowDrawList().addRectFilled(x, y, x + cellSize, y + cellSize, Settings.HOTBAR_CELL_COLOR);

            // Draw border
            int borderColor = (i == inventory.getSelectedSlot()) ? Settings.HOTBAR_CELL_SELECTED_BORDER_COLOR : Settings.HOTBAR_CELL_BORDER_COLOR;
            int thickness = (i == inventory.getSelectedSlot()) ? Settings.HOTBAR_CELL_SELECTED_BORDER_SIZE : Settings.HOTBAR_CELL_BORDER_SIZE;

            // Adjust rect to handle the thicker selected border without shifting cells
            ImGui.getWindowDrawList().addRect(x - border, y - border, x + cellSize + border, y + cellSize + border, borderColor, 0.0f, 0, (float) thickness);

        }

        // Draw item icons and item counts
        for (int i = 0; i < Settings.HOTBAR_CELL_COUNT; i++) {
            int x = startX + i * totalCellSize + padding + border;
            int y = startY + padding + border;

            // Draw item icon from atlas
            ItemType item = inventory.getItem(i);
            if (item != null) {
                int iconIndex = item.getIcon();
                int col = iconIndex % atlasColumns;
                int row = iconIndex / atlasColumns;

                float u0 = (float) col / atlasColumns;
                float v0 = (float) row / atlasRows;
                float u1 = (float) (col + 1) / atlasColumns;
                float v1 = (float) (row + 1) / atlasRows;

                // Render the atlas sub-region as the item icon with a small inset
                ImGui.getWindowDrawList().addImage(atlasTextureId, x + 4, y + 4, x + cellSize - 4, y + cellSize - 4, u0, v0, u1, v1);

                // Draw item count
                int itemCount = inventory.getItemCount(i);
                if (itemCount > 1) {
                    String countText = String.valueOf(itemCount);

                    ImGui.getFont().setScale((float) Settings.HOTBAR_CELL_ITEM_COUNT_FONT_SIZE / (float) ImGui.getFontSize());
                    // Draw at bottom right of the cell, leaving a small margin
                    float textX = x + cellSize / 2;
                    float textY = y + cellSize / 2;
                    // ImGui expects colors as ABGR or we can use the hex as is if we ensure alpha is full (FF).
                    ImGui.getWindowDrawList().addText(textX, textY, this.getImguiColor(Settings.HOTBAR_CELL_ITEM_COUNT_COLOR), countText);
                    ImGui.getFont().setScale(1.0f); // Reset scale
                }
            }
        }

        ImGui.end();
        ImGui.popStyleColor(2);
    }

    private int getImguiColor(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return ImGui.getColorU32(r, g, b, a);
    }

}
