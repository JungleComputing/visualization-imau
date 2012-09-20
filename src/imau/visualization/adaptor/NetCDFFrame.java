package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFNoSuchVariableException;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL3;

import openglCommon.textures.HDRTexture2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private final ImauSettings                     settings      = ImauSettings
                                                                         .getInstance();
    private final static Logger                    logger        = LoggerFactory
                                                                         .getLogger(NetCDFFrame.class);
    private static final int                       MAX_DEPTH     = 42;

    private int                                    selectedDepth = settings
                                                                         .getDepthDef();

    private final int                              frameNumber;

    private boolean                                initialized;

    private boolean                                error;
    private String                                 errMessage;

    private int                                    pixels, globeTextureWidth,
            globeTextureHeight, globeTextureDepth;
    private TGridPoint[]                           tGridPoints, dGridPoints;

    private final File                             file;

    private float                                  latMin, latMax;

    private final HashMap<GlobeState, FloatBuffer> preparedGlobeImages;
    private final HashMap<GlobeState, FloatBuffer> preparedDepthImages;
    private final HashMap<GlobeState, FloatBuffer> preparedLegendImages;

    private final HashMap<Integer, HDRTexture2D>   displayedImages;
    private final HashMap<Integer, GlobeState>     displayedImageStates;

    private final Variable[]                       variables;

    private NetcdfFile                             ncfile;

    public NetCDFFrame(File ncFile, int indexNumber, Variable[] variables) {
        this.file = ncFile;
        this.frameNumber = indexNumber;

        this.initialized = false;

        this.error = false;
        this.errMessage = "";

        this.preparedLegendImages = new HashMap<GlobeState, FloatBuffer>();
        this.preparedDepthImages = new HashMap<GlobeState, FloatBuffer>();
        this.preparedGlobeImages = new HashMap<GlobeState, FloatBuffer>();

        this.displayedImages = new HashMap<Integer, HDRTexture2D>();
        this.displayedImageStates = new HashMap<Integer, GlobeState>();

        this.variables = variables;
    }

    public int getNumber() {
        return frameNumber;
    }

    public synchronized void init() {
        if (!initialized) {
            try {
                // Open the correct file as a NetCDF specific file.
                this.ncfile = NetCDFUtil.open(file);

                // Read data
                String latName = NetCDFUtil.getUsedDimensionName(ncfile,
                        settings.getLatNamePermutations());
                String lonName = NetCDFUtil.getUsedDimensionName(ncfile,
                        settings.getLonNamePermutations());
                String depthName = NetCDFUtil.getUsedDimensionName(ncfile,
                        settings.getDepthNamePermutations());

                Array t_lat = tryPermutationsArrayOpen(ncfile, latName);
                Array t_lon = tryPermutationsArrayOpen(ncfile, lonName);
                Array t_depth = tryPermutationsArrayOpen(ncfile, depthName);

                // String[] varNames = NetCDFUtil.getVarNames(ncfile, latName,
                // lonName);

                this.globeTextureHeight = tryPermutationsGetLength(ncfile,
                        "t_lat", "nlat");
                this.globeTextureWidth = tryPermutationsGetLength(ncfile,
                        "t_lon", "nlon");
                this.globeTextureDepth = tryPermutationsGetLength(ncfile,
                        "depth_t", "z_t");

                latMin = t_lat.getFloat(0) + 90f;
                latMax = t_lat.getFloat(globeTextureHeight - 1) + 90f;

                tGridPoints = new TGridPoint[globeTextureHeight
                        * globeTextureWidth];

                dGridPoints = new TGridPoint[globeTextureHeight
                        * globeTextureDepth];

                float[] seaHeight = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "SSH");
                float[] surfHeatFlux = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "SHF");
                float[] saltFlux = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "SFWF");
                float[] mixedLayerDepth = efficientOpenSingleDepthVariable(
                        ncfile, selectedDepth, "HMXL");

                float[] salinity = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "SALT");
                float[] temperature = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "TEMP");

                float[] windstressX = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "TAUX");
                float[] windstressY = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "TAUY");
                float[] seaHeight2 = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "H2");

                float[] potentialDensity = efficientOpenSingleDepthVariable(
                        ncfile, selectedDepth, "PD");
                float[] velocityX = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "UVEL");
                float[] velocityY = efficientOpenSingleDepthVariable(ncfile,
                        selectedDepth, "VVEL");
                float[] horzKineticEnergy = efficientOpenSingleDepthVariable(
                        ncfile, selectedDepth, "KE");

                float tdepth = t_depth.getFloat(selectedDepth);

                for (int col = 0; col < globeTextureWidth; col++) {
                    float tlon = t_lon.getFloat(col);
                    for (int row = 0; row < globeTextureHeight; row++) {
                        float tlat = t_lat.getFloat(row);
                        int j = (row * globeTextureWidth) + col;

                        tGridPoints[j] = new TGridPoint(tlat, tlon, tdepth,
                                seaHeight[j], surfHeatFlux[j], saltFlux[j],
                                mixedLayerDepth[j], salinity[j],
                                temperature[j], velocityX[j], velocityY[j],
                                horzKineticEnergy[j], potentialDensity[j],
                                windstressX[j], windstressY[j], seaHeight2[j]);
                    }
                }

                // int selectedLongitude = 0;
                // float tlon = t_lon.getFloat(selectedLongitude);
                //
                // seaHeight = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "SSH");
                // surfHeatFlux = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "SHF");
                // saltFlux = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "SFWF");
                // mixedLayerDepth =
                // efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "HMXL");
                //
                // salinity = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "SALT");
                // temperature = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "TEMP");
                //
                // windstressX = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "TAUX");
                // windstressY = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "TAUY");
                // seaHeight2 = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "H2");
                //
                // potentialDensity =
                // efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "PD");
                // velocityX = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "UVEL");
                // velocityY = efficientOpenSingleLongitudeVariable(ncfile,
                // selectedLongitude, "VVEL");
                // horzKineticEnergy = efficientOpenSingleLongitudeVariable(
                // ncfile, selectedLongitude, "KE");
                //
                // for (int layer = 0; layer < globeTextureDepth; layer++) {
                // tdepth = t_depth.getFloat(layer);
                //
                // for (int row = 0; row < globeTextureHeight; row++) {
                // float tlat = t_lat.getFloat(row);
                //
                // int j = (row * globeTextureWidth) + layer;
                //
                // dGridPoints[j] = new TGridPoint(tlat, tlon, tdepth,
                // seaHeight[j], surfHeatFlux[j], saltFlux[j],
                // mixedLayerDepth[j], salinity[j],
                // temperature[j], velocityX[j], velocityY[j],
                // horzKineticEnergy[j], potentialDensity[j],
                // windstressX[j], windstressY[j], seaHeight2[j]);
                // }
                // }

                NetCDFUtil.close(ncfile);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            } catch (InvalidRangeException e) {
                e.printStackTrace();
            } catch (NetCDFNoSuchVariableException e2) {
                logger.error(e2.getMessage());
            }

            initialized = true;
        }
    }

    private float[] efficientOpenSingleDepthVariable(NetcdfFile ncfile,
            int selectedDepth, String varName) throws IOException,
            InvalidRangeException {
        float[] result = new float[globeTextureWidth * globeTextureHeight];

        Variable ncdfVar = ncfile.findVariable(varName);
        List<Dimension> dims = ncdfVar.getDimensions();

        Array ncdfArray2D;
        if (dims.size() > 3) {
            if (dims.get(0).getLength() == 1 || dims.size() > 4) {
                // Peel off the time 'dimension'
                int[] origin = new int[] { 0, selectedDepth, 0, 0 };
                int[] size = new int[] { 1, 1, globeTextureHeight,
                        globeTextureWidth };

                ncdfArray2D = ncdfVar.read(origin, size).reduce();
            } else {
                throw new IOException(
                        "Unanticipated NetCDF variable dimensions.");
            }
        } else if (dims.size() > 2) {
            // Select the correct the depth
            int[] origin = new int[] { selectedDepth, 0, 0 };
            int[] size = new int[] { 1, globeTextureHeight, globeTextureWidth };

            ncdfArray2D = ncdfVar.read(origin, size).reduce();
        } else {
            ncdfArray2D = ncdfVar.read();
        }

        result = (float[]) ncdfArray2D.get1DJavaArray(float.class);

        return result;
    }

    private float[] efficientOpenSingleLongitudeVariable(NetcdfFile ncfile,
            int selectedLongitude, String varName) throws IOException,
            InvalidRangeException {
        float[] result = new float[globeTextureWidth * globeTextureHeight];

        Variable ncdfVar = ncfile.findVariable(varName);
        List<Dimension> dims = ncdfVar.getDimensions();

        Array ncdfArray2D;
        if (dims.size() > 3) {
            if (dims.get(0).getLength() == 1 || dims.size() > 4) {
                // Peel off the time 'dimension'
                int[] origin = new int[] { 0, 0, selectedLongitude, 0 };
                int[] size = new int[] { 1, globeTextureDepth,
                        globeTextureHeight, 1 };

                ncdfArray2D = ncdfVar.read(origin, size).reduce();
            } else {
                throw new IOException(
                        "Unanticipated NetCDF variable dimensions.");
            }
        } else if (dims.size() > 2) {
            // Select the correct the depth
            int[] origin = new int[] { 0, selectedLongitude, 0 };
            int[] size = new int[] { globeTextureDepth, globeTextureHeight, 1 };

            ncdfArray2D = ncdfVar.read(origin, size).reduce();
        } else {
            ncdfArray2D = ncdfVar.read();
        }

        result = (float[]) ncdfArray2D.get1DJavaArray(float.class);

        return result;
    }

    private Array tryPermutationsArrayOpen(NetcdfFile ncfile,
            String... permutations) throws NetCDFNoSuchVariableException {
        boolean success = false;
        int i = 0;
        String current = permutations[i];
        Array result = null;

        while (!success) {
            try {
                result = NetCDFUtil.getData(ncfile, current);
                success = true;
            } catch (NetCDFNoSuchVariableException e) {
                i++;

                if (i > permutations.length - 1) {
                    break;
                } else {
                    current = permutations[i];
                }
            }
        }
        if (!success) {
            String perms = "";
            for (String s : permutations) {
                perms += s + "; ";
            }

            throw new NetCDFNoSuchVariableException(
                    "Dimension finder: All permutations (" + perms + ") failed");
        }
        return result;
    }

    private int tryPermutationsGetLength(NetcdfFile ncfile,
            String... permutations) throws NetCDFNoSuchVariableException {
        List<Dimension> dims = ncfile.getDimensions();

        int i = 0;
        String current = permutations[i];
        int result = -1;

        boolean success = false;
        while (!success) {
            for (Dimension d : dims) {
                if (d.getName().compareTo(current) == 0) {
                    result = d.getLength();
                    success = true;
                    break;
                }
            }

            i++;

            if (i > permutations.length - 1 || success) {
                break;
            } else {
                current = permutations[i];
            }
        }
        if (!success) {
            String perms = "";
            for (String s : permutations) {
                perms += s + "; ";
            }

            throw new NetCDFNoSuchVariableException(
                    "Dimension finder: All permutations (" + perms + ") failed");
        }
        return result;
    }

    public synchronized HDRTexture2D getLegendImage(GL3 gl, int glMultitexUnit,
            GlobeState state) throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber) {
            throw new WrongFrameException("ERROR: Request for frame nr "
                    + state.getFrameNumber() + " to NetCDFFrame " + frameNumber);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedLegendImages.containsKey(state)) {
                FloatBuffer fb = preparedLegendImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, 1, 500);
            } else {
                tex = ImageMaker.getLegendImage(gl, glMultitexUnit,
                        tGridPoints, state, 1, 500, true);
                preparedLegendImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public synchronized HDRTexture2D getLegendImage(GL3 gl,
            NetCDFFrame otherFrame, int glMultitexUnit, GlobeState state)
            throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber
                || otherFrame.getNumber() != frameNumber
                || state.getFrameNumber() != otherFrame.getNumber()) {
            throw new WrongFrameException("ERROR: FrameNumber mismatch");
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedLegendImages.containsKey(state)) {
                FloatBuffer fb = preparedLegendImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, 1, 500);
            } else {
                tex = ImageMaker.getLegendImage(gl, glMultitexUnit,
                        tGridPoints, otherFrame.getGridPoints(), state, 1, 500,
                        true);
                preparedLegendImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public synchronized HDRTexture2D getImage(GL3 gl, int glMultitexUnit,
            GlobeState state) throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber) {
            throw new WrongFrameException("ERROR: Request for frame nr "
                    + state.getFrameNumber() + " to NetCDFFrame " + frameNumber);
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedGlobeImages.containsKey(state)) {
                FloatBuffer fb = preparedGlobeImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, globeTextureWidth,
                        newHeight);
            } else {
                tex = ImageMaker.getImage(gl, glMultitexUnit, tGridPoints,
                        state, globeTextureWidth, globeTextureHeight,
                        newHeight, blankRows);
                preparedGlobeImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public synchronized HDRTexture2D getImage(GL3 gl, NetCDFFrame otherFrame,
            int glMultitexUnit, GlobeState state) throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber
                || otherFrame.getNumber() != frameNumber
                || state.getFrameNumber() != otherFrame.getNumber()) {
            throw new WrongFrameException("ERROR: FrameNumber mismatch");
        }

        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedGlobeImages.containsKey(state)) {
                FloatBuffer fb = preparedGlobeImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, globeTextureWidth,
                        newHeight);
            } else {
                tex = ImageMaker.getImage(gl, glMultitexUnit, tGridPoints,
                        otherFrame.getGridPoints(), state, globeTextureWidth,
                        globeTextureHeight, newHeight, blankRows);
                preparedGlobeImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public synchronized HDRTexture2D getDepthImage(GL3 gl, int glMultitexUnit,
            GlobeState state) throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber) {
            throw new WrongFrameException("ERROR: Request for frame nr "
                    + state.getFrameNumber() + " to NetCDFFrame " + frameNumber);
        }

        if (!initialized) {
            init();
        }

        int newWidth = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedDepthImages.containsKey(state)) {
                FloatBuffer fb = preparedDepthImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, newWidth,
                        globeTextureDepth);
            } else {
                tex = ImageMaker.getDepthImage(gl, glMultitexUnit, dGridPoints,
                        state, globeTextureHeight, globeTextureDepth, newWidth,
                        blankRows);
                preparedDepthImages.put(state, tex.getPixelBuffer());
            }
            displayedImages.put(glMultitexUnit, tex);
            displayedImageStates.put(glMultitexUnit, state);
        }

        return tex;
    }

    public synchronized HDRTexture2D getDepthImage(GL3 gl,
            NetCDFFrame otherFrame, int glMultitexUnit, GlobeState state)
            throws WrongFrameException {
        if (state.getFrameNumber() != frameNumber) {
            throw new WrongFrameException("ERROR: Request for frame nr "
                    + state.getFrameNumber() + " to NetCDFFrame " + frameNumber);
        }

        if (!initialized) {
            init();
        }

        int newWidth = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        HDRTexture2D tex;
        if (displayedImageStates.containsKey(glMultitexUnit)
                && displayedImageStates.get(glMultitexUnit) == state) {
            tex = displayedImages.get(glMultitexUnit);
        } else {
            if (preparedDepthImages.containsKey(state)) {
                FloatBuffer fb = preparedDepthImages.get(state);
                tex = new NetCDFTexture(glMultitexUnit, fb, newWidth,
                        globeTextureDepth);
            } else {
                tex = ImageMaker.getDepthImage(gl, glMultitexUnit, dGridPoints,
                        otherFrame.getGridPoints(), state, globeTextureHeight,
                        globeTextureDepth, newWidth, blankRows);
                preparedDepthImages.put(state, tex.getPixelBuffer());
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
