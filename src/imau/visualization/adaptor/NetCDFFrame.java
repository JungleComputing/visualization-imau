package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private int frameNumber;

    private boolean initialized, doneProcessing;

    private boolean error;
    private String errMessage;

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

                int tlat_dim = 0;
                int tlon_dim = 0;
                int tdepth_dim = 0;
                int ulat_dim = 0;
                int ulon_dim = 0;

                List<Dimension> dims = ncfile.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0) {
                        tlat_dim = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0) {
                        tlon_dim = d.getLength();
                    } else if (d.getName().compareTo("depth_t") == 0) {
                        tdepth_dim = d.getLength();
                    } else if (d.getName().compareTo("u_lat") == 0) {
                        tlat_dim = d.getLength();
                    } else if (d.getName().compareTo("u_lon") == 0) {
                        tlon_dim = d.getLength();
                    }
                }

                Variable vsalt = ncfile.findVariable("SALT");
                Variable vtemp = ncfile.findVariable("TEMP");
                Variable vssh = ncfile.findVariable("SSH");

                Array ssh = vssh.read();

                int[] origin = new int[] { 0, 0, 0 };
                int[] size = new int[] { 1, tlat_dim, tlon_dim };
                for (int tdepth_i = 0; tdepth_i < tdepth_dim; tdepth_i++) {
                    float tdepth = t_depth.getFloat(tdepth_i);

                    origin[0] = tdepth_i;
                    Array salt = vsalt.read(origin, size).reduce(0);
                    Array temp = vtemp.read(origin, size).reduce(0);

                    Index index = salt.getIndex();

                    for (int tlat_i = 0; tlat_i < tlat_dim; tlat_i++) {
                        float tlat = t_lat.getFloat(tlat_i);

                        for (int tlon_i = 0; tlon_i < tlon_dim; tlon_i++) {
                            float tlon = t_lat.getFloat(tlon_i);

                            index.set(tlat_i, tlon_i);

                            float height = ssh.getFloat(index);
                            float salinity = salt.getFloat(index);
                            float temperature = temp.getFloat(index);

                            TGridPoint sm = new TGridPoint(tlat, tlon, tdepth, height, salinity, temperature);
                            System.out.println(sm);
                        }
                    }
                }

                // System.out.println("grid size check: " + salt.getSize());

                // for (int tlat_i = 0; tlat_i < tlat_dim; tlat_i++) {
                // for (int tlon_i = 0; tlon_i < tlon_dim; tlon_i++) {
                //
                // }
                // }

                // for (int i = 0; i < 10; i++) {
                //
                // double tlat = t_lat.getDouble(i);
                // double tlon = t_lon.getDouble(i);
                //
                // double tdepth = t_depth.getDouble(i);
                //
                // double ulat = u_lat.getDouble(i);
                // double ulon = u_lon.getDouble(i);
                //
                // double sal = salt.getDouble(i);
                // double vx = uvel.getDouble(i);
                // double vy = vvel.getDouble(i);
                //
                // GridPoint gp = new GridPoint(tlat, tlon, tdepth, ulat, ulon,
                // sal, vx, vy);
                //
                // System.out.println("T Lat: " + gp.t_lat);
                // System.out.println("T Lon: " + gp.t_lon);
                // // System.out.println("U Lat: " + ulat);
                // // System.out.println("U Lon: " + ulon);
                // }

                // Close the file
                NetCDFUtil.close(ncfile);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            } catch (InvalidRangeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public synchronized void process() {
        if (!initialized) {
            init();
        }
        doneProcessing = true;
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
