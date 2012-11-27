package imau.visualization;

import imau.visualization.data.SurfaceTextureDescription;

import java.util.HashMap;

import openglCommon.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImauSettings {
    private final Logger logger = LoggerFactory.getLogger(ImauSettings.class);

    private static class SingletonHolder {
        public final static ImauSettings instance = new ImauSettings();
    }

    public static ImauSettings getInstance() {
        return SingletonHolder.instance;
    }

    public enum GlobeMode {
        FIRST_DATASET, SECOND_DATASET, DIFF
    };

    public enum Months {
        Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
    };

    private static final int             IMAGE_WIDTH                     = 900;
    private static final int             IMAGE_HEIGHT                    = 602;

    private boolean                      STEREO_RENDERING                = true;
    private boolean                      STEREO_SWITCHED                 = true;

    private float                        STEREO_OCULAR_DISTANCE_MIN      = 0f;
    private float                        STEREO_OCULAR_DISTANCE_DEF      = .2f;
    private float                        STEREO_OCULAR_DISTANCE_MAX      = 1f;

    // Size settings for default startup and screenshots
    private int                          DEFAULT_SCREEN_WIDTH            = 1024;
    private int                          DEFAULT_SCREEN_HEIGHT           = 768;

    private int                          SCREENSHOT_SCREEN_WIDTH         = 1280;
    private int                          SCREENSHOT_SCREEN_HEIGHT        = 720;

    // Settings for the initial view
    private int                          INITIAL_SIMULATION_FRAME        = 0;
    private float                        INITIAL_ROTATION_X              = 17f;
    private float                        INITIAL_ROTATION_Y              = -25f;
    private float                        INITIAL_ZOOM                    = -390.0f;

    // Setting per movie frame
    private boolean                      MOVIE_ROTATE                    = true;
    private float                        MOVIE_ROTATION_SPEED_MIN        = -1f;
    private float                        MOVIE_ROTATION_SPEED_MAX        = 1f;
    private float                        MOVIE_ROTATION_SPEED_DEF        = -0.25f;

    // Settings for the gas cloud octree
    private int                          MAX_OCTREE_DEPTH                = 25;
    private float                        OCTREE_EDGES                    = 800f;

    // Settings that should never change, but are listed here to make sure they
    // can be found if necessary
    private int                          MAX_EXPECTED_MODELS             = 1000;

    protected String                     SCREENSHOT_PATH                 = System.getProperty("user.dir")
                                                                                 + System.getProperty("path.separator");

    private long                         WAITTIME_FOR_RETRY              = 10000;
    private long                         WAITTIME_FOR_MOVIE              = 1000;
    private int                          TIME_STEP_SIZE                  = 1;
    private float                        EPSILON                         = 1.0E-7f;

    private int                          FILE_EXTENSION_LENGTH           = 2;
    private int                          FILE_NUMBER_LENGTH              = 4;

    private final String[]               ACCEPTABLE_POSTFIXES            = { ".nc" };

    private String                       CURRENT_POSTFIX                 = "nc";

    private int                          PREPROCESSING_AMOUNT            = 2;

    private final HashMap<String, Float> minValues;
    private final HashMap<String, Float> diffMinValues;
    private final HashMap<String, Float> maxValues;
    private final HashMap<String, Float> diffMaxValues;
    private final HashMap<String, Float> currentMinValues;
    private final HashMap<String, Float> currentDiffMinValues;
    private final HashMap<String, Float> currentMaxValues;
    private final HashMap<String, Float> currentDiffMaxValues;

    private int                          DEPTH_MIN                       = 0;
    private int                          DEPTH_DEF                       = 0;
    private int                          DEPTH_MAX                       = 41;

    private int                          WINDOW_SELECTION                = 0;

    private boolean                      IMAGE_STREAM_OUTPUT             = false;
    private final int                    SAGE_FRAMES_PER_SECOND          = 10;
    private boolean                      IMAGE_STREAM_GL_ONLY            = true;

    private float                        HEIGHT_DISTORION                = 0f;
    private final float                  HEIGHT_DISTORION_MIN            = 0f;
    private final float                  HEIGHT_DISTORION_MAX            = .01f;

    private String                       SAGE_DIRECTORY                  = "/home/maarten/sage-code/sage";

    private final boolean                TOUCH_CONNECTED                 = false;

    private SurfaceTextureDescription    ltDescription;
    private SurfaceTextureDescription    rtDescription;
    private SurfaceTextureDescription    lbDescription;
    private SurfaceTextureDescription    rbDescription;

    private final String                 grid_width_dimension_substring  = "lon";
    private final String                 grid_height_dimension_substring = "lat";

    private final int                    MAX_NUMBER_OF_SCREENS           = 4;

    private ImauSettings() {
        super();
        minValues = new HashMap<String, Float>();
        maxValues = new HashMap<String, Float>();
        currentMinValues = new HashMap<String, Float>();
        currentMaxValues = new HashMap<String, Float>();
        diffMinValues = new HashMap<String, Float>();
        diffMaxValues = new HashMap<String, Float>();
        currentDiffMinValues = new HashMap<String, Float>();
        currentDiffMaxValues = new HashMap<String, Float>();

        minValues.put("SSH", -250f);
        maxValues.put("SSH", 250f);
        currentMinValues.put("SSH", -200f);
        currentMaxValues.put("SSH", 100f);
        diffMinValues.put("SSH", -100f);
        diffMaxValues.put("SSH", 100f);
        currentDiffMinValues.put("SSH", -100f);
        currentDiffMaxValues.put("SSH", 100f);

        minValues.put("SHF", -500f);
        maxValues.put("SHF", 500f);
        currentMinValues.put("SHF", -400f);
        currentMaxValues.put("SHF", 250f);
        diffMinValues.put("SHF", -150f);
        diffMaxValues.put("SHF", 150f);
        currentDiffMinValues.put("SHF", -150f);
        currentDiffMaxValues.put("SHF", 150f);

        minValues.put("SFWF", -3E-4f);
        maxValues.put("SFWF", 3E-4f);
        currentMinValues.put("SFWF", -3E-4f);
        currentMaxValues.put("SFWF", 3E-4f);
        diffMinValues.put("SFWF", -1E-4f);
        diffMaxValues.put("SFWF", 1E-4f);
        currentDiffMinValues.put("SFWF", -1E-4f);
        currentDiffMaxValues.put("SFWF", 1E-4f);

        minValues.put("HMXL", 0f);
        maxValues.put("HMXL", 300000f);
        currentMinValues.put("HMXL", 0f);
        currentMaxValues.put("HMXL", 150000f);
        diffMinValues.put("HMXL", -50000f);
        diffMaxValues.put("HMXL", 50000f);
        currentDiffMinValues.put("HMXL", -50000f);
        currentDiffMaxValues.put("HMXL", 50000f);

        minValues.put("SALT", 0.00f);
        maxValues.put("SALT", 0.05f);
        currentMinValues.put("SALT", 0.03f);
        currentMaxValues.put("SALT", 0.04f);
        diffMinValues.put("SALT", -0.025f);
        diffMaxValues.put("SALT", 0.025f);
        currentDiffMinValues.put("SALT", -0.025f);
        currentDiffMaxValues.put("SALT", 0.025f);

        minValues.put("TEMP", -10f);
        maxValues.put("TEMP", 50f);
        currentMinValues.put("TEMP", -2f);
        currentMaxValues.put("TEMP", 30f);
        diffMinValues.put("TEMP", -15f);
        diffMaxValues.put("TEMP", 15f);
        currentDiffMinValues.put("TEMP", -15f);
        currentDiffMaxValues.put("TEMP", 15f);

        minValues.put("UVEL", -200f);
        maxValues.put("UVEL", 200f);
        currentMinValues.put("UVEL", -200f);
        currentMaxValues.put("UVEL", 200f);
        diffMinValues.put("UVEL", -100f);
        diffMaxValues.put("UVEL", 100f);
        currentDiffMinValues.put("UVEL", -100f);
        currentDiffMaxValues.put("UVEL", 100f);

        minValues.put("VVEL", -200f);
        maxValues.put("VVEL", 200f);
        currentMinValues.put("VVEL", -200f);
        currentMaxValues.put("VVEL", 200f);
        diffMinValues.put("VVEL", -100f);
        diffMaxValues.put("VVEL", 100f);
        currentDiffMinValues.put("VVEL", -100f);
        currentDiffMaxValues.put("VVEL", 100f);

        minValues.put("KE", 0f);
        maxValues.put("KE", 10000f);
        currentMinValues.put("KE", 0f);
        currentMaxValues.put("KE", 20000f);
        diffMinValues.put("KE", -5000f);
        diffMaxValues.put("KE", 5000f);
        currentDiffMinValues.put("KE", -5000f);
        currentDiffMaxValues.put("KE", 5000f);

        minValues.put("PD", 1f);
        maxValues.put("PD", 1.1f);
        currentMinValues.put("PD", 1f);
        currentMaxValues.put("PD", 1.04f);
        diffMinValues.put("PD", -0.01f);
        diffMaxValues.put("PD", 0.01f);
        currentDiffMinValues.put("PD", -0.01f);
        currentDiffMaxValues.put("PD", 0.01f);

        minValues.put("TAUX", -1f);
        maxValues.put("TAUX", 1f);
        currentMinValues.put("TAUX", -1f);
        currentMaxValues.put("TAUX", 1f);
        diffMinValues.put("TAUX", -.5f);
        diffMaxValues.put("TAUX", .5f);
        currentDiffMinValues.put("TAUX", -.5f);
        currentDiffMaxValues.put("TAUX", .5f);

        minValues.put("TAUY", -1f);
        maxValues.put("TAUY", 1f);
        currentMinValues.put("TAUY", -1f);
        currentMaxValues.put("TAUY", 1f);
        diffMinValues.put("TAUY", -.5f);
        diffMaxValues.put("TAUY", .5f);
        currentDiffMinValues.put("TAUY", -.5f);
        currentDiffMaxValues.put("TAUY", .5f);

        minValues.put("H2", 0f);
        maxValues.put("H2", 200000f);
        currentMinValues.put("H2", 0f);
        currentMaxValues.put("H2", 100000f);
        diffMinValues.put("H2", -50000f);
        diffMaxValues.put("H2", 50000f);
        currentDiffMinValues.put("H2", -50000f);
        currentDiffMaxValues.put("H2", 50000f);

        ltDescription = new SurfaceTextureDescription(7502, 0, "TEMP",
                "default", false, false, false, currentMinValues.get("TEMP"),
                currentMaxValues.get("TEMP"));

        rtDescription = new SurfaceTextureDescription(7502, 0, "KE", "rainbow",
                false, false, false, currentMinValues.get("KE"),
                currentMaxValues.get("KE"));

        lbDescription = new SurfaceTextureDescription(7502, 0, "SALT",
                "inv_diff", false, false, false, currentMinValues.get("SALT"),
                currentMaxValues.get("SALT"));

        rbDescription = new SurfaceTextureDescription(7502, 0, "HMXL",
                "hotres", false, false, false, currentMinValues.get("HMXL"),
                currentMaxValues.get("HMXL"));

        try {
            final TypedProperties props = new TypedProperties();
            props.loadFromFile("settings.properties");

            STEREO_RENDERING = props.getBooleanProperty("STEREO_RENDERING");
            STEREO_SWITCHED = props.getBooleanProperty("STEREO_SWITCHED");

            STEREO_OCULAR_DISTANCE_MIN = props
                    .getFloatProperty("STEREO_OCULAR_DISTANCE_MIN");
            STEREO_OCULAR_DISTANCE_MAX = props
                    .getFloatProperty("STEREO_OCULAR_DISTANCE_MAX");
            STEREO_OCULAR_DISTANCE_DEF = props
                    .getFloatProperty("STEREO_OCULAR_DISTANCE_DEF");

            // Size settings for default startup and screenshots
            DEFAULT_SCREEN_WIDTH = props.getIntProperty("DEFAULT_SCREEN_WIDTH");
            DEFAULT_SCREEN_HEIGHT = props
                    .getIntProperty("DEFAULT_SCREEN_HEIGHT");

            SCREENSHOT_SCREEN_WIDTH = props
                    .getIntProperty("SCREENSHOT_SCREEN_WIDTH");
            SCREENSHOT_SCREEN_HEIGHT = props
                    .getIntProperty("SCREENSHOT_SCREEN_HEIGHT");

            // Settings for the initial view
            INITIAL_SIMULATION_FRAME = props
                    .getIntProperty("INITIAL_SIMULATION_FRAME");
            INITIAL_ROTATION_X = props.getFloatProperty("INITIAL_ROTATION_X");
            INITIAL_ROTATION_Y = props.getFloatProperty("INITIAL_ROTATION_Y");
            INITIAL_ZOOM = props.getFloatProperty("INITIAL_ZOOM");
            TIME_STEP_SIZE = props.getIntProperty("TIME_STEP_SIZE");

            // Setting per movie frame
            MOVIE_ROTATE = props.getBooleanProperty("MOVIE_ROTATE");
            MOVIE_ROTATION_SPEED_MIN = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_MIN");
            MOVIE_ROTATION_SPEED_MAX = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_MAX");
            MOVIE_ROTATION_SPEED_DEF = props
                    .getFloatProperty("MOVIE_ROTATION_SPEED_DEF");

            // Settings for the gas cloud octree
            MAX_OCTREE_DEPTH = props.getIntProperty("MAX_OCTREE_DEPTH");
            OCTREE_EDGES = props.getFloatProperty("OCTREE_EDGES");

            // Settings that should never change, but are listed here to make
            // sure
            // they
            // can be found if necessary
            MAX_EXPECTED_MODELS = props.getIntProperty("MAX_EXPECTED_MODELS");

            SCREENSHOT_PATH = props.getProperty("SCREENSHOT_PATH");

            WAITTIME_FOR_RETRY = props.getLongProperty("WAITTIME_FOR_RETRY");
            WAITTIME_FOR_MOVIE = props.getLongProperty("WAITTIME_FOR_MOVIE");

            System.out.println(IMAGE_STREAM_OUTPUT ? "true" : "false");

            setIMAGE_STREAM_OUTPUT(props
                    .getBooleanProperty("IMAGE_STREAM_OUTPUT"));

            System.out.println(IMAGE_STREAM_OUTPUT ? "true" : "false");

            // grid_width_dimension_substring = props
            // .getProperty("grid_width_dimension_substring");
            // grid_height_dimension_substring = props
            // .getProperty("grid_height_dimension_substring");

        } catch (NumberFormatException e) {
            logger.warn(e.getMessage());
        }
    }

    public void setWaittimeBeforeRetry(long value) {
        WAITTIME_FOR_RETRY = value;
    }

    public void setWaittimeMovie(long value) {
        WAITTIME_FOR_MOVIE = value;
    }

    public void setEpsilon(float value) {
        EPSILON = value;
    }

    public void setFileExtensionLength(int value) {
        FILE_EXTENSION_LENGTH = value;
    }

    public void setFileNumberLength(int value) {
        FILE_NUMBER_LENGTH = value;
    }

    public void setCurrentExtension(String value) {
        CURRENT_POSTFIX = value;
    }

    public long getWaittimeBeforeRetry() {
        return WAITTIME_FOR_RETRY;
    }

    public long getWaittimeMovie() {
        return WAITTIME_FOR_MOVIE;
    }

    public float getEpsilon() {
        return EPSILON;
    }

    public int getFileExtensionLength() {
        return FILE_EXTENSION_LENGTH;
    }

    public int getFileNumberLength() {
        return FILE_NUMBER_LENGTH;
    }

    public String[] getAcceptableExtensions() {
        return ACCEPTABLE_POSTFIXES;
    }

    public String getCurrentExtension() {
        return CURRENT_POSTFIX;
    }

    public int getPreprocessAmount() {
        return PREPROCESSING_AMOUNT;
    }

    public void setPreprocessAmount(int value) {
        PREPROCESSING_AMOUNT = value;
    }

    public float getVarMax(String var) {
        return maxValues.get(var);
    }

    public float getVarDiffMax(String var) {
        return diffMaxValues.get(var);
    }

    public float getVarMin(String var) {
        return minValues.get(var);
    }

    public float getVarDiffMin(String var) {
        return diffMinValues.get(var);
    }

    public float getCurrentVarMax(String var) {
        return currentMaxValues.get(var);
    }

    public float getCurrentVarDiffMax(String var) {
        return currentDiffMaxValues.get(var);
    }

    public float getCurrentVarMin(String var) {
        return currentMinValues.get(var);
    }

    public float getCurrentVarDiffMin(String var) {
        return currentDiffMinValues.get(var);
    }

    public int getDepthMin() {
        return DEPTH_MIN;
    }

    public void setDepthMin(int value) {
        DEPTH_MIN = value;
    }

    public int getDepthDef() {
        return DEPTH_DEF;
    }

    public void setFrameNumber(int value) {
        SurfaceTextureDescription result = null, state;

        state = ltDescription;
        if (state.getFrameNumber() != value) {
            result = new SurfaceTextureDescription(value, state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            ltDescription = result;
        }

        state = rtDescription;
        if (state.getFrameNumber() != value) {
            result = new SurfaceTextureDescription(value, state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            rtDescription = result;
        }

        state = lbDescription;
        if (state.getFrameNumber() != value) {
            result = new SurfaceTextureDescription(value, state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            lbDescription = result;
        }

        state = rbDescription;
        if (state.getFrameNumber() != value) {
            result = new SurfaceTextureDescription(value, state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            rbDescription = result;
        }
    }

    public void setDepth(int value) {
        SurfaceTextureDescription result = null, state;

        state = ltDescription;
        if (state.getDepth() != value) {
            result = new SurfaceTextureDescription(state.getFrameNumber(),
                    value, state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            ltDescription = result;
        }

        state = rtDescription;
        if (state.getDepth() != value) {
            result = new SurfaceTextureDescription(state.getFrameNumber(),
                    value, state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            rtDescription = result;
        }

        state = lbDescription;
        if (state.getDepth() != value) {
            result = new SurfaceTextureDescription(state.getFrameNumber(),
                    value, state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            lbDescription = result;
        }

        state = rbDescription;
        if (state.getDepth() != value) {
            result = new SurfaceTextureDescription(state.getFrameNumber(),
                    value, state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), state.getLowerBound(),
                    state.getUpperBound());
            rbDescription = result;
        }

        DEPTH_DEF = value;
    }

    public int getDepthMax() {
        return DEPTH_MAX;
    }

    public void setDepthMax(int value) {
        DEPTH_MAX = value;
    }

    public void setWindowSelection(int i) {
        WINDOW_SELECTION = i;
    }

    public int getWindowSelection() {
        return WINDOW_SELECTION;
    }

    public String selectionToString(int windowSelection) {
        if (windowSelection == 1) {
            return "Left Top";
        } else if (windowSelection == 2) {
            return "Right Top";
        } else if (windowSelection == 3) {
            return "Left Bottom";
        } else if (windowSelection == 4) {
            return "Right Bottom";
        }

        return "All";
    }

    public synchronized void setLTDataMode(boolean dynamic, boolean diff,
            boolean secondSet) {
        SurfaceTextureDescription state = ltDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                state.getColorMap(), dynamic, diff, secondSet,
                state.getLowerBound(), state.getUpperBound());
        ltDescription = result;
    }

    public synchronized void setRTDataMode(boolean dynamic, boolean diff,
            boolean secondSet) {
        SurfaceTextureDescription state = rtDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                state.getColorMap(), dynamic, diff, secondSet,
                state.getLowerBound(), state.getUpperBound());
        rtDescription = result;
    }

    public synchronized void setLBDataMode(boolean dynamic, boolean diff,
            boolean secondSet) {
        SurfaceTextureDescription state = lbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                state.getColorMap(), dynamic, diff, secondSet,
                state.getLowerBound(), state.getUpperBound());
        lbDescription = result;
    }

    public synchronized void setRBDataMode(boolean dynamic, boolean diff,
            boolean secondSet) {
        SurfaceTextureDescription state = rbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                state.getColorMap(), dynamic, diff, secondSet,
                state.getLowerBound(), state.getUpperBound());
        rbDescription = result;
    }

    public synchronized void setLTVariable(String variable) {
        SurfaceTextureDescription state = ltDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), variable,
                state.getColorMap(), state.isDynamicDimensions(),
                state.isDiff(), state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        ltDescription = result;
    }

    public synchronized void setRTVariable(String variable) {
        SurfaceTextureDescription state = rtDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), variable,
                state.getColorMap(), state.isDynamicDimensions(),
                state.isDiff(), state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        rtDescription = result;
    }

    public synchronized void setLBVariable(String variable) {
        SurfaceTextureDescription state = lbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), variable,
                state.getColorMap(), state.isDynamicDimensions(),
                state.isDiff(), state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        lbDescription = result;
    }

    public synchronized void setRBVariable(String variable) {
        SurfaceTextureDescription state = rbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), variable,
                state.getColorMap(), state.isDynamicDimensions(),
                state.isDiff(), state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        rbDescription = result;
    }

    public synchronized SurfaceTextureDescription getLTSurfaceDescription() {
        return ltDescription;
    }

    public synchronized SurfaceTextureDescription getRTSurfaceDescription() {
        return rtDescription;
    }

    public synchronized SurfaceTextureDescription getLBSurfaceDescription() {
        return lbDescription;
    }

    public synchronized SurfaceTextureDescription getRBSurfaceDescription() {
        return rbDescription;
    }

    public synchronized void setLTColorMap(String selectedColorMap) {
        SurfaceTextureDescription state = ltDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                selectedColorMap, state.isDynamicDimensions(), state.isDiff(),
                state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        ltDescription = result;
    }

    public synchronized void setRTColorMap(String selectedColorMap) {
        SurfaceTextureDescription state = rtDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                selectedColorMap, state.isDynamicDimensions(), state.isDiff(),
                state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        rtDescription = result;
    }

    public synchronized void setLBColorMap(String selectedColorMap) {
        SurfaceTextureDescription state = lbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                selectedColorMap, state.isDynamicDimensions(), state.isDiff(),
                state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        lbDescription = result;
    }

    public synchronized void setRBColorMap(String selectedColorMap) {
        SurfaceTextureDescription state = rbDescription;
        SurfaceTextureDescription result = new SurfaceTextureDescription(
                state.getFrameNumber(), state.getDepth(), state.getVarName(),
                selectedColorMap, state.isDynamicDimensions(), state.isDiff(),
                state.isSecondSet(), state.getLowerBound(),
                state.getUpperBound());
        rbDescription = result;
    }

    public boolean isIMAGE_STREAM_OUTPUT() {
        return IMAGE_STREAM_OUTPUT;
    }

    public void setIMAGE_STREAM_OUTPUT(boolean value) {
        IMAGE_STREAM_OUTPUT = value;
    }

    public String getSAGE_DIRECTORY() {
        return SAGE_DIRECTORY;
    }

    public void setSAGE_DIRECTORY(String sAGE_DIRECTORY) {
        SAGE_DIRECTORY = sAGE_DIRECTORY;
    }

    public boolean isIMAGE_STREAM_GL_ONLY() {
        return IMAGE_STREAM_GL_ONLY;
    }

    public void setIMAGE_STREAM_GL_ONLY(boolean iMAGE_STREAM_GL_ONLY) {
        IMAGE_STREAM_GL_ONLY = iMAGE_STREAM_GL_ONLY;
    }

    public float getHeightDistortion() {
        return HEIGHT_DISTORION;
    }

    public float getHeightDistortionMin() {
        return HEIGHT_DISTORION_MIN;
    }

    public float getHeightDistortionMax() {
        return HEIGHT_DISTORION_MAX;
    }

    public void setHeightDistortion(float value) {
        HEIGHT_DISTORION = value;
    }

    public boolean isTouchConnected() {
        return TOUCH_CONNECTED;
    }

    public String getMonth(int frameNumber) {
        String result = "";
        if (frameNumber % 12 == 0) {
            result = "Jan";
        } else if (frameNumber % 12 == 1) {
            result = "Feb";
        } else if (frameNumber % 12 == 2) {
            result = "Mar";
        } else if (frameNumber % 12 == 3) {
            result = "Apr";
        } else if (frameNumber % 12 == 4) {
            result = "May";
        } else if (frameNumber % 12 == 5) {
            result = "Jun";
        } else if (frameNumber % 12 == 6) {
            result = "Jul";
        } else if (frameNumber % 12 == 7) {
            result = "Aug";
        } else if (frameNumber % 12 == 8) {
            result = "Sep";
        } else if (frameNumber % 12 == 9) {
            result = "Oct";
        } else if (frameNumber % 12 == 10) {
            result = "Nov";
        } else if (frameNumber % 12 == 11) {
            result = "Dec";
        }

        result += ", year " + (75 + (int) Math.floor(frameNumber / 12));

        return result;
    }

    public int getSageFramesPerSecond() {
        return SAGE_FRAMES_PER_SECOND;
    }

    public int getTimestep() {
        return TIME_STEP_SIZE;
    }

    public void setTimestep(int value) {
        System.out.println("Timestep set to: " + value);
        TIME_STEP_SIZE = value;
    }

    public boolean getStereo() {
        return STEREO_RENDERING;
    }

    public void setStereo(int stateChange) {
        if (stateChange == 1)
            STEREO_RENDERING = true;
        if (stateChange == 2)
            STEREO_RENDERING = false;
    }

    public boolean getStereoSwitched() {
        return STEREO_SWITCHED;
    }

    public void setStereoSwitched(int stateChange) {
        if (stateChange == 1)
            STEREO_SWITCHED = true;
        if (stateChange == 2)
            STEREO_SWITCHED = false;
    }

    public float getStereoOcularDistanceMin() {
        return STEREO_OCULAR_DISTANCE_MIN;
    }

    public float getStereoOcularDistanceMax() {
        return STEREO_OCULAR_DISTANCE_MAX;
    }

    public float getStereoOcularDistance() {
        return STEREO_OCULAR_DISTANCE_DEF;
    }

    public void setStereoOcularDistance(float value) {
        STEREO_OCULAR_DISTANCE_DEF = value;
    }

    public int getDefaultScreenWidth() {
        return DEFAULT_SCREEN_WIDTH;
    }

    public int getDefaultScreenHeight() {
        return DEFAULT_SCREEN_HEIGHT;
    }

    public int getScreenshotScreenWidth() {
        return SCREENSHOT_SCREEN_WIDTH;
    }

    public int getScreenshotScreenHeight() {
        return SCREENSHOT_SCREEN_HEIGHT;
    }

    public int getMaxOctreeDepth() {
        return MAX_OCTREE_DEPTH;
    }

    public float getOctreeEdges() {
        return OCTREE_EDGES;
    }

    public int getMaxExpectedModels() {
        return MAX_EXPECTED_MODELS;
    }

    public float getInitialRotationX() {
        return INITIAL_ROTATION_X;
    }

    public float getInitialRotationY() {
        return INITIAL_ROTATION_Y;
    }

    public float getInitialZoom() {
        return INITIAL_ZOOM;
    }

    public void setMovieRotate(int stateChange) {
        if (stateChange == 1)
            MOVIE_ROTATE = true;
        if (stateChange == 2)
            MOVIE_ROTATE = false;
    }

    public boolean getMovieRotate() {
        return MOVIE_ROTATE;
    }

    public void setMovieRotationSpeed(float value) {
        MOVIE_ROTATION_SPEED_DEF = value;
    }

    public float getMovieRotationSpeedMin() {
        return MOVIE_ROTATION_SPEED_MIN;
    }

    public float getMovieRotationSpeedMax() {
        return MOVIE_ROTATION_SPEED_MAX;
    }

    public float getMovieRotationSpeedDef() {
        return MOVIE_ROTATION_SPEED_DEF;
    }

    public int getInitialSimulationFrame() {
        return INITIAL_SIMULATION_FRAME;
    }

    public void setInitial_simulation_frame(int initialSimulationFrame) {
        INITIAL_SIMULATION_FRAME = initialSimulationFrame;
    }

    public void setInitial_rotation_x(float initialRotationX) {
        INITIAL_ROTATION_X = initialRotationX;
    }

    public void setInitial_rotation_y(float initialRotationY) {
        INITIAL_ROTATION_Y = initialRotationY;
    }

    public String getScreenshotPath() {
        return SCREENSHOT_PATH;
    }

    public void setScreenshotPath(String newPath) {
        SCREENSHOT_PATH = newPath;
    }

    public void setVariableRange(int whichglobe, String varName,
            int sliderLowerValue, int sliderUpperValue) {

        float diff = (maxValues.get(varName) - minValues.get(varName));

        currentMinValues.put(varName, (sliderLowerValue / 100f) * diff
                + minValues.get(varName));
        currentMaxValues.put(varName, (sliderUpperValue / 100f) * diff
                + minValues.get(varName));
        float minFloatValue = currentMinValues.get(varName);
        float maxFloatValue = currentMaxValues.get(varName);

        if (whichglobe == 0) {
            SurfaceTextureDescription state = ltDescription;
            SurfaceTextureDescription result = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), minFloatValue, maxFloatValue);
            ltDescription = result;
        } else if (whichglobe == 1) {
            SurfaceTextureDescription state = rtDescription;
            SurfaceTextureDescription result = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), minFloatValue, maxFloatValue);
            rtDescription = result;
        } else if (whichglobe == 2) {
            SurfaceTextureDescription state = lbDescription;
            SurfaceTextureDescription result = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), minFloatValue, maxFloatValue);
            lbDescription = result;
        } else if (whichglobe == 3) {
            SurfaceTextureDescription state = rbDescription;
            SurfaceTextureDescription result = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(),
                    state.getVarName(), state.getColorMap(),
                    state.isDynamicDimensions(), state.isDiff(),
                    state.isSecondSet(), minFloatValue, maxFloatValue);
            rbDescription = result;
        }
    }

    // public int getImageWidth() {
    // return IMAGE_WIDTH;
    // }
    //
    // public int getImageHeight() {
    // return IMAGE_HEIGHT;
    // }

    public int getRangeSliderLowerValue(int whichglobe) {
        SurfaceTextureDescription state = null;

        if (whichglobe == 0) {
            state = ltDescription;
        } else if (whichglobe == 1) {
            state = rtDescription;
        } else if (whichglobe == 2) {
            state = lbDescription;
        } else if (whichglobe == 3) {
            state = rbDescription;
        }

        float min = getVarMin(state.getVarName());
        float max = getVarMax(state.getVarName());
        float currentMin = getCurrentVarMin(state.getVarName());

        float diff = max - min;
        float result = (currentMin - min) / diff;

        return (int) (result * 100) - 1;
    }

    public int getRangeSliderUpperValue(int whichglobe) {
        SurfaceTextureDescription state = null;

        if (whichglobe == 0) {
            state = ltDescription;
        } else if (whichglobe == 1) {
            state = rtDescription;
        } else if (whichglobe == 2) {
            state = lbDescription;
        } else if (whichglobe == 3) {
            state = rbDescription;
        }

        float min = getVarMin(state.getVarName());
        float max = getVarMax(state.getVarName());
        float currentMax = getCurrentVarMax(state.getVarName());

        float diff = max - min;
        float result = (currentMax - min) / diff;

        return (int) (result * 100) - 1;
    }

    public String getWidthSubstring() {
        return grid_width_dimension_substring;
    }

    public String getHeightSubstring() {
        return grid_height_dimension_substring;
    }

    public int getNumScreens() {
        return MAX_NUMBER_OF_SCREENS;
    }

    // public String verbalizeDataMode(int index) {
    // String result = "";
    //
    // if (index == 0) {
    // result = "Control";
    // } else if (index == 1) {
    // result = "0.5 Sv";
    // } else if (index == 2) {
    // result = "Difference";
    // }
    //
    // return result;
    // }
}
