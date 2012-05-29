package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.media.opengl.GL3;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private ImauSettings settings = ImauSettings.getInstance();

    private int frameNumber;

    private boolean initialized, doneProcessing;

    private boolean error;
    private String errMessage;

    private int pixels, width, height;
    private TGridPoint[] tGridPoints;
    private NetCDFTexture image;

    private File initialFile;

    public NetCDFFrame(int frameNumber, File initialFile) {
        this.frameNumber = frameNumber;
        this.initialFile = initialFile;

        this.initialized = false;
        this.doneProcessing = false;

        this.error = false;
        this.errMessage = "";
    }

    public int getNumber() {
        return frameNumber;
    }

    public synchronized void init() {
        if (!initialized) {
            try {
                // Open the correct file as a NetCDF specific file.
                File myFile = NetCDFUtil.getFile(initialFile, frameNumber);
                NetcdfFile ncfile = NetCDFUtil.open(myFile);

                // Read data
                Array t_lat = NetCDFUtil.getData(ncfile, "t_lat");
                Array t_lon = NetCDFUtil.getData(ncfile, "t_lon");

                Array t_depth = NetCDFUtil.getData(ncfile, "depth_t");

                Array u_lat = NetCDFUtil.getData(ncfile, "u_lat");
                Array u_lon = NetCDFUtil.getData(ncfile, "u_lon");

                int width = 0;
                int height = 0;
                int tdepth_dim = 0;
                int ulat_dim = 0;
                int ulon_dim = 0;

                List<Dimension> dims = ncfile.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0) {
                        width = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0) {
                        height = d.getLength();
                    } else if (d.getName().compareTo("depth_t") == 0) {
                        tdepth_dim = d.getLength();
                    } else if (d.getName().compareTo("u_lat") == 0) {
                        width = d.getLength();
                    } else if (d.getName().compareTo("u_lon") == 0) {
                        height = d.getLength();
                    }
                }

                Variable vsalt = ncfile.findVariable("SALT");
                Variable vtemp = ncfile.findVariable("TEMP");
                Variable vssh = ncfile.findVariable("SSH");

                Array ssh = vssh.read();

                this.pixels = width * height;
                tGridPoints = new TGridPoint[pixels];

                int[] origin = new int[] { 0, 0, 0 };
                int[] size = new int[] { 1, width, height };
                for (int tdepth_i = 0; tdepth_i < tdepth_dim; tdepth_i++) {

                    float tdepth = t_depth.getFloat(tdepth_i);

                    origin[0] = tdepth_i;
                    Array salt = vsalt.read(origin, size).reduce(0);
                    Array temp = vtemp.read(origin, size).reduce(0);

                    Index index = salt.getIndex();

                    for (int tlat_i = 0; tlat_i < width; tlat_i++) {
                        float tlat = t_lat.getFloat(tlat_i);

                        for (int tlon_i = 0; tlon_i < height; tlon_i++) {
                            float tlon = t_lon.getFloat(tlon_i);

                            index.set(tlat_i, tlon_i);

                            float seaHeight = ssh.getFloat(index);
                            float salinity = salt.getFloat(index);
                            float temperature = temp.getFloat(index);

                            tGridPoints[(tlat_i * height) + tlon_i] = new TGridPoint(tlat, tlon, tdepth, seaHeight,
                                    salinity, temperature);
                            // System.out.println(tGridPoints[(tlat_i *
                            // tlat_dim) + tlon_i]);
                        }
                    }
                }

                NetCDFUtil.close(ncfile);

                initialized = true;
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
                e.printStackTrace();
            } catch (InvalidRangeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public NetCDFTexture getImage() {
        if (!doneProcessing) {
            process();
        }

        return image;
    }

    public synchronized void process() {
        if (!initialized) {
            init();
        }

        if (!doneProcessing) {
            ByteBuffer outBuf = ByteBuffer.allocate(pixels * 4);
            outBuf.clear();
            outBuf.rewind();

            float sal_max = -1;
            for (int i = 0; i < pixels; i++) {
                float sal = tGridPoints[i].salinity;
                if (sal > sal_max) {
                    sal_max = sal;
                }
                if (sal < 0 && sal > -1.0E33) {
                    System.out.println("Faulty value? : " + sal);
                }
            }

            System.out.println("Sal max: " + sal_max);

            for (int i = 0; i < pixels; i++) {
                outBuf.put((byte) 0xFF); // (tGridPoints[i].salinity / sal_max *
                                         // 255));
                outBuf.put((byte) (tGridPoints[i].salinity / sal_max * 255));
                outBuf.put((byte) (tGridPoints[i].salinity / sal_max * 255));
                outBuf.put((byte) 0xFF);
            }

            outBuf.rewind();

            this.image = new NetCDFTexture(GL3.GL_TEXTURE10, outBuf, width, height);

            doneProcessing = true;
        }

    }

    @Override
    public void run() {
        init();
        process();
    }

    public boolean isError() {
        return error;
    }

}
