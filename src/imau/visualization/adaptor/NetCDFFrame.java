package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL3;

import openglCommon.textures.HDRTexture2D;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private final ImauSettings                   settings      = ImauSettings
                                                                       .getInstance();

    private int                                  selectedDepth = settings
                                                                       .getDepthDef();

    private final int                            frameNumber;

    private boolean                              initialized;

    private final boolean                        doneProcessing;

    private boolean                              error;
    private String                               errMessage;

    private int                                  pixels, width, height;
    private TGridPoint[]                         tGridPoints;

    private final File                           initialFile;

    private final float                          epsilon       = settings
                                                                       .getEpsilon();

    float                                        latMin, latMax;

    private final HashMap<Integer, GlobeState>   storedStates;
    private final HashMap<Integer, HDRTexture2D> storedTextures;
    private final HashMap<Integer, HDRTexture2D> storedLegends;

    public NetCDFFrame(int frameNumber, File initialFile) {
        this.frameNumber = frameNumber;
        this.initialFile = initialFile;

        this.initialized = false;
        this.doneProcessing = false;

        this.error = false;
        this.errMessage = "";

        storedStates = new HashMap<Integer, GlobeState>();
        storedTextures = new HashMap<Integer, HDRTexture2D>();
        storedLegends = new HashMap<Integer, HDRTexture2D>();
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
                int tlat_dim = 0;
                int tlon_dim = 0;

                Array t_depth = NetCDFUtil.getData(ncfile, "depth_t");
                int tdepth_dim = 0;

                List<Dimension> dims = ncfile.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0) {
                        tlat_dim = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0) {
                        tlon_dim = d.getLength();
                    } else if (d.getName().compareTo("depth_t") == 0) {
                        tdepth_dim = d.getLength();
                    }
                }

                this.height = tlat_dim;
                this.width = tlon_dim;

                latMin = t_lat.getFloat(0) + 90f;
                latMax = t_lat.getFloat(height - 1) + 90f;

                Variable vssh = ncfile.findVariable("SSH");
                Variable vshf = ncfile.findVariable("SHF");
                Variable vsfwf = ncfile.findVariable("SFWF");
                Variable vhmxl = ncfile.findVariable("HMXL");

                Variable vsalt = ncfile.findVariable("SALT");
                Variable vtemp = ncfile.findVariable("TEMP");

                Array ssh = vssh.read();
                Array shf = vshf.read();
                Array sfwf = vsfwf.read();
                Array hmxl = vhmxl.read();

                this.pixels = tlat_dim * tlon_dim;

                tGridPoints = new TGridPoint[pixels];

                int[] origin = new int[] { 0, 0, 0 };
                int[] size = new int[] { 1, tlat_dim, tlon_dim };

                int tdepth_i = selectedDepth;
                float tdepth = t_depth.getFloat(tdepth_i);

                origin[0] = tdepth_i;
                Array salt = vsalt.read(origin, size).reduce(0);
                Array temp = vtemp.read(origin, size).reduce(0);

                Index ssh_index = ssh.getIndex();
                Index shf_index = shf.getIndex();
                Index sfwf_index = sfwf.getIndex();
                Index hmxl_index = hmxl.getIndex();

                Index salt_index = salt.getIndex();
                Index temp_index = temp.getIndex();

                for (int tlon_i = 0; tlon_i < tlon_dim; tlon_i++) {
                    float tlon = t_lon.getFloat(tlon_i);

                    for (int tlat_i = 0; tlat_i < tlat_dim; tlat_i++) {
                        float tlat = t_lat.getFloat(tlat_i);

                        ssh_index.set(tlat_i, tlon_i);
                        shf_index.set(tlat_i, tlon_i);
                        sfwf_index.set(tlat_i, tlon_i);
                        hmxl_index.set(tlat_i, tlon_i);

                        salt_index.set(tlat_i, tlon_i);
                        temp_index.set(tlat_i, tlon_i);

                        float seaHeight = ssh.getFloat(ssh_index);
                        float surfHeatFlux = shf.getFloat(shf_index);
                        float saltFlux = sfwf.getFloat(sfwf_index);
                        float mixedLayerDepth = hmxl.getFloat(hmxl_index);

                        float salinity = salt.getFloat(salt_index);
                        float temperature = temp.getFloat(temp_index);

                        tGridPoints[(tlat_i * tlon_dim) + tlon_i] = new TGridPoint(
                                tlat, tlon, tdepth, seaHeight, surfHeatFlux,
                                saltFlux, mixedLayerDepth, salinity,
                                temperature);
                    }
                }

                NetCDFUtil.close(ncfile);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            } catch (InvalidRangeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            initialized = true;
        }
    }

    public HDRTexture2D getLegendImage(GL3 gl, int glMultitexUnit,
            GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err
                    .println("ERROR: Request for frame nr "
                            + state.getFrameNumber() + " to NetCDFFrame "
                            + frameNumber);
            System.exit(1);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        // If the state was used already, retrieve the image for re-use
        if (storedStates.containsKey(glMultitexUnit)
                && storedStates.get(glMultitexUnit).equals(state)) {
            return storedLegends.get(glMultitexUnit);
        }

        HDRTexture2D image = ImageMaker.getLegendImage(gl, glMultitexUnit,
                tGridPoints, state, 1, 500, true);

        // Either the state has changed, or the glMultitexUnit was not used
        // before, so change the image.

        storedLegends.put(glMultitexUnit, image);
        storedStates.put(glMultitexUnit, state);

        return image;
    }

    public HDRTexture2D getLegendImage(GL3 gl, NetCDFFrame otherFrame,
            int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err
                    .println("ERROR: Request for frame nr "
                            + state.getFrameNumber() + " to NetCDFFrame "
                            + frameNumber);
            System.exit(1);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        // If the state was used already, retrieve the image for re-use
        if (storedStates.containsKey(glMultitexUnit)
                && storedStates.get(glMultitexUnit).equals(state)) {
            return storedLegends.get(glMultitexUnit);
        }

        // Either the state has changed, or the glMultitexUnit was not used
        // before, so change the image.

        HDRTexture2D image = ImageMaker.getLegendImage(gl, glMultitexUnit,
                tGridPoints, otherFrame.getGridPoints(), state, 1, 500, true);

        storedLegends.put(glMultitexUnit, image);
        storedStates.put(glMultitexUnit, state);

        return image;
    }

    public HDRTexture2D getImage(GL3 gl, int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err
                    .println("ERROR: Request for frame nr "
                            + state.getFrameNumber() + " to NetCDFFrame "
                            + frameNumber);
            System.exit(1);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin)) * height);
        int blankRows = (int) Math.floor(180f / latMax);

        // If the state was used already, retrieve the image for re-use
        if (storedStates.containsKey(glMultitexUnit)
                && storedStates.get(glMultitexUnit).equals(state)) {
            return storedTextures.get(glMultitexUnit);
        }

        // Either the state has changed, or the glMultitexUnit was not used yet,
        // so change the image.

        HDRTexture2D image = ImageMaker.getImage(gl, glMultitexUnit,
                tGridPoints, state, width, height, newHeight, blankRows);
        storedTextures.put(glMultitexUnit, image);
        storedStates.put(glMultitexUnit, state);

        return image;
    }

    public HDRTexture2D getImage(GL3 gl, NetCDFFrame otherFrame,
            int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err
                    .println("ERROR: Request for frame nr "
                            + state.getFrameNumber() + " to NetCDFFrame "
                            + frameNumber);
            System.exit(1);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin)) * height);
        int blankRows = (int) Math.floor(180f / latMax);

        // If the state was used already, retrieve the image for re-use
        if (storedStates.containsKey(glMultitexUnit)
                && storedStates.get(glMultitexUnit).equals(state)) {
            return storedTextures.get(glMultitexUnit);
        }

        // Either the state has changed, or the glMultitexUnit was not used
        // before, so change the image.

        HDRTexture2D image = ImageMaker.getImage(gl, glMultitexUnit,
                tGridPoints, otherFrame.getGridPoints(), state, width, height,
                newHeight, blankRows);
        storedTextures.put(glMultitexUnit, image);
        storedStates.put(glMultitexUnit, state);

        return image;
    }

    private TGridPoint[] getGridPoints() {
        if (!initialized) {
            init();
        }
        return tGridPoints;
    }

    public synchronized void process() {
        if (!initialized) {
            init();
        }
    }

    @Override
    public void run() {
        init();
        process();
    }

    public boolean isError() {
        init();

        return error;
    }

    public String getError() {
        return errMessage;
    }

}
