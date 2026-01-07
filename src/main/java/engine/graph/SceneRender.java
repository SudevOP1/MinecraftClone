package engine.graph;

import engine.scene.Entity;
import engine.scene.Scene;

import java.util.*;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        this.createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void render(Scene scene) {
        shaderProgram.bind();
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("txtSampler", 0);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();

        // First render opaque materials
        for (Model model : models) {
            List<Entity> entities = model.getEntitiesList();

            for (Material material : model.getMaterialList()) {
                if (material.isTransparent())
                    continue;
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
                if (!material.isTransparent())
                    continue;
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
    }

}
