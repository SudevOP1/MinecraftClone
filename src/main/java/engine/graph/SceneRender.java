package engine.graph;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.system.MemoryUtil;

import data_structures.Vector3s;
import engine.scene.Entity;
import engine.scene.Scene;
import game.Settings;

public class SceneRender {

    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    private int targetBlockVaoId;
    private int targetBlockVboId;
    private int targetBlockVertices;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        this.createUniforms();
        this.initTargetBlockBorder();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        glDeleteBuffers(targetBlockVboId);
        glDeleteVertexArrays(targetBlockVaoId);
    }

    public void render(Scene scene) {
        shaderProgram.bind();
        uniformsMap.setUniform("isWireframe", 0);
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("txtSampler", 0);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();

        // First render opaque materials
        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();

            for (Material material : model.getMaterialList()) {
                if (material.isTransparent()) {
                    continue;
                }
                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                for (Mesh mesh : material.getMeshList()) {
                    glBindVertexArray(mesh.getVaoId());
                    for (Entity entity : entities) {
                        uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    }
                }
            }
        }

        // Collect transparent drawables (material, mesh, entity) to sort and draw
        // back-to-front
        List<TransparentDraw> transparentDraws = new ArrayList<>();
        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();
            for (Material material : model.getMaterialList()) {
                if (!material.isTransparent()) {
                    continue;
                }
                for (Mesh mesh : material.getMeshList()) {
                    for (Entity entity : entities) {
                        transparentDraws.add(new TransparentDraw(material, mesh, entity));
                    }
                }
            }
        }

        if (!transparentDraws.isEmpty()) {
            // sort by distance from camera (furthest first)
            transparentDraws.sort((a, b) -> {
                float da = scene.getCamera().getPosition().distanceSquared(a.entity.getPosition());
                float db = scene.getCamera().getPosition().distanceSquared(b.entity.getPosition());
                return Float.compare(db, da);
            });

            // Disable depth writes while drawing transparent objects
            glDepthMask(false);
            for (TransparentDraw td : transparentDraws) {
                Texture texture = textureCache.getTexture(td.material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();
                glBindVertexArray(td.mesh.getVaoId());
                uniformsMap.setUniform("modelMatrix", td.entity.getModelMatrix());
                glDrawElements(GL_TRIANGLES, td.mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
            }
            // Re-enable depth writes
            glDepthMask(true);
        }

        // Target Block wireframe rendering
        if (scene.getTargetBlock() != null) {
            Vector3s tb = scene.getTargetBlock();
            glLineWidth(Settings.TARGET_BLOCK_BORDER_THICKNESS);
            uniformsMap.setUniform("isWireframe", 1);

            float[] color = Settings.TARGET_BLOCK_BORDER_COLOR;
            uniformsMap.setUniform("wireframeColor", new Vector4f(color[0], color[1], color[2], color[3]));

            // Slightly upscale the box (centered) to prevent z-fighting with the block
            float offset = -0.002f;
            float scale = 1.004f;
            Matrix4f modelMatrix = new Matrix4f()
                    .translate(tb.x + offset, tb.y + offset, tb.z + offset)
                    .scale(scale);
            uniformsMap.setUniform("modelMatrix", modelMatrix);

            glBindVertexArray(targetBlockVaoId);
            glDrawArrays(GL_LINES, 0, targetBlockVertices);

            uniformsMap.setUniform("isWireframe", 0);
            glLineWidth(1.0f); // Reset line width
        }

        glBindVertexArray(0);
        shaderProgram.unbind();
    }

    private static class TransparentDraw {

        final Material material;
        final Mesh mesh;
        final engine.scene.Entity entity;

        TransparentDraw(Material material, Mesh mesh, engine.scene.Entity entity) {
            this.material = material;
            this.mesh = mesh;
            this.entity = entity;
        }
    }

    private void createUniforms() {
        this.uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        this.uniformsMap.createUniform("projectionMatrix");
        this.uniformsMap.createUniform("viewMatrix");
        this.uniformsMap.createUniform("modelMatrix");
        this.uniformsMap.createUniform("txtSampler");
        this.uniformsMap.createUniform("isWireframe");
        this.uniformsMap.createUniform("wireframeColor");
    }

    private void initTargetBlockBorder() {
        float[] positions = new float[]{
            0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, // Bottom
            0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, // Top
            0, 0, 0, 0, 1, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1 // Vertical
        };
        targetBlockVertices = positions.length / 3;
        targetBlockVaoId = glGenVertexArrays();
        glBindVertexArray(targetBlockVaoId);
        targetBlockVboId = glGenBuffers();
        FloatBuffer posBuffer = MemoryUtil.memAllocFloat(positions.length);
        posBuffer.put(positions).flip();
        glBindBuffer(GL_ARRAY_BUFFER, targetBlockVboId);
        glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        MemoryUtil.memFree(posBuffer);
    }

}
