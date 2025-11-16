package engine.graph;

import org.lwjgl.system.MemoryStack;

import engine.Utils;

import java.nio.*;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    private int textureId;
    private String texturePath;

    public Texture(int width, int height, ByteBuffer buf) {
        this.texturePath = "";
        this.generateTexture(width, height, buf);
    }

    public Texture(String texturePath) {

        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.texturePath = texturePath;
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer imgBuf = Utils.readResourceToByteBuffer(texturePath, 1024);

            ByteBuffer buf = stbi_load_from_memory(imgBuf, w, h, channels, 4); // desired channels = 4 (RGBA)
            if (buf == null) {
                throw new RuntimeException("Image file [" + texturePath + "] not loaded: " + stbi_failure_reason());
            }

            this.generateTexture(w.get(), h.get(), buf);
            stbi_image_free(buf);
        }
    }

    void generateTexture(int width, int height, ByteBuffer buf) {
        this.textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                width,
                height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buf);
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    public void cleanup() {
        glDeleteTextures(this.textureId);
    }

    public String getTexturePath() {
        return this.texturePath;
    }

}
