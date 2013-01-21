package nl.esciencecenter.visualization.esalsa.glExt;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

import nl.esciencecenter.visualization.openglCommon.datastructures.GLSLAttrib;
import nl.esciencecenter.visualization.openglCommon.datastructures.Material;
import nl.esciencecenter.visualization.openglCommon.datastructures.VBO;

public class Model {
    public static enum vertex_format {
        TRIANGLES, POINTS, LINES
    };

    protected vertex_format format;
    protected FloatBuffer   vertices, normals, texCoords;
    protected VBO           vbo;
    protected int           numVertices;

    protected Material      material;

    private boolean         initialized = false;

    public Model(Material material, vertex_format format) {
        vertices = null;
        normals = null;
        texCoords = null;
        numVertices = 0;

        this.material = material;
        this.format = format;
    }

    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib nAttrib = new GLSLAttrib(normals, "MCnormal",
                    GLSLAttrib.SIZE_FLOAT, 3);
            GLSLAttrib tAttrib = new GLSLAttrib(texCoords, "MCtexCoord",
                    GLSLAttrib.SIZE_FLOAT, 3);

            vbo = new VBO(gl, vAttrib, nAttrib, tAttrib);
        }
        initialized = true;
    }

    public void delete(GL3 gl) {
        vertices = null;
        normals = null;
        texCoords = null;

        if (initialized) {
            vbo.delete(gl);
        }
    }

    public VBO getVBO() {
        return vbo;
    }

    public int getNumVertices() {
        return numVertices;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material newMaterial) {
        material = newMaterial;
    }

    public void draw(GL3 gl, Program program) {
        vbo.bind(gl);

        program.linkAttribs(gl, vbo.getAttribs());

        if (format == vertex_format.TRIANGLES) {
            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
        } else if (format == vertex_format.POINTS) {
            gl.glDrawArrays(GL3.GL_POINTS, 0, numVertices);
        } else if (format == vertex_format.LINES) {
            gl.glDrawArrays(GL3.GL_LINES, 0, numVertices);
        }
    }
}
