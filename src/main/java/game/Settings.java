package game;

public class Settings {

    // world
    public static final short WORLD_Y_LOWER_LIMIT = 0;
    public static final short WORLD_Y_UPPER_LIMIT = 128;
    public static final short CHUNK_WIDTH = 16;
    public static final short CHUNK_HEIGHT = 100;
    public static final short RENDER_DISTANCE = 15;
    public static final float PLAYER_HEIGHT = 2.0f;
    public static final float DEFAULT_SPAWN_X = 0;
    public static final float DEFAULT_SPAWN_Y = PLAYER_HEIGHT + 1;
    public static final float DEFAULT_SPAWN_Z = 0;

    // movement (all speeds/accelerations are in blocks per second, frame rate independent)
    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVE_ACCELERATION = 45.0f;
    public static final float MAX_HORIZONTAL_SPEED = 11.0f;
    public static final float MAX_VERTICAL_SPEED = 9.0f;
    // fraction of the velocity that survives after one full second of no input
    public static final float MOVE_DAMPING = 0.02f;
    // largest delta time a single frame may apply, stops lag spikes from teleporting the player
    public static final float MAX_DELTA_TIME = 0.1f;

    // blocks
    public static final float MAX_BLOCK_REACH = 5.0f;
    public static final long BREAK_COOLDOWN_MS = 200;
    public static final float TARGET_BLOCK_BORDER_THICKNESS = 1.0f; // increasing this to >1 wont work as intended,
                                                                    // OpenGL has deprecated line widths >1.0f
    public static final float[] TARGET_BLOCK_BORDER_COLOR = { 0.0f, 0.0f, 0.0f, 1.0f }; // black

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
