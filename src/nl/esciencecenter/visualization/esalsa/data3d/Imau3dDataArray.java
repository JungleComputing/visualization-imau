package nl.esciencecenter.visualization.esalsa.data3d;

import java.io.IOException;
import java.util.List;

import nl.esciencecenter.visualization.esalsa.netcdf.NetCDFUtil;
import openglCommon.exceptions.UninitializedException;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class Imau3dDataArray implements Runnable {
    protected SurfaceTexture3dDescription description;

    private final NetcdfFile              ncFile1;
    private boolean                       initialized = false;
    private float[]                       data;
    private int                           width;
    private int                           height;
    private int                           depth;

    public Imau3dDataArray(NetcdfFile frameFile,
            SurfaceTexture3dDescription description) throws IOException {
        this.description = description;

        this.ncFile1 = frameFile;
    }

    @Override
    public void run() {
        if (!initialized) {
            Variable ncdfVar1 = ncFile1.findVariable(description.getVarName());
            List<Dimension> dims = ncdfVar1.getDimensions();
            for (Dimension d : dims) {
                if (d.getName().compareTo("t_lat") == 0
                        || d.getName().compareTo("u_lat") == 0) {
                    height = d.getLength();
                } else if (d.getName().compareTo("t_lon") == 0
                        || d.getName().compareTo("u_lon") == 0) {
                    width = d.getLength();
                } else if (d.getName().compareTo("depth_t") == 0
                        || d.getName().compareTo("depth_u") == 0) {
                    depth = d.getLength();
                }
            }

            try {
                Array ncdfArray3D;
                ncdfArray3D = ncdfVar1.read();

                float[] result = (float[]) ncdfArray3D
                        .get1DJavaArray(float.class);

                data = result;

                NetCDFUtil.close(ncFile1);

                initialized = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public float[] getData() throws UninitializedException {
        if (initialized) {
            return data;
        }

        throw new UninitializedException();
    }

    public int getWidth() throws UninitializedException {
        if (initialized) {
            return width;
        }

        throw new UninitializedException();
    }

    public int getHeight() throws UninitializedException {
        if (initialized) {
            return height;
        }

        throw new UninitializedException();
    }

    public int getDepth() throws UninitializedException {
        if (initialized) {
            return depth;
        }

        throw new UninitializedException();
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof Imau3dDataArray))
            return false;

        // cast to native object is now safe
        Imau3dDataArray that = (Imau3dDataArray) thatObject;

        // now a proper field-by-field evaluation can be made
        return (description.equals(that.description));
    }

    public SurfaceTexture3dDescription getDescription() {
        return description;
    }
}