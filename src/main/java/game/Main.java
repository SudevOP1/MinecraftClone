package game;

import java.util.Random;
import java.util.Scanner;

import engine.world.World;
import utils.Debug;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String worldName, seedInput;
        int seed;

        Debug.print("Enter world name: ");
        worldName = sc.nextLine();

        Debug.print("Enter seed: ");
        seedInput = sc.nextLine();

        if (worldName.isEmpty()) {
            worldName = "New World";
        }
        if (seedInput.isEmpty()) {
            seed = new Random().nextInt();
        } else {
            seed = Integer.parseInt(seedInput);
        }

        World world = new World(seed, worldName);
        world.run();
        sc.close();
    }

}
