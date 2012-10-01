package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFUtil;

import java.io.IOException;
import java.util.List;

import openglCommon.exceptions.UninitializedException;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFArray implements Runnable {
    protected SurfaceTextureDescription description;

    private final NetcdfFile            ncFile;
    private boolean                     initialized = false;
    private float[]                     data;
    private int                         width;
    private int                         height;

    public NetCDFArray(NetcdfFile frameFile, SurfaceTextureDescription description) throws IOException {
        this.description = description;

        this.ncFile = frameFile;
    }

    @Override
    public void run() {
        if (!initialized) {
            try {
                Variable ncdfVar = ncFile.findVariable(description.getVarName());
                List<Dimension> dims = ncdfVar.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0 || d.getName().compareTo("u_lat") == 0) {
                        height = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0 || d.getName().compareTo("u_lon") == 0) {
                        width = d.getLength();
                    }
                }

                Array ncdfArray2D;
                if (dims.size() > 3) {
                    if (dims.get(0).getLength() == 1 || dims.size() > 4) {
                        // Peel off the time 'dimension'
                        int[] origin = new int[] { 0, description.getDepth(), 0, 0 };
                        int[] size = new int[] { 1, 1, height, width };

                        ncdfArray2D = ncdfVar.read(origin, size).reduce();
                    } else {
                        throw new IOException("Unanticipated NetCDF variable dimensions.");
                    }
                } else if (dims.size() > 2) {
                    // Select the correct the depth
                    int[] origin = new int[] { description.getDepth(), 0, 0 };
                    int[] size = new int[] { 1, height, width };

                    ncdfArray2D = ncdfVar.read(origin, size).reduce();
                } else {
                    ncdfArray2D = ncdfVar.read();
                }

                float[] result = (float[]) ncdfArray2D.get1DJavaArray(float.class);

                data = result;

                NetCDFUtil.close(ncFile);

                initialized = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidRangeException e) {
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

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof GlobeState))
            return false;

        // cast to native object is now safe
        NetCDFArray that = (NetCDFArray) thatObject;

        // now a proper field-by-field evaluation can be made
        return (description.equals(that.description));
    }

    public SurfaceTextureDescription getDescription() {
        return description;
    }
}