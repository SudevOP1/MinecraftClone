package engine.world.player;

public enum GameMode {
    SURVIVAL,
    CREATIVE,
    SPECTATOR;

    public boolean canBreakBlocks() {
        return this == SURVIVAL || this == CREATIVE;
    }

    public boolean canPlaceBlocks() {
        return this == SURVIVAL || this == CREATIVE;
    }

    public boolean canFly() {
        return this == CREATIVE || this == SPECTATOR;
    }

    public boolean canBreakBlocksInstantly() {
        return this == CREATIVE;
    }

}
