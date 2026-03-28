package engine.graph;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform4fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import org.lwjgl.system.MemoryStack;

public class UniformsMap {

    private int programId;
    private Map<String, Integer> uniforms;

    public UniformsMap(int programId) {
        this.programId = programId;
        this.uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException(
                    "Could not find uniform [" + uniformName + "] in shader program [" + programId + "]");
        }
        uniforms.put(uniformName, uniformLocation);
    }

    private int getUniformLocation(String uniformName) {
        Integer location = this.uniforms.get(uniformName);
        if (location == null) {
            throw new RuntimeException("Could not find uniform [" + uniformName + "]");
        }
        return location.intValue();
    }

    public void setUniform(String uniformName, int value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform1i(getUniformLocation(uniformName), value);
        }
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, Vector4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform4fv(getUniformLocation(uniformName), value.get(stack.mallocFloat(4)));
        }
    }

}
