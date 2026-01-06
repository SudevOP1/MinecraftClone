package engine.graph;

import java.util.*;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class TextureCache {

    public static final String DEFAULT_TEXTURE = "models/default_texture.png";

    private Map<String, Texture> textureMap;

    public TextureCache() {
        this.textureMap = new HashMap<>();
        // Create a generated 1x1 magenta default texture to avoid loading a file
        ByteBuffer buf = BufferUtils.createByteBuffer(4);
        buf.put((byte) 0xFF); // R
        buf.put((byte) 0x00); // G
        buf.put((byte) 0xFF); // B
        buf.put((byte) 0xFF); // A
        buf.flip();
        Texture defaultTex = new Texture(1, 1, buf);
        this.textureMap.put(DEFAULT_TEXTURE, defaultTex);
    }

    public Texture createTexture(String texturePath) {
        if (texturePath == null) {
            return textureMap.get(DEFAULT_TEXTURE);
        }
        return this.textureMap.computeIfAbsent(texturePath, path -> {
            try {
                return new Texture(path);
            } catch (RuntimeException e) {
                return textureMap.get(DEFAULT_TEXTURE);
            }
        });
    }

    public Texture getTexture(String texturePath) {
        Texture texture = null;
        if (texturePath != null) {
            texture = textureMap.get(texturePath);
            if (texture == null) {
                // try to create it lazily
                texture = createTexture(texturePath);
            }
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
