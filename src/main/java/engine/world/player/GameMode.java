package engine.world.player;

public enum GameMode {
    SURVIVAL("Survival"),
    CREATIVE("Creative"),
    SPECTATOR("Spectator");

    private final String label;

    GameMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

}
