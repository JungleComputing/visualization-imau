package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.adaptor.ImageMaker.Color;
import imau.visualization.adaptor.ImageMaker.Dimensions;
import imau.visualization.netcdf.NetCDFNoSuchVariableException;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private final static ImauSettings                 settings      = ImauSettings
                                                                            .getInstance();
    private final static Logger                       logger        = LoggerFactory
                                                                            .getLogger(NetCDFFrame.class);

    private int                                       selectedDepth = settings
                                                                            .getDepthDef();

    private final int                                 frameNumber;

    private boolean                                   initialized, processed;

    private boolean                                   error;
    private String                                    errMessage;

    private int                                       globeTextureWidth,
            globeTextureHeight, globeTextureDepth;
    private TGridPoint[]                              tGridPoints;

    private final File                                file;

    private float                                     latMin, latMax;

    private NetcdfFile                                ncfile;

    private final HashMap<GlobeState, NetCDFDataGrid> dataGrids;
    private final ArrayList<GlobeState>               currentStates;

    public NetCDFFrame(File ncFile, int indexNumber) {
        this.file = ncFile;
        this.frameNumber = indexNumber;

        this.initialized = false;

        this.error = false;
        this.errMessage = "";

        this.dataGrids = new HashMap<GlobeState, NetCDFDataGrid>();
        this.currentStates = new ArrayList<GlobeState>();
    }

    private void getCurrentStates() {
        currentStates.clear();
        currentStates.add(settings.getLTState());
        currentStates.add(settings.getRTState());
        currentStates.add(settings.getLBState());
        currentStates.add(settings.getRBState());
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

        if (!processed) {
            for (GlobeState state : currentStates) {
                try {
                    NetCDFDataGrid grid;
                    if (dataGrids.containsKey(state)) {
                        grid = dataGrids.get(state);
                    } else {
                        float[] data = efficientOpenSingleDepthVariable(ncfile,
                                selectedDepth, state.getVariable().toString());

                        grid = new NetCDFDataGrid(state, globeTextureWidth,
                                globeTextureHeight, data);
                        dataGrids.put(state, grid);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidRangeException e) {
                    e.printStackTrace();
                }
            }
        }

        processed = true;
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

    public FloatBuffer getLegendImage(GlobeState state) {
        Dimensions dims;
        if (settings.isDynamicDimensions()) {
            dims = ImageMaker.getDynamicDimensions(tGridPoints, state);
        } else {
            dims = ImageMaker.getDimensions(state);
        }

        return calcLegendImage(state, dims);
    }

    public FloatBuffer getLegendImage(GlobeState state, NetCDFFrame otherFrame) {
        Dimensions dims;
        if (settings.isDynamicDimensions()) {
            dims = ImageMaker.getDynamicDimensions(tGridPoints,
                    otherFrame.getGridPoints(), state);
        } else {
            dims = ImageMaker.getDimensions(state);
        }

        return calcLegendImage(state, dims);
    }

    private FloatBuffer calcLegendImage(GlobeState state, Dimensions dims) {
        if (state.getDepth() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        String colorMapName = state.getColorMap();
        int height = 500;
        int width = 1;
        FloatBuffer outBuf = FloatBuffer.allocate(500 * 1 * 4);

        for (int row = height - 1; row >= 0; row--) {
            float index = row / (float) height;
            float var = (index * dims.getDiff()) + dims.min;

            Color c = ImageMaker.getColor(colorMapName, dims, var);

            for (int col = 0; col < width; col++) {
                outBuf.put(c.red);
                outBuf.put(c.green);
                outBuf.put(c.blue);
                outBuf.put(1f);
            }
        }

        outBuf.flip();

        return outBuf;
    }

    public FloatBuffer getSurfaceImage(GlobeState state) {
        if (state.getDepth() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        imau.visualization.adaptor.GlobeState.Variable variable = state
                .getVariable();
        String colorMapName = state.getColorMap();

        int pixels = newHeight * globeTextureWidth;

        FloatBuffer outBuf = FloatBuffer.allocate(pixels * 4);
        outBuf.clear();
        outBuf.rewind();

        for (int i = 0; i < blankRows; i++) {
            for (int w = 0; w < globeTextureWidth; w++) {
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
            }
        }

        Dimensions dims;

        if (settings.isDynamicDimensions()) {
            dims = ImageMaker.getDynamicDimensions(tGridPoints, state);
        } else {
            dims = ImageMaker.getDimensions(state);
        }

        for (int row = globeTextureHeight - 1; row >= 0; row--) {
            for (int col = 0; col < globeTextureWidth; col++) {
                int i = (row * globeTextureWidth + col);

                Color c = null;
                if (variable == GlobeState.Variable.SSH) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].ssh);
                } else if (variable == GlobeState.Variable.SHF) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].shf);
                } else if (variable == GlobeState.Variable.SFWF) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].sfwf);
                } else if (variable == GlobeState.Variable.HMXL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].hmxl);
                } else if (variable == GlobeState.Variable.SALT) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].salinity);
                } else if (variable == GlobeState.Variable.TEMP) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].temp);
                } else if (variable == GlobeState.Variable.UVEL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].uvel);
                } else if (variable == GlobeState.Variable.VVEL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].vvel);
                } else if (variable == GlobeState.Variable.KE) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].ke);
                } else if (variable == GlobeState.Variable.PD) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].pd);
                } else if (variable == GlobeState.Variable.TAUX) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].taux);
                } else if (variable == GlobeState.Variable.TAUY) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].tauy);
                } else if (variable == GlobeState.Variable.H2) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].h2);
                }

                if (c != null) {
                    outBuf.put(c.red);
                    outBuf.put(c.green);
                    outBuf.put(c.blue);
                    outBuf.put(0f);
                } else {
                    outBuf.put(0f);
                    outBuf.put(0f);
                    outBuf.put(0f);
                    outBuf.put(0f);
                }
            }
        }

        while (outBuf.hasRemaining()) {
            outBuf.put(0f);
        }

        outBuf.flip();

        return outBuf;
    }

    public FloatBuffer getSurfaceImage(GlobeState state, NetCDFFrame otherFrame) {
        if (!currentStates.contains(state)) {
            getCurrentStates();
            processed = false;
        }

        if (state.getDepth() != selectedDepth) {
            selectedDepth = state.getDepth();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        int newHeight = (int) Math.floor((180f / (latMax - latMin))
                * globeTextureHeight);
        int blankRows = (int) Math.floor(180f / latMax);

        imau.visualization.adaptor.GlobeState.Variable variable = state
                .getVariable();
        String colorMapName = state.getColorMap();

        int pixels = newHeight * globeTextureWidth;

        FloatBuffer outBuf = FloatBuffer.allocate(pixels * 4);
        outBuf.clear();
        outBuf.rewind();

        for (int i = 0; i < blankRows; i++) {
            for (int w = 0; w < globeTextureWidth; w++) {
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
            }
        }

        TGridPoint[] tGridPointsDS2 = otherFrame.getGridPoints();

        Dimensions dims;
        if (settings.isDynamicDimensions()) {
            dims = ImageMaker.getDynamicDimensions(tGridPoints, tGridPointsDS2,
                    state);
        } else {
            dims = ImageMaker.getDimensions(state);
        }

        for (int row = globeTextureHeight - 1; row >= 0; row--) {
            for (int col = 0; col < globeTextureWidth; col++) {
                int i = (row * globeTextureWidth + col);

                Color c = null;
                if (variable == GlobeState.Variable.SSH) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].ssh, tGridPointsDS2[i].ssh);
                } else if (variable == GlobeState.Variable.SHF) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].shf, tGridPointsDS2[i].shf);
                } else if (variable == GlobeState.Variable.SFWF) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].sfwf, tGridPointsDS2[i].sfwf);
                } else if (variable == GlobeState.Variable.HMXL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].hmxl, tGridPointsDS2[i].hmxl);
                } else if (variable == GlobeState.Variable.SALT) {
                    c = ImageMaker
                            .getColor(colorMapName, dims,
                                    tGridPoints[i].salinity,
                                    tGridPointsDS2[i].salinity);
                } else if (variable == GlobeState.Variable.TEMP) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].temp, tGridPointsDS2[i].temp);
                } else if (variable == GlobeState.Variable.UVEL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].uvel, tGridPointsDS2[i].uvel);
                } else if (variable == GlobeState.Variable.VVEL) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].vvel, tGridPointsDS2[i].vvel);
                } else if (variable == GlobeState.Variable.KE) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].ke, tGridPointsDS2[i].ke);
                } else if (variable == GlobeState.Variable.PD) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].pd, tGridPointsDS2[i].pd);
                } else if (variable == GlobeState.Variable.TAUX) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].taux, tGridPointsDS2[i].taux);
                } else if (variable == GlobeState.Variable.TAUY) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].tauy, tGridPointsDS2[i].tauy);
                } else if (variable == GlobeState.Variable.H2) {
                    c = ImageMaker.getColor(colorMapName, dims,
                            tGridPoints[i].h2, tGridPointsDS2[i].h2);
                }

                if (c != null) {
                    outBuf.put(c.red);
                    outBuf.put(c.green);
                    outBuf.put(c.blue);
                    outBuf.put(0f);
                } else {
                    outBuf.put(0f);
                    outBuf.put(0f);
                    outBuf.put(0f);
                    outBuf.put(0f);
                }
            }
        }

        while (outBuf.hasRemaining()) {
            outBuf.put(0f);
        }

        outBuf.flip();

        return outBuf;
    }
}
