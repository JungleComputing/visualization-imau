package nl.esciencecenter.visualization.esalsa.unused;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.visualization.openglCommon.datastructures.Material;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;
import nl.esciencecenter.visualization.openglCommon.math.VecF4;
import nl.esciencecenter.visualization.openglCommon.math.VectorFMath;
import nl.esciencecenter.visualization.openglCommon.models.Model;
import nl.esciencecenter.visualization.openglCommon.util.Settings;

public class GeoSphereCutEdge extends Model {
    Settings          settings = Settings.getInstance();

    private final int latRibs;

    public GeoSphereCutEdge(Material material, int latRibs, float radius) {
        super(material, vertex_format.TRIANGLES);
        this.latRibs = latRibs;

        List<VecF4> points4List = new ArrayList<VecF4>();
        List<VecF3> normals3List = new ArrayList<VecF3>();
        List<VecF3> tCoords3List = new ArrayList<VecF3>();

        makeVertices(points4List, normals3List, tCoords3List, radius,
                (radius * 0.75f));

        numVertices = points4List.size();

        vertices = VectorFMath.vec4ListToBuffer(points4List);
        normals = VectorFMath.vec3ListToBuffer(normals3List);
        texCoords = VectorFMath.vec3ListToBuffer(tCoords3List);
    }

    private void makeVertices(List<VecF4> pointsList, List<VecF3> normalsList,
            List<VecF3> tCoords3List, float fullRadius, float cutRadius) {

        float latAngle = (float) ((Math.PI) / latRibs);

        for (int lat = 0; lat < latRibs; lat++) {
            float flat = lat;
            float flatribs = latRibs;

            float startLatAngle = latAngle * lat;
            float stopLatAngle = latAngle * (lat + 1);

            float x00 = (float) Math.sin(startLatAngle);
            float x10 = (float) Math.sin(stopLatAngle);
            float x01 = (float) Math.sin(startLatAngle);
            float x11 = (float) Math.sin(stopLatAngle);

            float y00 = (float) (Math.cos(startLatAngle));
            float y10 = (float) (Math.cos(stopLatAngle));
            float y01 = (float) (Math.cos(startLatAngle));
            float y11 = (float) (Math.cos(stopLatAngle));

            float z00 = 0f;
            float z10 = 0f;
            float z01 = 0f;
            float z11 = 0f;

            pointsList
                    .add(new VecF4(new VecF3(x00, y00, z00).mul(cutRadius), 1));
            pointsList.add(new VecF4(new VecF3(x01, y01, z01).mul(fullRadius),
                    1));
            pointsList.add(new VecF4(new VecF3(x11, y11, z11).mul(fullRadius),
                    1));

            normalsList.add(VectorFMath.normalize(new VecF3(x00, y00, z00)));
            normalsList.add(VectorFMath.normalize(new VecF3(x01, y01, z01)));
            normalsList.add(VectorFMath.normalize(new VecF3(x11, y11, z11)));

            tCoords3List.add(new VecF3((flat / flatribs), 1, 0));
            tCoords3List.add(new VecF3((flat / flatribs), 0, 0));
            tCoords3List.add(new VecF3(((flat + 1) / flatribs), 0, 0));

            pointsList
                    .add(new VecF4(new VecF3(x00, y00, z00).mul(cutRadius), 1));
            pointsList.add(new VecF4(new VecF3(x11, y11, z11).mul(fullRadius),
                    1));
            pointsList
                    .add(new VecF4(new VecF3(x10, y10, z10).mul(cutRadius), 1));

            normalsList.add(VectorFMath.normalize(new VecF3(x00, y00, z00)));
            normalsList.add(VectorFMath.normalize(new VecF3(x11, y11, z11)));
            normalsList.add(VectorFMath.normalize(new VecF3(x10, y10, z10)));

            tCoords3List.add(new VecF3(((flat / flatribs)), 1, 0));
            tCoords3List.add(new VecF3(((flat + 1) / flatribs), 0, 0));
            tCoords3List.add(new VecF3(((flat + 1) / flatribs), 1, 0));

        }
    }

    public int getNumlatRibs() {
        return latRibs;
    }
}
