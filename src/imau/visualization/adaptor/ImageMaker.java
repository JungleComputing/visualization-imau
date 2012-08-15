package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.adaptor.GlobeState.Variable;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;

import javax.media.opengl.GL3;

import openglCommon.textures.HDRTexture2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMaker {
    private final static ImauSettings settings = ImauSettings.getInstance();
    private final static Logger       logger   = LoggerFactory
                                                       .getLogger(ImageMaker.class);

    private static class Dimensions {
        public float min, max;

        public Dimensions(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float getDiff() {
            return max - min;
        }
    }

    public static class Color {
        public float              red, green, blue;
        public static final Color WHITE = new Color(1f, 1f, 1f);

        public Color() {
            this.red = 0f;
            this.green = 0f;
            this.blue = 0f;
        }

        public Color(float i, float j, float k) {
            this.red = i;
            this.green = j;
            this.blue = k;
        }
    }

    private static HashMap<String, HashMap<Integer, Color>> colorMapMaps;
    private static HashMap<GlobeState.Variable, Dimensions> dimensionMap;
    private static HashMap<GlobeState.Variable, Dimensions> doubleDimensionMap;

    private static int                                      currentFrameNumber;
    private static int                                      currentDepth;

    static {
        rebuild();
    }

    public static void rebuild() {
        colorMapMaps = new HashMap<String, HashMap<Integer, Color>>();
        dimensionMap = new HashMap<GlobeState.Variable, ImageMaker.Dimensions>();
        doubleDimensionMap = new HashMap<GlobeState.Variable, ImageMaker.Dimensions>();

        try {
            String[] colorMapFileNames = NetCDFUtil.getColorMaps();
            for (String fileName : colorMapFileNames) {
                HashMap<Integer, Color> colorMap = new HashMap<Integer, Color>();

                BufferedReader in = new BufferedReader(new FileReader(
                        "colormaps/" + fileName + ".ncmap"));
                String str;

                int key = 0;
                while ((str = in.readLine()) != null) {
                    String[] numbers = str.split(" ");
                    colorMap.put(key,
                            new Color(Integer.parseInt(numbers[0]) / 255f,
                                    Integer.parseInt(numbers[1]) / 255f,
                                    Integer.parseInt(numbers[2]) / 255f));
                    key++;
                }

                in.close();

                colorMapMaps.put(fileName, colorMap);
                System.out.println("Colormap " + fileName
                        + " registered for use.");
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static HDRTexture2D getImage(GL3 gl, int glMultitexUnit,
            int frameNumber, int depth, TGridPoint[] tGridPoints,
            GlobeState state, int dsWidth, int dsHeight, int imgHeight,
            int blankStartRows) {
        Variable variable = state.getVariable();
        String colorMapName = state.getColorMap();

        int pixels = imgHeight * dsWidth;

        FloatBuffer outBuf = FloatBuffer.allocate(pixels * 4);
        outBuf.clear();
        outBuf.rewind();

        for (int i = 0; i < blankStartRows; i++) {
            for (int w = 0; w < dsWidth; w++) {
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
            }
        }

        for (int row = dsHeight - 1; row >= 0; row--) {
            for (int col = 0; col < dsWidth; col++) {
                int i = (row * dsWidth + col);

                Color c = null;

                Dimensions dims;

                if (settings.isDynamicDimensions()) {
                    dims = getDimensions(frameNumber, depth, tGridPoints,
                            variable);
                } else {
                    dims = getDimensions(variable);
                }

                if (variable == GlobeState.Variable.SSH) {
                    c = getColor(colorMapName, dims, tGridPoints[i].ssh);
                } else if (variable == GlobeState.Variable.SHF) {
                    c = getColor(colorMapName, dims, tGridPoints[i].shf);
                } else if (variable == GlobeState.Variable.SFWF) {
                    c = getColor(colorMapName, dims, tGridPoints[i].sfwf);
                } else if (variable == GlobeState.Variable.HMXL) {
                    c = getColor(colorMapName, dims, tGridPoints[i].hmxl);
                } else if (variable == GlobeState.Variable.SALT) {
                    c = getColor(colorMapName, dims, tGridPoints[i].salinity);
                } else if (variable == GlobeState.Variable.TEMP) {
                    c = getColor(colorMapName, dims, tGridPoints[i].temp);
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

        return new NetCDFTexture(glMultitexUnit, outBuf, dsWidth, imgHeight);
    }

    public static HDRTexture2D getImage(GL3 gl, int glMultitexUnit,
            int frameNumber, int depth, TGridPoint[] tGridPoints1,
            TGridPoint[] tGridPoints2, GlobeState state, int dsWidth,
            int dsHeight, int imgHeight, int blankStartRows) {
        Variable variable = state.getVariable();
        String colorMapName = state.getColorMap();

        int pixels = imgHeight * dsWidth;

        FloatBuffer outBuf = FloatBuffer.allocate(pixels * 4);
        outBuf.clear();
        outBuf.rewind();

        for (int i = 0; i < blankStartRows; i++) {
            for (int w = 0; w < dsWidth; w++) {
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
                outBuf.put(0f);
            }
        }

        for (int row = dsHeight - 1; row >= 0; row--) {
            for (int col = 0; col < dsWidth; col++) {
                int i = (row * dsWidth + col);

                Color c = null;

                Dimensions dims;

                dims = getDimensions(frameNumber, depth, tGridPoints1,
                        tGridPoints2, variable);

                if (variable == GlobeState.Variable.SSH) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].ssh,
                            tGridPoints2[i].ssh);
                } else if (variable == GlobeState.Variable.SHF) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].shf,
                            tGridPoints2[i].shf);
                } else if (variable == GlobeState.Variable.SFWF) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].sfwf,
                            tGridPoints2[i].sfwf);
                } else if (variable == GlobeState.Variable.HMXL) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].hmxl,
                            tGridPoints2[i].hmxl);
                } else if (variable == GlobeState.Variable.SALT) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].salinity,
                            tGridPoints2[i].salinity);
                } else if (variable == GlobeState.Variable.TEMP) {
                    c = getColor(colorMapName, dims, tGridPoints1[i].temp,
                            tGridPoints2[i].temp);
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

        return new NetCDFTexture(glMultitexUnit, outBuf, dsWidth, imgHeight);
    }

    public synchronized static Color getColor(String colorMapName,
            Dimensions dim, float var) {
        if (!colorMapMaps.containsKey(colorMapName)) {
            System.err.println("Unregistered color map requested: "
                    + colorMapName);
            colorMapMaps.get("default");
        }

        HashMap<Integer, Color> colorMap = colorMapMaps.get(colorMapName);

        int cmEntries = colorMap.size();

        Color color = null;

        float result = (var - dim.min) / dim.getDiff();
        float rawIndex = result * cmEntries;

        if (var < dim.min) {
            color = Color.WHITE;
        } else if (var > dim.max) {
            color = Color.WHITE;
        } else {
            float red = 0;
            float green = 0;
            float blue = 0;

            int iLow = (int) Math.floor(rawIndex);
            int iHigh = (int) Math.ceil(rawIndex);

            Color cLow;
            if (iLow == cmEntries) {
                cLow = colorMap.get(cmEntries - 1);
            } else if (iLow < 0) {
                cLow = colorMap.get(0);
            } else {
                cLow = colorMap.get(iLow);
            }

            Color cHigh;
            if (iHigh == cmEntries) {
                cHigh = colorMap.get(cmEntries - 1);
            } else if (iHigh < 0) {
                cHigh = colorMap.get(0);
            } else {
                cHigh = colorMap.get(iHigh);
            }

            float colorInterval = (float) (rawIndex - Math.floor(rawIndex));

            red = getInterpolatedColor(cHigh.red, cLow.red, colorInterval);
            green = getInterpolatedColor(cHigh.green, cLow.green, colorInterval);
            blue = getInterpolatedColor(cHigh.blue, cLow.blue, colorInterval);

            color = new Color(red, green, blue);
        }

        return color;
    }

    public synchronized static Color getColor(String colorMapName,
            Dimensions dim, float var1, float var2) {
        if (!colorMapMaps.containsKey(colorMapName)) {
            System.err.println("Unregistered color map requested: "
                    + colorMapName);
            colorMapMaps.get("default");
        }

        HashMap<Integer, Color> colorMap = colorMapMaps.get(colorMapName);

        int cmEntries = colorMap.size();

        Color color = null;

        float var = var1 - var2;
        float result = (var - dim.min) / dim.getDiff();
        float rawIndex = (result * cmEntries);

        if (var1 < -1E33 || var2 < -1E33) {
            color = Color.WHITE;
        } else {
            float red = 0;
            float green = 0;
            float blue = 0;

            int iLow = (int) Math.floor(rawIndex);
            int iHigh = (int) Math.ceil(rawIndex);

            Color cLow;
            if (iLow == cmEntries) {
                cLow = colorMap.get(cmEntries - 1);
            } else if (iLow < 0) {
                cLow = colorMap.get(0);
            } else {
                cLow = colorMap.get(iLow);
            }

            Color cHigh;
            if (iHigh == cmEntries) {
                cHigh = colorMap.get(cmEntries - 1);
            } else if (iHigh < 0) {
                cHigh = colorMap.get(0);
            } else {
                cHigh = colorMap.get(iHigh);
            }

            float colorInterval = (float) (rawIndex - Math.floor(rawIndex));

            red = getInterpolatedColor(cHigh.red, cLow.red, colorInterval);
            green = getInterpolatedColor(cHigh.green, cLow.green, colorInterval);
            blue = getInterpolatedColor(cHigh.blue, cLow.blue, colorInterval);

            color = new Color(red, green, blue);
        }

        return color;
    }

    private static float getInterpolatedColor(float high, float low,
            float colorInterval) {
        float result = 0f;

        if (low > high) {
            float temp = high;
            high = low;
            low = temp;

            result = low + (colorInterval * (high - low));
        } else if (low == high) {
            result = low;
        } else {
            result = low + (colorInterval * (high - low));
        }

        return result;
    }

    private static Dimensions getDimensions(int frameNumber, int depth,
            TGridPoint[] tGridPoints, GlobeState.Variable variable) {
        int pixels = tGridPoints.length;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        if (dimensionMap.containsKey(variable)) {
            return dimensionMap.get(variable);
        }

        if (variable == GlobeState.Variable.SSH) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].ssh;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (variable == GlobeState.Variable.SHF) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].shf;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (variable == GlobeState.Variable.SFWF) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].sfwf;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (variable == GlobeState.Variable.HMXL) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].hmxl;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (variable == GlobeState.Variable.SALT) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].salinity;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (variable == GlobeState.Variable.TEMP) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].temp;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        }

        Dimensions dim = new Dimensions(min, max);
        dimensionMap.put(variable, dim);
        currentFrameNumber = frameNumber;
        currentDepth = depth;

        return dim;
    }

    private static Dimensions getDimensions(int frameNumber, int depth,
            TGridPoint[] tGridPoints1, TGridPoint[] tGridPoints2,
            GlobeState.Variable variable) {
        int pixels = tGridPoints1.length;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        if (currentFrameNumber == frameNumber && currentDepth == depth
                && doubleDimensionMap.containsKey(variable)) {
            return doubleDimensionMap.get(variable);
        } else {
            doubleDimensionMap.clear();
        }

        if (variable == GlobeState.Variable.SSH) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].ssh;
                float val2 = tGridPoints2[i].ssh;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        } else if (variable == GlobeState.Variable.SHF) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].shf;
                float val2 = tGridPoints2[i].shf;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        } else if (variable == GlobeState.Variable.SFWF) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].sfwf;
                float val2 = tGridPoints2[i].sfwf;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        } else if (variable == GlobeState.Variable.HMXL) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].hmxl;
                float val2 = tGridPoints2[i].hmxl;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        } else if (variable == GlobeState.Variable.SALT) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].salinity;
                float val2 = tGridPoints2[i].salinity;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        } else if (variable == GlobeState.Variable.TEMP) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].temp;
                float val2 = tGridPoints2[i].temp;
                float val = val1 - val2;

                if (val1 > -1E33 && val2 > -1E33) {
                    if (val > max) {
                        max = val;
                    }
                    if (val < min) {
                        min = val;
                    }
                }
            }
        }

        Dimensions dim = new Dimensions(min, max);
        doubleDimensionMap.put(variable, dim);
        currentFrameNumber = frameNumber;
        currentDepth = depth;

        return dim;
    }

    private static Dimensions getDimensions(GlobeState.Variable variable) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        if (variable == GlobeState.Variable.SSH) {
            max = settings.getMaxSsh();
            min = settings.getMinSsh();
        } else if (variable == GlobeState.Variable.SHF) {
            max = settings.getMaxShf();
            min = settings.getMinShf();
        } else if (variable == GlobeState.Variable.SFWF) {
            max = settings.getMaxSfwf();
            min = settings.getMinSfwf();
        } else if (variable == GlobeState.Variable.HMXL) {
            max = settings.getMaxHmxl();
            min = settings.getMinHmxl();
        } else if (variable == GlobeState.Variable.SALT) {
            max = settings.getMaxSalt();
            min = settings.getMinSalt();
        } else if (variable == GlobeState.Variable.TEMP) {
            max = settings.getMaxTemp();
            min = settings.getMinTemp();
        }

        return new Dimensions(min, max);
    }
}
