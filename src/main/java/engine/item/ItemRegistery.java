package engine.item;

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

public class ItemRegistery {

    private static final Map<String, ItemType> REGISTRY = new HashMap<>();
    private static int itemLength = 256;
    private static int atlasWidth = 2048;
    private static int atlasHeight = 512;

    static {
        loadFromJson("items_data.json");
    }

    public static ItemType get(String codename) {
        return REGISTRY.get(codename);
    }

    public static int getItemLength() {
        return itemLength;
    }

    public static int getAtlasWidth() {
        return atlasWidth;
    }

    public static int getAtlasHeight() {
        return atlasHeight;
    }

    public static int getAtlasColumns() {
        return atlasWidth / itemLength;
    }

    public static int getAtlasRows() {
        return atlasHeight / itemLength;
    }

    private static void loadFromJson(String filename) {
        try {
            InputStream is = ItemRegistery.class.getClassLoader().getResourceAsStream(filename);

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
            if (root.has("itemLength")) {
                itemLength = root.get("itemLength").getAsInt();
            }

            if (root.has("resolution")) {
                JsonObject resolution = root.getAsJsonObject("resolution");
                atlasWidth = resolution.get("width").getAsInt();
                atlasHeight = resolution.get("height").getAsInt();
            }

            // Read items
            if (!root.has("icons")) {
                Debug.errln("'icons' field not found in JSON");
                reader.close();
                return;
            }

            JsonObject iconsObj = root.getAsJsonObject("icons");

            for (String codename : iconsObj.keySet()) {
                JsonObject iconData = iconsObj.getAsJsonObject(codename);
                ItemType icon = gson.fromJson(iconData, ItemType.class);

                if (icon == null) {
                    Debug.logln("Skipping null icon: " + codename);
                    continue;
                }

                icon.codename = codename;
                REGISTRY.put(codename, icon);
            }

            reader.close();

            Debug.logln("Loaded " + REGISTRY.size() + " icons from " + filename);

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
