package nl.esciencecenter.visualization.esalsa.unused;

import java.util.ArrayList;
import java.util.List;

import openglCommon.datastructures.Material;
import openglCommon.math.VecF3;
import openglCommon.math.VecF4;
import openglCommon.math.VectorFMath;
import openglCommon.models.Model;
import openglCommon.util.Settings;

public class GeoSphereCut extends Model {
    Settings        settings      = Settings.getInstance();

    private boolean texCoordsIn3D = false;

    private final int latRibs, lonRibs;

    public GeoSphereCut(Material material, int latRibs, int lonRibs,
            float radius, boolean texCoordsIn3D) {
        super(material, vertex_format.TRIANGLES);
        this.texCoordsIn3D = texCoordsIn3D;
        this.latRibs = latRibs;
        this.lonRibs = lonRibs;

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
        float lonAngle = (float) ((2 * Math.PI) / lonRibs);
        float latAngle = (float) ((Math.PI) / latRibs);

        for (int lon = 0; lon < lonRibs; lon++) {
            float flon = lon;
            float flonribs = lonRibs;

            float startLonAngle = lonAngle * lon;
            float stopLonAngle = lonAngle * (lon + 1);

            float startRadius = fullRadius - (flon / flonribs)
                    * (fullRadius - cutRadius);
            float stopRadius = fullRadius - ((flon + 1) / flonribs)
                    * (fullRadius - cutRadius);

            for (int lat = 0; lat < latRibs; lat++) {
                float flat = lat;
                float flatribs = latRibs;

                float startLatAngle = latAngle * lat;
                float stopLatAngle = latAngle * (lat + 1);

                float x00 = (float) (Math.sin(startLatAngle) * Math
                        .cos(startLonAngle));
                float x10 = (float) (Math.sin(stopLatAngle) * Math
                        .cos(startLonAngle));
                float x01 = (float) (Math.sin(startLatAngle) * Math
                        .cos(stopLonAngle));
                float x11 = (float) (Math.sin(stopLatAngle) * Math
                        .cos(stopLonAngle));

                float y00 = (float) (Math.cos(startLatAngle));
                float y10 = (float) (Math.cos(stopLatAngle));
                float y01 = (float) (Math.cos(startLatAngle));
                float y11 = (float) (Math.cos(stopLatAngle));

                float z00 = (float) (Math.sin(startLatAngle) * Math
                        .sin(startLonAngle));
                float z10 = (float) (Math.sin(stopLatAngle) * Math
                        .sin(startLonAngle));
                float z01 = (float) (Math.sin(startLatAngle) * Math
                        .sin(stopLonAngle));
                float z11 = (float) (Math.sin(stopLatAngle) * Math
                        .sin(stopLonAngle));

                pointsList.add(new VecF4(new VecF3(x00, y00, z00)
                        .mul(startRadius), 1));
                pointsList.add(new VecF4(new VecF3(x01, y01, z01)
                        .mul(stopRadius), 1));
                pointsList.add(new VecF4(new VecF3(x11, y11, z11)
                        .mul(stopRadius), 1));

                normalsList
                        .add(VectorFMath.normalize(new VecF3(x00, y00, z00)));
                normalsList
                        .add(VectorFMath.normalize(new VecF3(x01, y01, z01)));
                normalsList
                        .add(VectorFMath.normalize(new VecF3(x11, y11, z11)));

                if (texCoordsIn3D) {
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x00
                            * startRadius / 2 * startRadius), (y00
                            * startRadius / 2 * startRadius), (z00
                            * startRadius / 2 * startRadius))));
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x01
                            * stopRadius / 2 * stopRadius), (y01 * stopRadius
                            / 2 * stopRadius),
                            (z01 * stopRadius / 2 * stopRadius))));
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x11
                            * stopRadius / 2 * stopRadius), (y11 * stopRadius
                            / 2 * stopRadius),
                            (z11 * stopRadius / 2 * stopRadius))));
                } else {
                    tCoords3List.add(new VecF3(1 - (flon / flonribs),
                            (flat / flatribs), 0));
                    tCoords3List.add(new VecF3(1 - ((flon + 1) / flonribs),
                            (flat / flatribs), 0));
                    tCoords3List.add(new VecF3(1 - ((flon + 1) / flonribs),
                            ((flat + 1) / flatribs), 0));
                }

                pointsList.add(new VecF4(new VecF3(x00, y00, z00)
                        .mul(startRadius), 1));
                pointsList.add(new VecF4(new VecF3(x11, y11, z11)
                        .mul(stopRadius), 1));
                pointsList.add(new VecF4(new VecF3(x10, y10, z10)
                        .mul(startRadius), 1));

                normalsList
                        .add(VectorFMath.normalize(new VecF3(x00, y00, z00)));
                normalsList
                        .add(VectorFMath.normalize(new VecF3(x11, y11, z11)));
                normalsList
                        .add(VectorFMath.normalize(new VecF3(x10, y10, z10)));

                if (texCoordsIn3D) {
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x00
                            * startRadius / 2 * startRadius), (y00
                            * startRadius / 2 * startRadius), (z00
                            * startRadius / 2 * startRadius))));
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x11
                            * stopRadius / 2 * stopRadius), (y11 * stopRadius
                            / 2 * stopRadius),
                            (z11 * stopRadius / 2 * stopRadius))));
                    tCoords3List.add(VectorFMath.normalize(new VecF3((x10
                            * startRadius / 2 * startRadius), (y10
                            * startRadius / 2 * startRadius), (z10
                            * startRadius / 2 * startRadius))));
                } else {
                    tCoords3List.add(new VecF3(1 - (flon / flonribs),
                            ((flat / flatribs)), 0));
                    tCoords3List.add(new VecF3(1 - ((flon + 1) / flonribs),
                            ((flat + 1) / flatribs), 0));
                    tCoords3List.add(new VecF3(1 - (flon / flonribs),
                            ((flat + 1) / flatribs), 0));
                }
            }
        }
    }

    public int getNumlatRibs() {
        return latRibs;
    }

    public int getNumlonRibs() {
        return lonRibs;
    }
}
