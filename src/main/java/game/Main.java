package game;

import engine.world.World;

public class Main {

    public static void main(String[] args) {

        int seed = 0;
        String worldName = "New World";

        World world = new World(seed, worldName);
        world.run();

    }

}
