package engine.graph;

import java.util.*;

public class TextureCache {

    public static final String DEFAULT_TEXTURE = "models/default_texture.png";

    private Map<String, Texture> textureMap;

    public TextureCache() {
        this.textureMap = new HashMap<>();
        this.textureMap.put(DEFAULT_TEXTURE, new Texture(DEFAULT_TEXTURE));
    }

    public Texture createTexture(String texturePath) {
        return this.textureMap.computeIfAbsent(texturePath, path -> new Texture(path));
    }

    public Texture getTexture(String texturePath) {
        Texture texture = null;
        if (texturePath != null) {
            texture = textureMap.get(texturePath);
        }
        if (texture == null) {
            texture = textureMap.get(DEFAULT_TEXTURE);
        }
        return texture;
    }

    public void cleanup() {
        textureMap.values().forEach((texture) -> {
            texture.cleanup();
        });
    }

}
