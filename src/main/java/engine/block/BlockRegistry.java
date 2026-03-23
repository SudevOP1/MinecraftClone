package engine.block;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import utils.Debug;

public class BlockRegistry {

    private static final Map<String, BlockType> REGISTRY = new HashMap<>();
    private static int blockLength = 512;
    private static int atlasWidth = 4096;
    private static int atlasHeight = 2048;

    static {
        loadFromJson("BlocksData.json");
    }

    public static BlockType get(String codename) {
        return REGISTRY.get(codename);
    }

    public static int getBlockLength() {
        return blockLength;
    }

    public static int getAtlasWidth() {
        return atlasWidth;
    }

    public static int getAtlasHeight() {
        return atlasHeight;
    }

    public static int getAtlasColumns() {
        return atlasWidth / blockLength;
    }

    public static int getAtlasRows() {
        return atlasHeight / blockLength;
    }

    private static void loadFromJson(String filename) {
        try {
            InputStream is = BlockRegistry.class.getClassLoader().getResourceAsStream(filename);

            if (is == null) {
                Debug.errln("File " + filename + " not found in classpath");
                return;
            }

            Reader reader = new InputStreamReader(is);

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            JsonObject root = gson.fromJson(reader, JsonObject.class);

            if (root == null) {
                Debug.errln("Invalid JSON format in " + filename);
                reader.close();
                return;
            }

            // Read metadata
            if (root.has("blockLength")) {
                blockLength = root.get("blockLength").getAsInt();
            }

            if (root.has("resolution")) {
                JsonObject resolution = root.getAsJsonObject("resolution");
                atlasWidth = resolution.get("width").getAsInt();
                atlasHeight = resolution.get("height").getAsInt();
            }

            // Read blocks
            if (!root.has("blocks")) {
                Debug.errln("'blocks' field not found in JSON");
                reader.close();
                return;
            }

            JsonObject blocksObj = root.getAsJsonObject("blocks");

            for (String codename : blocksObj.keySet()) {
                JsonObject blockData = blocksObj.getAsJsonObject(codename);
                BlockType block = gson.fromJson(blockData, BlockType.class);

                if (block == null) {
                    Debug.logln("Skipping null block: " + codename);
                    continue;
                }

                block.codename = codename;
                REGISTRY.put(codename, block);
            }

            reader.close();

            Debug.logln("Loaded " + REGISTRY.size() + " blocks from " + filename);
            Debug.logln("Atlas: " + atlasWidth + "x" + atlasHeight + ", Block size: " + blockLength);

        } catch (Exception e) {
            Debug.errln("Failed to load " + filename + ":");
            e.printStackTrace();
        }
    }

    public static int size() {
        return REGISTRY.size();
    }

    public static Set<String> keySet() {
        return REGISTRY.keySet();
    }

}
