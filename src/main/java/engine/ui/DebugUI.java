package engine.ui;

import org.joml.Vector3f;

import data_structures.Vector3s;
import engine.block.BlockType;
import engine.world.World;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public class DebugUI {

    public DebugUI() {
        // Managed by UIManager
    }

    public void render(World world) {
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

        Vector3f pos = world.camera.getPosition();
        Vector3f rot = world.camera.getRotation();
        String direction = "";
        if (rot.y == 0) {
            direction = "North";
        } else if (rot.y == Math.PI / 2) {
            direction = "East";
        } else if (rot.y == Math.PI) {
            direction = "South";
        } else if (rot.y == 3 * Math.PI / 2) {
            direction = "West";
        }

        drawTextWithBg(String.format("Player X, Y, Z: %.3f, %.3f, %.3f", pos.x, pos.y, pos.z));
        drawTextWithBg(String.format("Camera Pitch, Yaw, Roll: %.2fdeg, %.2fdeg, %.2fdeg", Math.toDegrees(rot.x), Math.toDegrees(rot.y), Math.toDegrees(rot.z)));
        drawTextWithBg(String.format("Direction: Facing %s", direction));

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
                imgui.ImColor.intToColor(0, 0, 0, 50)
        );
        ImGui.text(text);
    }
}
