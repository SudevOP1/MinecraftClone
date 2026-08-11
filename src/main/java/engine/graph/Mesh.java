package engine.graph;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private int numVertices;
    private int vaoId; // VAO = Vertex Array Object
    private List<Integer> vboIdList; // VBO = Vertex Buffer Object

    public Mesh(float[] positions, float textCoords[], int[] indices) {
        this(positions, positions.length, textCoords, textCoords.length, indices, indices.length);
    }

    // Length-aware variant: lets callers pass oversized scratch arrays (e.g. a
    // chunk mesh builder's growable buffers) without copying them down to size
    // first.
    public Mesh(float[] positions, int positionsLength,
            float[] textCoords, int textCoordsLength,
            int[] indices, int indicesLength) {
        numVertices = indicesLength;
        vboIdList = new ArrayList<>();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // positions VBO
        int vboId = glGenBuffers();
        vboIdList.add(vboId);
        FloatBuffer positionsBuffer = MemoryUtil.memAllocFloat(positionsLength);
        positionsBuffer.put(positions, 0, positionsLength).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // texture VBO
        vboId = glGenBuffers();
        vboIdList.add(vboId);
        FloatBuffer textCoordsBuffer = MemoryUtil.memAllocFloat(textCoordsLength);
        textCoordsBuffer.put(textCoords, 0, textCoordsLength).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        // index VBO
        vboId = glGenBuffers();
        vboIdList.add(vboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indicesLength);
        indicesBuffer.put(indices, 0, indicesLength).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // unbind VBOs (VAO keeps the element buffer binding)
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // free the buffers
        MemoryUtil.memFree(positionsBuffer);
        MemoryUtil.memFree(textCoordsBuffer);
        MemoryUtil.memFree(indicesBuffer);
    }

    public void cleanup() {
        vboIdList.forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoId);
    }

    public int getNumVertices() {
        return numVertices;
    }

    public final int getVaoId() {
        return vaoId;
    }
}