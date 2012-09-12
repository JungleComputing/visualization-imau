package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
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
    private final ImauSettings settings = ImauSettings.getInstance();

    private int selectedDepth = settings.getDepthDef();

    private final int frameNumber;

    private boolean initialized;

    private boolean error;
    private String errMessage;

    private int pixels, width, height;
    private TGridPoint[] tGridPoints;

    private final File file;

    float latMin, latMax;

    HashMap<GlobeState, FloatBuffer> preparedGlobeImages;
    HashMap<GlobeState, FloatBuffer> preparedLegendImages;

    HashMap<Integer, HDRTexture2D> displayedImages;
    HashMap<Integer, GlobeState> displayedImageStates;

    public NetCDFFrame(File ncFile) {
        this.file = ncFile;
        this.frameNumber = NetCDFDatasetManager.getIndexOfFrameNumber(NetCDFUtil.getFrameNumber(ncFile));

        this.initialized = false;

        this.error = false;
        this.errMessage = "";

        this.preparedLegendImages = new HashMap<GlobeState, FloatBuffer>();
        this.preparedGlobeImages = new HashMap<GlobeState, FloatBuffer>();

        this.displayedImages = new HashMap<Integer, HDRTexture2D>();
        this.displayedImageStates = new HashMap<Integer, GlobeState>();
    }

    public int getNumber() {
        return frameNumber;
    }

    public synchronized void init() {
        if (!initialized) {
            try {
                // Open the correct file as a NetCDF specific file.
                NetcdfFile ncfile = NetCDFUtil.open(file);

                // Read data
                Array t_lat = NetCDFUtil.getData(ncfile, "t_lat");
                Array t_lon = NetCDFUtil.getData(ncfile, "t_lon");
                int tlat_dim = 0;
                int tlon_dim = 0;

                Array t_depth = NetCDFUtil.getData(ncfile, "depth_t");

                List<Dimension> dims = ncfile.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0) {
                        tlat_dim = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0) {
                        tlon_dim = d.getLength();
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

                Variable vuvel = ncfile.findVariable("UVEL");
                Variable vvvel = ncfile.findVariable("VVEL");
                Variable vke = ncfile.findVariable("KE");

                Variable vpd = ncfile.findVariable("PD");
                Variable vtaux = ncfile.findVariable("TAUX");
                Variable vtauy = ncfile.findVariable("TAUY");
                Variable vh2 = ncfile.findVariable("H2");

                Array ssh = vssh.read();
                Array shf = vshf.read();
                Array sfwf = vsfwf.read();
                Array hmxl = vhmxl.read();

                Array taux = vtaux.read();
                Array tauy = vtauy.read();
                Array h2 = vh2.read();

                this.pixels = tlat_dim * tlon_dim;

                tGridPoints = new TGridPoint[pixels];

                int[] origin = new int[] { 0, 0, 0 };
                int[] size = new int[] { 1, tlat_dim, tlon_dim };

                int tdepth_i = selectedDepth;
                float tdepth = t_depth.getFloat(tdepth_i);

                origin[0] = tdepth_i;
                Array salt = vsalt.read(origin, size).reduce(0);
                Array temp = vtemp.read(origin, size).reduce(0);

                Array pd = vpd.read(origin, size).reduce(0);
                Array uvel = vuvel.read(origin, size).reduce(0);
                Array vvel = vvvel.read(origin, size).reduce(0);
                Array ke = vke.read(origin, size).reduce(0);

                Index ssh_index = ssh.getIndex();
                Index shf_index = shf.getIndex();
                Index sfwf_index = sfwf.getIndex();
                Index hmxl_index = hmxl.getIndex();

                Index salt_index = salt.getIndex();
                Index temp_index = temp.getIndex();

                Index taux_index = taux.getIndex();
                Index tauy_index = tauy.getIndex();
                Index h2_index = h2.getIndex();

                Index pd_index = pd.getIndex();
                Index uvel_index = uvel.getIndex();
                Index vvel_index = vvel.getIndex();
                Index ke_index = ke.getIndex();

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

                        taux_index.set(tlat_i, tlon_i);
                        tauy_index.set(tlat_i, tlon_i);
                        h2_index.set(tlat_i, tlon_i);

                        pd_index.set(tlat_i, tlon_i);
                        uvel_index.set(tlat_i, tlon_i);
                        vvel_index.set(tlat_i, tlon_i);
                        ke_index.set(tlat_i, tlon_i);

                        float seaHeight = ssh.getFloat(ssh_index);
                        float surfHeatFlux = shf.getFloat(shf_index);
                        float saltFlux = sfwf.getFloat(sfwf_index);
                        float mixedLayerDepth = hmxl.getFloat(hmxl_index);

                        float salinity = salt.getFloat(salt_index);
                        float temperature = temp.getFloat(temp_index);

                        float windstressX = taux.getFloat(taux_index);
                        float windstressY = tauy.getFloat(tauy_index);
                        float seaHeight2 = h2.getFloat(h2_index);

                        float potentialDensity = pd.getFloat(pd_index);
                        float velocityX = uvel.getFloat(uvel_index);
                        float velocityY = vvel.getFloat(vvel_index);
                        float horzKineticEnergy = ke.getFloat(ke_index);

                        tGridPoints[(tlat_i * tlon_dim) + tlon_i] = new TGridPoint(tlat, tlon, tdepth, seaHeight,
                                surfHeatFlux, saltFlux, mixedLayerDepth, salinity, temperature, velocityX, velocityY,
                                horzKineticEnergy, potentialDensity, windstressX, windstressY, seaHeight2);
                    }
                }

                NetCDFUtil.close(ncfile);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            }

            initialized = true;
        }
    }

    public HDRTexture2D getLegendImage(GL3 gl, int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err.println("ERROR: Request for frame nr " + state.getFrameNumber() + " to NetCDFFrame "
                    + frameNumber);
            // new Exception().printStackTrace(System.err);
            return null;
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit) && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedLegendImages.containsKey(state)) {
                FloatBuffer fb = preparedLegendImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, 1, 500);
            } else {
                tex = ImageMaker.getLegendImage(gl, glMultitexUnit, tGridPoints, state, 1, 500, true);
                preparedLegendImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public HDRTexture2D getLegendImage(GL3 gl, NetCDFFrame otherFrame, int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err.println("ERROR: Request for frame nr " + state.getFrameNumber() + " to NetCDFFrame "
                    + frameNumber);
            // new Exception().printStackTrace(System.err);
            return null;
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit) && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedLegendImages.containsKey(state)) {
                FloatBuffer fb = preparedLegendImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, 1, 500);
            } else {
                tex = ImageMaker.getLegendImage(gl, glMultitexUnit, tGridPoints, otherFrame.getGridPoints(), state, 1,
                        500, true);
                preparedLegendImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public HDRTexture2D getImage(GL3 gl, int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err.println("ERROR: Request for frame nr " + state.getFrameNumber() + " to NetCDFFrame "
                    + frameNumber);
            // new Exception().printStackTrace(System.err);
            return null;
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

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit) && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedGlobeImages.containsKey(state)) {
                FloatBuffer fb = preparedGlobeImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, width, newHeight);
            } else {
                tex = ImageMaker.getImage(gl, glMultitexUnit, tGridPoints, state, width, height, newHeight, blankRows);
                preparedGlobeImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public HDRTexture2D getImage(GL3 gl, NetCDFFrame otherFrame, int glMultitexUnit, GlobeState state) {
        if (state.getFrameNumber() != frameNumber) {
            System.err.println("ERROR: Request for frame nr " + state.getFrameNumber() + " to NetCDFFrame "
                    + frameNumber);
            // new Exception().printStackTrace(System.err);
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

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit) && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedGlobeImages.containsKey(state)) {
                FloatBuffer fb = preparedGlobeImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, width, newHeight);
            } else {
                tex = ImageMaker.getImage(gl, glMultitexUnit, tGridPoints, otherFrame.getGridPoints(), state, width,
                        height, newHeight, blankRows);
                preparedGlobeImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
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
