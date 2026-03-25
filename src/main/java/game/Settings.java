package game;

public class Settings {

    // world
    public static final short WORLD_Y_LOWER_LIMIT = 0;
    public static final short WORLD_Y_UPPER_LIMIT = 128;
    public static final short CHUNK_WIDTH = 16;
    public static final short RENDER_DISTANCE = 3; // 3 chunks

    // movement
    public static final float MOUSE_SENSITIVITY = 0.1f;
    public static final float MOVEMENT_SPEED = 0.005f;

    // blocks
    public static final float MAX_BLOCK_REACH = 5.0f;
    public static final long BREAK_COOLDOWN_MS = 200;

}
