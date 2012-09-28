package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.adaptor.GlobeState.DataMode;
import imau.visualization.adaptor.GlobeState.Variable;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageMaker {
    private final static ImauSettings settings = ImauSettings.getInstance();
    private final static Logger       logger   = LoggerFactory
                                                       .getLogger(ImageMaker.class);

    public static class Dimensions {
        public float min, max;

        public Dimensions(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float getDiff() {
            return max - min;
        }

        @Override
        public int hashCode() {
            return (int) (min + max);
        }

        @Override
        public boolean equals(Object thatObject) {
            if (this == thatObject)
                return true;
            if (!(thatObject instanceof GlobeState))
                return false;

            // cast to native object is now safe
            Dimensions that = (Dimensions) thatObject;

            // now a proper field-by-field evaluation can be made
            return (min == that.min && max == that.max);
        }
    }

    public static class Color {
        public float              red, green, blue;
        public static final Color WHITE = new Color(1f, 1f, 1f);
        public static final Color BLACK = new Color(0f, 0f, 0f);

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
    private static HashMap<GlobeState, Dimensions>          dimensionMap;
    private static HashMap<String, Dimensions>              dimension2Map;

    private static HashMap<GlobeState, Dimensions>          doubleDimensionMap;

    private static HashMap<Integer, GlobeState>             storedStates;
    private static HashMap<Integer, Texture2D>              storedTextures;
    private static HashMap<Integer, Texture2D>              storedLegends;

    static {
        rebuild();
    }

    public static void rebuild() {
        colorMapMaps = new HashMap<String, HashMap<Integer, Color>>();
        dimension2Map = new HashMap<String, ImageMaker.Dimensions>();
        dimensionMap = new HashMap<GlobeState, ImageMaker.Dimensions>();
        doubleDimensionMap = new HashMap<GlobeState, ImageMaker.Dimensions>();
        storedStates = new HashMap<Integer, GlobeState>();
        storedTextures = new HashMap<Integer, Texture2D>();
        storedLegends = new HashMap<Integer, Texture2D>();

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

        if (var < -1E33) {
            color = Color.BLACK;
        } else if (var < dim.min) {
            color = colorMap.get(0);
        } else if (var > dim.max) {
            color = colorMap.get(cmEntries - 1);
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

            float colorInterval = rawIndex - iLow;

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

        float var = var2 - var1;
        float result = (var - dim.min) / dim.getDiff();
        float rawIndex = (result * cmEntries);

        if (var1 < -1E33 || var2 < -1E33) {
            color = Color.BLACK;
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

            float colorInterval = rawIndex - iLow;

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

    public static Dimensions getDynamicDimensions(TGridPoint[] tGridPoints,
            GlobeState state) {
        int pixels = tGridPoints.length;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        if (dimensionMap.containsKey(state)) {
            return dimensionMap.get(state);
        }

        if (state.getVariable() == GlobeState.Variable.SSH) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].ssh;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.SHF) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].shf;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.SFWF) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].sfwf;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.HMXL) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].hmxl;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.SALT) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].salinity;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.TEMP) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].temp;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.UVEL) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].uvel;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.VVEL) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].vvel;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.KE) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].ke;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.PD) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].pd;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.TAUX) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].taux;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.TAUY) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].tauy;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        } else if (state.getVariable() == GlobeState.Variable.H2) {
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].h2;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
        }

        Dimensions dim = new Dimensions(min, max);
        dimensionMap.put(state, dim);

        return dim;
    }

    public static Dimensions getDynamicDimensions(TGridPoint[] tGridPoints1,
            TGridPoint[] tGridPoints2, GlobeState state) {
        int pixels = tGridPoints1.length;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        float avg = 0f;

        if (doubleDimensionMap.containsKey(state)) {
            return doubleDimensionMap.get(state);
        }

        float total = 0f;
        if (state.getVariable() == GlobeState.Variable.SSH) {
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

                total += val;
            }
        } else if (state.getVariable() == GlobeState.Variable.SHF) {
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
        } else if (state.getVariable() == GlobeState.Variable.SFWF) {
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
        } else if (state.getVariable() == GlobeState.Variable.HMXL) {
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
        } else if (state.getVariable() == GlobeState.Variable.SALT) {
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
        } else if (state.getVariable() == GlobeState.Variable.TEMP) {
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
        } else if (state.getVariable() == GlobeState.Variable.UVEL) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].uvel;
                float val2 = tGridPoints2[i].uvel;
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
        } else if (state.getVariable() == GlobeState.Variable.VVEL) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].vvel;
                float val2 = tGridPoints2[i].vvel;
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
        } else if (state.getVariable() == GlobeState.Variable.KE) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].ke;
                float val2 = tGridPoints2[i].ke;
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
        } else if (state.getVariable() == GlobeState.Variable.PD) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].pd;
                float val2 = tGridPoints2[i].pd;
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
        } else if (state.getVariable() == GlobeState.Variable.TAUX) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].taux;
                float val2 = tGridPoints2[i].taux;
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
        } else if (state.getVariable() == GlobeState.Variable.TAUY) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].tauy;
                float val2 = tGridPoints2[i].tauy;
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
        } else if (state.getVariable() == GlobeState.Variable.H2) {
            for (int i = 0; i < pixels; i++) {
                float val1 = tGridPoints1[i].h2;
                float val2 = tGridPoints2[i].h2;
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

        avg = total / pixels;
        float minDiff = avg - min;
        float maxDiff = max - avg;

        if (maxDiff > minDiff) {
            min = avg - maxDiff;
        } else if (minDiff > maxDiff) {
            max = avg + minDiff;
        }

        Dimensions dim = new Dimensions(min, max);
        doubleDimensionMap.put(state, dim);

        return dim;
    }

    public static Dimensions getDimensions(GlobeState state) {
        Variable var = state.getVariable();
        float max = 0;
        float min = 0;
        if (state.getDataMode() == DataMode.DIFF) {
            max = settings.getVarDiffMax(var);
            min = settings.getVarDiffMin(var);
        } else {
            max = settings.getVarMax(var);
            min = settings.getVarMin(var);
        }

        return new Dimensions(min, max);
    }

    public static Dimensions getDimensions(String varName) {
        float max = 0;
        float min = 0;

        max = settings.getVarMax(varName);
        min = settings.getVarMin(varName);

        return new Dimensions(min, max);
    }

    public static Dimensions getDynamicDimensions(String varName,
            float[] gridPoints) {
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        if (dimension2Map.containsKey(varName)) {
            return dimension2Map.get(varName);
        }

        for (int i = 0; i < gridPoints.length; i++) {
            float val = gridPoints[i];
            if (val > max) {
                max = val;
            }
            if (val < min && val > -1E33) {
                min = val;
            }
        }
        Dimensions dims = new Dimensions(min, max);
        dimension2Map.put(varName, dims);
        return dims;
    }

}
