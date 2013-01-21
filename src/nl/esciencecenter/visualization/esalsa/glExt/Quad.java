package nl.esciencecenter.visualization.esalsa.glExt;

import nl.esciencecenter.visualization.openglCommon.datastructures.Material;
import nl.esciencecenter.visualization.openglCommon.math.Point4;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;
import nl.esciencecenter.visualization.openglCommon.math.VectorFMath;

public class Quad extends Model {
    float width;
    float height;

    public Quad(Material material, float height, float width, VecF3 center) {
        super(material, vertex_format.TRIANGLES);

        Point4[] vertices = makeVertices(height, width, center);

        int numVertices = 6;

        Point4[] points = new Point4[numVertices];
        VecF3[] normals = new VecF3[numVertices];
        VecF3[] tCoords = new VecF3[numVertices];

        int arrayindex = 0;
        for (int i = arrayindex; i < arrayindex + 6; i++) {
            normals[i] = new VecF3(0, 0, -1);
        }

        arrayindex = newQuad(points, arrayindex, vertices, tCoords, 1, 0, 3, 2); // FRONT

        this.numVertices = numVertices;
        this.vertices = VectorFMath.toBuffer(points);
        this.normals = VectorFMath.toBuffer(normals);
        this.texCoords = VectorFMath.toBuffer(tCoords);
    }

    private Point4[] makeVertices(float height, float width, VecF3 center) {
        float x = center.get(0);
        float y = center.get(1);

        float xpos = x + width / 2f;
        float xneg = x - width / 2f;
        float ypos = y + height / 2f;
        float yneg = y - height / 2f;

        Point4[] result = new Point4[] { new Point4(xneg, yneg, 0.0f, 1.0f),
                new Point4(xneg, ypos, 0.0f, 1.0f),
                new Point4(xpos, ypos, 0.0f, 1.0f),
                new Point4(xpos, yneg, 0.0f, 1.0f) };

        return result;
    }

    private int newQuad(Point4[] points, int arrayindex, Point4[] source,
            VecF3[] tCoords, int a, int b, int c, int d) {
        points[arrayindex] = source[a];
        tCoords[arrayindex] = new VecF3(0, 0, 0);
        arrayindex++;
        points[arrayindex] = source[b];
        tCoords[arrayindex] = new VecF3(0, 1, 0);
        arrayindex++;
        points[arrayindex] = source[c];
        tCoords[arrayindex] = new VecF3(1, 1, 0);
        arrayindex++;
        points[arrayindex] = source[a];
        tCoords[arrayindex] = new VecF3(0, 0, 0);
        arrayindex++;
        points[arrayindex] = source[c];
        tCoords[arrayindex] = new VecF3(1, 1, 0);
        arrayindex++;
        points[arrayindex] = source[d];
        tCoords[arrayindex] = new VecF3(1, 0, 0);
        arrayindex++;

        return arrayindex;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
