package nl.esciencecenter.visualization.esalsa.glExt;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL3;

import nl.esciencecenter.visualization.openglCommon.datastructures.GLSLAttrib;
import nl.esciencecenter.visualization.openglCommon.datastructures.Material;
import nl.esciencecenter.visualization.openglCommon.datastructures.VBO;
import nl.esciencecenter.visualization.openglCommon.exceptions.UninitializedException;
import nl.esciencecenter.visualization.openglCommon.math.Color4;
import nl.esciencecenter.visualization.openglCommon.math.MatF4;
import nl.esciencecenter.visualization.openglCommon.math.MatrixFMath;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;
import nl.esciencecenter.visualization.openglCommon.math.VecF4;
import nl.esciencecenter.visualization.openglCommon.math.VectorFMath;
import nl.esciencecenter.visualization.openglCommon.text.GlyphShape;
import nl.esciencecenter.visualization.openglCommon.text.OutlineShape;
import nl.esciencecenter.visualization.openglCommon.text.TypecastFont;

import com.jogamp.graph.geom.Triangle;
import com.jogamp.graph.geom.Vertex;
import com.jogamp.graph.geom.opengl.SVertex;

public class MultiColorText extends Model {
    private boolean                            initialized = false;

    private final HashMap<Integer, GlyphShape> glyphs;
    private final HashMap<Integer, VecF4>      colors;

    FloatBuffer                                vertexColors;

    private final BoundingBox                  bbox;

    private String                             cachedString;
    private final TypecastFont                 font;
    private int                                cachedSize;

    public MultiColorText(Material material, TypecastFont font) {
        super(material, vertex_format.TRIANGLES);

        this.font = font;

        cachedString = "";

        this.bbox = new BoundingBox();
        colors = new HashMap<Integer, VecF4>();
        glyphs = new HashMap<Integer, GlyphShape>();

        numVertices = 0;

        VecF4[] points = new VecF4[numVertices];

        this.vertices = VectorFMath.toBuffer(points);
        this.vertexColors = VectorFMath.toBuffer(points);
    }

    @Override
    public void init(GL3 gl) {
        if (!initialized) {
            GLSLAttrib vAttrib = new GLSLAttrib(vertices, "MCvertex",
                    GLSLAttrib.SIZE_FLOAT, 4);
            GLSLAttrib cAttrib = new GLSLAttrib(vertexColors, "MCvertexColor",
                    GLSLAttrib.SIZE_FLOAT, 4);

            vbo = new VBO(gl, vAttrib, cAttrib);
        }
        initialized = true;
    }

    public void setString(GL3 gl, String str, Color4 basicColor, int size) {
        if (cachedString.compareTo(str) != 0 || cachedSize != size) {
            colors.clear();
            glyphs.clear();

            if (str.compareTo(cachedString) != 0 || cachedSize != size) {
                // Get the outline shapes for the current string in this font
                ArrayList<OutlineShape> shapes = font.getOutlineShapes(str,
                        size, SVertex.factory());

                // Make a set of glyph shapes from the outlines
                int numGlyps = shapes.size();

                for (int index = 0; index < numGlyps; index++) {
                    if (shapes.get(index) == null) {
                        colors.put(index, null);
                        glyphs.put(index, null);
                        continue;
                    }
                    GlyphShape glyphShape = new GlyphShape(SVertex.factory(),
                            shapes.get(index));

                    if (glyphShape.getNumVertices() < 3) {
                        colors.put(index, null);
                        glyphs.put(index, null);
                        continue;
                    }
                    colors.put(index, basicColor);
                    glyphs.put(index, glyphShape);
                }

                makeVBO(gl);
                this.cachedString = str;
                this.cachedSize = size;
            }
        }
    }

    private void makeVBO(GL3 gl) {
        // Create list of vertices based on the glyph shapes
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        ArrayList<VecF4> vertexColors = new ArrayList<VecF4>();
        for (int i = 0; i < glyphs.size(); i++) {
            if (glyphs.get(i) != null) {
                GlyphShape glyph = glyphs.get(i);
                VecF4 glypColor = colors.get(i);

                ArrayList<Triangle> gtris = glyph.triangulate();
                for (Triangle t : gtris) {
                    vertices.add(t.getVertices()[0]);
                    vertices.add(t.getVertices()[1]);
                    vertices.add(t.getVertices()[2]);

                    vertexColors.add(glypColor);
                    vertexColors.add(glypColor);
                    vertexColors.add(glypColor);
                }
            }
        }

        // Transform the vertices from Vertex objects to Vec4 objects
        // and
        // update BoundingBox.
        VecF4[] myVertices = new VecF4[vertices.size()];
        int i = 0;
        for (Vertex v : vertices) {
            VecF3 vec = new VecF3(v.getX(), v.getY(), v.getZ());
            bbox.resize(vec);

            myVertices[i] = new VecF4(vec, 1f);

            i++;
        }

        if (vbo != null) {
            vbo.delete(gl);
        }
        this.vertices = VectorFMath.toBuffer(myVertices);
        this.vertexColors = VectorFMath.vec4ListToBuffer(vertexColors);
        GLSLAttrib vAttrib = new GLSLAttrib(this.vertices, "MCvertex",
                GLSLAttrib.SIZE_FLOAT, 4);
        GLSLAttrib cAttrib = new GLSLAttrib(this.vertexColors, "MCvertexColor",
                GLSLAttrib.SIZE_FLOAT, 4);
        vbo = new VBO(gl, vAttrib, cAttrib);

        this.numVertices = vertices.size();

        initialized = true;
    }

    public void setSubstringColors(GL3 gl, HashMap<String, Color4> map) {
        for (Map.Entry<String, Color4> entry : map.entrySet()) {
            setSubstringColorWordBounded(gl, entry.getKey(), entry.getValue());
        }
    }

    public void setSubstringColorWordBounded(GL3 gl, String subString,
            Color4 newColor) {
        if (cachedString.contains(subString) && subString.compareTo("") != 0) {
            Pattern p = Pattern.compile("\\b" + subString + "\\b");
            Matcher m = p.matcher(cachedString);

            int startIndex = 0;
            while (m.find(startIndex)) {
                startIndex = m.start();
                for (int i = 0; i < subString.length(); i++) {
                    colors.put(startIndex + i, newColor);
                }
                startIndex++; // read past to avoid never-ending loop
            }
        }
    }

    public void setSubstringColor(GL3 gl, String subString, Color4 newColor) {
        if (cachedString.contains(subString) && subString.compareTo("") != 0) {
            int startIndex = cachedString.indexOf(subString);
            while (startIndex > -1) {
                for (int i = 0; i < subString.length(); i++) {
                    colors.put(startIndex + i, newColor);
                }
                startIndex = cachedString.indexOf(subString, startIndex + 1);
            }
        }
    }

    public void setSubstringAtIndexColor(GL3 gl, int startIndex,
            String subString, Color4 newColor) {
        if (cachedString.contains(subString) && subString.compareTo("") != 0) {
            for (int i = 0; i < subString.length(); i++) {
                colors.put(startIndex + i, newColor);
            }
        }
    }

    public void finalizeColorScheme(GL3 gl) {
        makeVBO(gl);
    }

    public void draw(GL3 gl, Program program, float canvasWidth,
            float canvasHeight, float RasterPosX, float RasterPosY) {
        if (initialized) {
            program.setUniformMatrix(
                    "MVMatrix",
                    getMVMatrixForHUD(canvasWidth, canvasHeight, RasterPosX,
                            RasterPosY));
            program.setUniformMatrix("PMatrix",
                    getPMatrixForHUD(canvasWidth, canvasHeight));

            try {
                program.use(gl);
            } catch (UninitializedException e) {
                e.printStackTrace();
            }

            vbo.bind(gl);

            program.linkAttribs(gl, vbo.getAttribs());

            gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVertices);
        }
    }

    @Override
    public String toString() {
        return cachedString;
    }

    private MatF4 getMVMatrixForHUD(float canvasWidth, float canvasHeight,
            float RasterPosX, float RasterPosY) {

        MatF4 MVMatrix = new MatF4();
        MVMatrix = MVMatrix.mul(MatrixFMath.translate(
                (RasterPosX / canvasWidth), (RasterPosY / canvasHeight), 0f));

        return MVMatrix;
    }

    private MatF4 getPMatrixForHUD(float canvasWidth, float canvasHeight) {

        MatF4 PMatrix = MatrixFMath.ortho(0f, canvasWidth, 0f, canvasHeight,
                -1f, 1f);

        return PMatrix;
    }

}
