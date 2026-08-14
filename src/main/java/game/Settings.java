package game;

public class Settings {

    // world
    public static final short WORLD_Y_LOWER_LIMIT = 0;
    public static final short WORLD_Y_UPPER_LIMIT = 128;
    public static final short CHUNK_WIDTH = 16;
    public static final short CHUNK_HEIGHT = 100;
    public static final short RENDER_DISTANCE = 15;
    public static final float PLAYER_HEIGHT = 1.8f;
    public static final float PLAYER_WIDTH = 0.6f;
    public static final float PLAYER_EYE_HEIGHT = 1.62f; // camera offset above the player's feet
    public static final float DEFAULT_SPAWN_X = 0;
    public static final float DEFAULT_SPAWN_Y = PLAYER_HEIGHT + 1;
    public static final float DEFAULT_SPAWN_Z = 0;

    // movement
    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVE_ACCELERATION = 45.0f;
    public static final float MAX_HORIZONTAL_SPEED = 11.0f;
    public static final float MAX_VERTICAL_SPEED = 9.0f;
    public static final float MOVE_DAMPING = 0.02f; // fraction of the velocity that survives after one full second of
                                                    // no input
    public static final float MAX_DELTA_TIME = 0.1f; // largest delta time a single frame may apply, stops lag spikes
                                                     // from teleporting the player
    public static final float DOUBLE_TAP_MAX_TIME = 0.5f; // seconds
    public static final float GRAVITY = 32.0f;
    public static final float TERMINAL_VELOCITY = 78.0f;
    public static final float JUMP_VELOCITY = 9.0f;
    public static final float WALK_ACCELERATION = 60.0f;
    public static final float MAX_WALK_SPEED = 4.5f;
    public static final float SNEAK_SPEED_MULTIPLIER = 0.3f;
    public static final float SPRINT_SPEED_MULTIPLIER = 1.35f;
    public static final float SPRINT_ACCELERATION_MULTIPLIER = 2.0f;
    public static final float FLY_SPRINT_SPEED_MULTIPLIER = 2.0f;
    public static final float FLY_SPRINT_ACCELERATION_MULTIPLIER = 2.0f;
    public static final float AIR_CONTROL = 0.25f; // fraction of the walk acceleration usable while airborne
    public static final float GROUND_DAMPING = 1e-6f; // fraction of the horizontal velocity surviving one second
    public static final float AIR_DAMPING = 0.6f; // much weaker, so jumps keep their momentum

    // collision
    public static final float COLLISION_EPSILON = 1e-3f; // gap left between the player and a block it snaps against

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

    // gamemode selector (held F1)
    public static final int GAMEMODE_BUTTON_SIZE = 80;
    public static final int GAMEMODE_BUTTON_GAP = 8;
    public static final int GAMEMODE_PANEL_PADDING = 10;
    public static final float GAMEMODE_ICON_SIZE = 0.6f; // fraction of the button a icon may occupy
    public static final float GAMEMODE_ICON_SUPERSAMPLE = 2.0f; // icons are rasterized at this multiple of their
                                                                // on screen size
    public static final int GAMEMODE_LABEL_FONT_SIZE = 20;
    public static final int GAMEMODE_LABEL_PADDING = 12; // gap between the panel and the selected mode's name
    // How far the virtual selection cursor travels per pixel of mouse movement.
    // Lower values need a wider mouse sweep to cross the whole panel.
    public static final float GAMEMODE_CURSOR_SENSITIVITY = 1.5f;
    // colors (in ARGB format)
    public static final int GAMEMODE_PANEL_COLOR = 0x80000000; // #00000080
    public static final int GAMEMODE_BUTTON_COLOR = 0x40000000; // #00000040
    public static final int GAMEMODE_BUTTON_SELECTED_COLOR = 0x59FFFFFF; // #ffffff59
    public static final int GAMEMODE_BUTTON_SELECTED_BORDER_COLOR = 0xFFFFFFFF; // #ffffffff
    public static final int GAMEMODE_BUTTON_SELECTED_BORDER_SIZE = 3;
    public static final int GAMEMODE_ICON_COLOR = 0xFFFFFFFF; // #ffffffff
    public static final int GAMEMODE_LABEL_COLOR = 0xFFFFFFFF; // #ffffffff

}
