package game;

public class Settings {

    // world
    public static final short WORLD_Y_LOWER_LIMIT = 0;
    public static final short WORLD_Y_UPPER_LIMIT = 128;
    public static final short CHUNK_WIDTH = 16;
    public static final short RENDER_DISTANCE = 3; // 3 chunks
    public static final float PLAYER_HEIGHT = 2.0f;
    public static final float SPAWN_X = 0;
    public static final float SPAWN_Y = PLAYER_HEIGHT + 1;
    public static final float SPAWN_Z = 0;

    // movement
    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVEMENT_SPEED = 0.005f;

    // blocks
    public static final float MAX_BLOCK_REACH = 5.0f;
    public static final long BREAK_COOLDOWN_MS = 200;

    // inventory
    public static final int HOTBAR_CELL_COUNT = 9;
    public static final int INVENTORY_SIZE = 9 * 4; // 0-8 hotbar, 9-35 main inventory

    // ui
    public static final int HOTBAR_CELL_SIZE = 60;
    public static final int HOTBAR_CELL_BORDER_SIZE = 4;
    public static final int HOTBAR_CELL_SELECTED_BORDER_SIZE = 6;
    public static final int HOTBAR_CELL_PADDING = 2;
    public static final int HOTBAR_BOTTOM_PADDING = 10;
    // colors (in ABGR format)
    public static final int HOTBAR_CELL_COLOR = 0x7F000000; // #7f000000
    public static final int HOTBAR_CELL_BORDER_COLOR = 0xFF8B8B8B; // #b0b0b0ff
    public static final int HOTBAR_CELL_SELECTED_BORDER_COLOR = 0xFFBFBFBF; // #bfbfbfff
    public static final int HOTBAR_CELL_ITEM_COUNT_COLOR = 0xFFFFFFFF; // #ffffffff
    public static final int HOTBAR_CELL_ITEM_COUNT_FONT_SIZE = 12;

}
