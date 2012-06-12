package imau.visualization.adaptor;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import openglCommon.math.VecF3;
import openglCommon.math.VectorFMath;
import openglCommon.models.GeoSphere;
import ucar.ma2.Array;

public class GeoSphereCoords {
    HashMap<Integer, Float> latCoords;

    float latMin, latMax;

    FloatBuffer newTexCoords;

    public GeoSphereCoords(GeoSphere sphere, int tlat_dim, Array t_lat) {
        latMin = t_lat.getFloat(0);
        latMax = t_lat.getFloat(tlat_dim - 1);

        int latRibs = sphere.getNumlatRibs();
        int lonRibs = sphere.getNumlonRibs();

        latCoords = new HashMap<Integer, Float>();
        for (int tlat_i = 0; tlat_i < tlat_dim; tlat_i++) {
            float transformedTexCoord = (t_lat.getFloat(tlat_i) + 90f) / 180f;
            // System.out.println(transformedTexCoord);
            latCoords.put(tlat_i, transformedTexCoord);
        }

        List<VecF3> tCoords3List = new ArrayList<VecF3>();

        float[] latCoords = getLatTexCoords(latRibs);

        for (int lon = 0; lon < lonRibs; lon++) {
            for (int lat = 0; lat < latRibs; lat++) {
                float flon0 = (float) (lon) / (float) (lonRibs);
                float flon1 = (float) (lon + 1) / (float) (lonRibs);
                float flat0 = latCoords[lat];

                float flat1;
                if (lat < latRibs - 1) {
                    flat1 = latCoords[lat + 1];
                } else {
                    flat1 = 1f;
                }

                tCoords3List.add(new VecF3(flon0, flat0, 0));
                tCoords3List.add(new VecF3(flon0, flat1, 0));
                tCoords3List.add(new VecF3(flon1, flat1, 0));

                tCoords3List.add(new VecF3(flon0, flat0, 0));
                tCoords3List.add(new VecF3(flon1, flat1, 0));
                tCoords3List.add(new VecF3(flon1, flat0, 0));
            }
        }

        newTexCoords = VectorFMath.vec3ListToBuffer(tCoords3List);
    }

    public float[] getLatTexCoords(int ribs) {
        float[] result = new float[ribs];

        for (int rib = 0; rib < ribs; rib++) {
            float rib_latCoord = rib * (180f / ribs);

            if (rib_latCoord < latMax && rib_latCoord > latMin) {
                int key = getFlooredCoord(rib_latCoord);
                result[rib] = latCoords.get(key);
            } else {
                result[rib] = -1f;
            }
        }

        return result;
    }

    private int getFlooredCoord(float realCoord) {
        int highest = 0;

        for (Map.Entry<Integer, Float> entry : latCoords.entrySet()) {
            int texIntCoord = entry.getKey();
            float texLatCoord = entry.getValue();

            if (texLatCoord < realCoord) {
                if (texIntCoord > highest) {
                    highest = texIntCoord;
                }
            }
        }

        return highest;
    }

    public FloatBuffer getNewTexCoords() {
        return newTexCoords;
    }

}
