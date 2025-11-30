package engine.block;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockRegistry {

    private static final Map<String, BlockType> REGISTRY = new HashMap<>();

    static {
        loadFromJson("BlocksData.json");
    }

    public static BlockType get(String codename) {
        return REGISTRY.get(codename);
    }

    private static class BlocksWrapper {
        public List<BlockType> blocks;
    }

    private static void loadFromJson(String filename) {
        try {
            InputStream is = BlockRegistry.class.getClassLoader().getResourceAsStream(filename);

            if (is == null) {
                System.err.println("[ERROR] " + filename + " not found in classpath");
                return;
            }

            Reader reader = new InputStreamReader(is);

            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            BlocksWrapper data = gson.fromJson(reader, BlocksWrapper.class);

            if (data == null) {
                System.err.println("[ERROR] GSON returned null, JSON format may be invalid");
                reader.close();
                return;
            }

            if (data.blocks == null) {
                System.err.println("[ERROR] 'blocks' field is null in JSON");
                reader.close();
                return;
            }

            for (BlockType block : data.blocks) {
                if (block == null) {
                    System.err.println("[WARNING] skipping null block in list");
                    continue;
                }
                if (block.codename == null) {
                    System.err.println("[WARNING] skipping block with null codename: " + block.name);
                    continue;
                }
                REGISTRY.put(block.codename, block);
            }

            reader.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load " + filename + ":");
            e.printStackTrace();
        }
    }
}