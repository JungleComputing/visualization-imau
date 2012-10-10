package imau.visualization;

import imau.visualization.adaptor.GlobeState;
import imau.visualization.adaptor.GlobeState.Variable;
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

    private boolean        STEREO_RENDERING           = true;
    private boolean        STEREO_SWITCHED            = true;

    private float          STEREO_OCULAR_DISTANCE_MIN = 0f;
    private float          STEREO_OCULAR_DISTANCE_DEF = .2f;
    private float          STEREO_OCULAR_DISTANCE_MAX = 1f;

    // Size settings for default startup and screenshots
    private int            DEFAULT_SCREEN_WIDTH       = 1024;
    private int            DEFAULT_SCREEN_HEIGHT      = 768;

    private int            SCREENSHOT_SCREEN_WIDTH    = 1280;
    private int            SCREENSHOT_SCREEN_HEIGHT   = 720;

    // Settings for the initial view
    private int            INITIAL_SIMULATION_FRAME   = 0;
    private float          INITIAL_ROTATION_X         = 17f;
    private float          INITIAL_ROTATION_Y         = -25f;
    private float          INITIAL_ZOOM               = -390.0f;

    // Setting per movie frame
    private boolean        MOVIE_ROTATE               = true;
    private float          MOVIE_ROTATION_SPEED_MIN   = -1f;
    private float          MOVIE_ROTATION_SPEED_MAX   = 1f;
    private float          MOVIE_ROTATION_SPEED_DEF   = -0.25f;

    // Settings for the gas cloud octree
    private int            MAX_OCTREE_DEPTH           = 25;
    private float          OCTREE_EDGES               = 800f;

    // Settings that should never change, but are listed here to make sure they
    // can be found if necessary
    private int            MAX_EXPECTED_MODELS        = 1000;

    protected String       SCREENSHOT_PATH            = System.getProperty("user.dir")
                                                              + System.getProperty("path.separator");

    private GlobeState     globeStateLT               = new GlobeState(
                                                              GlobeState.DataMode.FIRST_DATASET,
                                                              false,
                                                              GlobeState.Variable.TEMP,
                                                              75, 0, "default");
    private GlobeState     globeStateRT               = new GlobeState(
                                                              GlobeState.DataMode.FIRST_DATASET,
                                                              false,
                                                              GlobeState.Variable.KE,
                                                              75, 0, "rainbow");
    private GlobeState     globeStateLB               = new GlobeState(
                                                              GlobeState.DataMode.FIRST_DATASET,
                                                              false,
                                                              GlobeState.Variable.SALT,
                                                              75, 0, "inv_diff");
    private GlobeState     globeStateRB               = new GlobeState(
                                                              GlobeState.DataMode.FIRST_DATASET,
                                                              false,
                                                              GlobeState.Variable.HMXL,
                                                              75, 0, "hotres");

    private long           WAITTIME_FOR_RETRY         = 10000;
    private long           WAITTIME_FOR_MOVIE         = 1000;
    private int            TIME_STEP_SIZE             = 1;
    private float          EPSILON                    = 1.0E-7f;

    private int            FILE_EXTENSION_LENGTH      = 2;
    private int            FILE_NUMBER_LENGTH         = 4;

    private final String[] ACCEPTABLE_POSTFIXES       = { ".nc" };

    private String         CURRENT_POSTFIX            = "nc";

    private int            PREPROCESSING_AMOUNT       = 2;

    private float          MIN_SSH                    = -200f;
    private float          MAX_SSH                    = 100f;
    private float          MIN_DIFF_SSH               = -100f;
    private float          MAX_DIFF_SSH               = 100f;

    private float          MIN_SHF                    = -400f;
    private float          MAX_SHF                    = 250f;
    private float          MIN_DIFF_SHF               = -150f;
    private float          MAX_DIFF_SHF               = 150f;

    private float          MIN_SFWF                   = -3E-4f;
    private float          MAX_SFWF                   = 3E-4f;
    private float          MIN_DIFF_SFWF              = -1E-4f;
    private float          MAX_DIFF_SFWF              = 1E-4f;

    private float          MIN_HMXL                   = 0f;
    private float          MAX_HMXL                   = 150000f;
    private float          MIN_DIFF_HMXL              = -50000f;
    private float          MAX_DIFF_HMXL              = 50000f;

    private float          MIN_SALT                   = 0.03f;
    private float          MAX_SALT                   = 0.04f;
    private float          MIN_DIFF_SALT              = -0.025f;
    private float          MAX_DIFF_SALT              = 0.025f;

    private float          MIN_TEMP                   = -2f;
    private float          MAX_TEMP                   = 30f;
    private float          MIN_DIFF_TEMP              = -15f;
    private float          MAX_DIFF_TEMP              = 15f;

    private float          MIN_UVEL                   = -200f;
    private float          MAX_UVEL                   = 200f;
    private float          MIN_DIFF_UVEL              = -100f;
    private float          MAX_DIFF_UVEL              = 100f;

    private float          MIN_VVEL                   = -200f;
    private float          MAX_VVEL                   = 200f;
    private float          MIN_DIFF_VVEL              = -100f;
    private float          MAX_DIFF_VVEL              = 100f;

    private float          MIN_KE                     = 0f;
    private float          MAX_KE                     = 10000f;
    private float          MIN_DIFF_KE                = -5000f;
    private float          MAX_DIFF_KE                = 5000f;

    private float          MIN_PD                     = 1f;
    private float          MAX_PD                     = 1.04f;
    private float          MIN_DIFF_PD                = -0.01f;
    private float          MAX_DIFF_PD                = 0.01f;

    private float          MIN_TAUX                   = -1f;
    private float          MAX_TAUX                   = 1f;
    private float          MIN_DIFF_TAUX              = -.5f;
    private float          MAX_DIFF_TAUX              = .5f;

    private float          MIN_TAUY                   = -1f;
    private float          MAX_TAUY                   = 1f;
    private float          MIN_DIFF_TAUY              = -.5f;
    private float          MAX_DIFF_TAUY              = .5f;

    private float          MIN_H2                     = 0f;
    private float          MAX_H2                     = 100000f;
    private float          MIN_DIFF_H2                = -50000f;
    private float          MAX_DIFF_H2                = 50000f;

    private int            DEPTH_MIN                  = 0;
    private int            DEPTH_DEF                  = 0;
    private int            DEPTH_MAX                  = 41;

    private int            WINDOW_SELECTION           = 0;

    private boolean        DYNAMIC_DIMENSIONS         = false;

    private boolean        IMAGE_STREAM_OUTPUT        = false;
    private final int      SAGE_FRAMES_PER_SECOND     = 10;
    private boolean        IMAGE_STREAM_GL_ONLY       = true;

    private float          HEIGHT_DISTORION           = 0f;
    private final float    HEIGHT_DISTORION_MIN       = 0f;
    private final float    HEIGHT_DISTORION_MAX       = .01f;

    private String         SAGE_DIRECTORY             = "/home/maarten/sage-code/sage";

    private final boolean  TOUCH_CONNECTED            = false;

    private final String[] POSSIBLE_LAT_AXIS_NAMES    = { "t_lat", "TLAT",
            "T_LAT", "tlat", "lat_t", "latt", "LATT", "u_lat", "ULAT", "U_LAT",
            "ulat", "lat_u", "latu", "LATU", "nlat", "NLAT", "latn", "LATN",
            "n_lat", "N_LAT", "lat_n", "LAT_N"       };
    private final String[] POSSIBLE_LON_AXIS_NAMES    = { "t_lon", "TLON",
            "T_LON", "tlon", "lon_t", "lont", "LONT", "t_long", "TLONG",
            "T_LONG", "tlong", "long_t", "longt", "LONGT", "u_lon", "ULON",
            "U_LON", "ulon", "lon_u", "lonu", "LONU", "u_long", "ULONG",
            "U_LONG", "ulong", "long_u", "longu", "LONGU", "nlon", "NLON",
            "lonn", "LONN", "n_lon", "N_LON", "lon_n", "LON_N", "nlong",
            "NLONG", "longn", "LONGN", "n_long", "N_LONG", "long_n", "LONG_N" };
    private final String[] POSSIBLE_DEPTH_AXIS_NAMES  = { "t_depth", "TDEPTH",
            "T_DEPTH", "tdepth", "depth_t", "deptht", "DEPTHT", "ZT", "zt",
            "Z_T", "z_t", "TZ", "tz", "T_Z", "t_z", "u_depth", "UDEPTH",
            "U_DEPTH", "udepth", "depth_u", "depthu", "DEPTHU", "ZU", "zu",
            "Z_U", "z_u", "UZ", "uz", "U_Z", "u_z"   };

    private final String   LAT_SUBSTRING              = "lat";
    private final String   LON_SUBSTRING              = "lon";

    private ImauSettings() {
        super();

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

            // PREPROCESSING_AMOUNT = props
            // .getIntProperty("PREPROCESSING_AMOUNT");
            //
            // MAX_SSH = props.getFloatProperty("MAX_SSH");
            // MIN_SSH = props.getFloatProperty("MIN_SSH");
            // MAX_DIFF_SSH =
            // props.getFloatProperty("MAX_DIFF_SSH");
            // MIN_DIFF_SSH =
            // props.getFloatProperty("MIN_DIFF_SSH");
            //
            // MAX_SHF = props.getFloatProperty("MAX_SHF");
            // MIN_SHF = props.getFloatProperty("MIN_SHF");
            // MAX_DIFF_SHF =
            // props.getFloatProperty("MAX_DIFF_SHF");
            // MIN_DIFF_SHF =
            // props.getFloatProperty("MIN_DIFF_SHF");
            //
            // MAX_SFWF = props.getFloatProperty("MAX_SFWF");
            // MIN_SFWF = props.getFloatProperty("MIN_SFWF");
            // MAX_DIFF_SFWF = props
            // .getFloatProperty("MAX_DIFF_SFWF");
            // MIN_DIFF_SFWF = props
            // .getFloatProperty("MIN_DIFF_SFWF");
            //
            // MAX_HMXL = props.getFloatProperty("MAX_HMXL");
            // MIN_HMXL = props.getFloatProperty("MIN_HMXL");
            // MAX_DIFF_HMXL = props
            // .getFloatProperty("MAX_DIFF_HMXL");
            // MIN_DIFF_HMXL = props
            // .getFloatProperty("MIN_DIFF_HMXL");
            //
            // MAX_SALT = props.getFloatProperty("MAX_SALT");
            // MIN_SALT = props.getFloatProperty("MIN_SALT");
            // MAX_DIFF_SALT = props
            // .getFloatProperty("MAX_DIFF_SALT");
            // MIN_DIFF_SALT = props
            // .getFloatProperty("MIN_DIFF_SALT");
            //
            // MAX_TEMP = props.getFloatProperty("MAX_TEMP");
            // MIN_TEMP = props.getFloatProperty("MIN_TEMP");
            // MAX_DIFF_TEMP = props
            // .getFloatProperty("MAX_DIFF_TEMP");
            // MIN_DIFF_TEMP = props
            // .getFloatProperty("MIN_DIFF_TEMP");
            //
            // DEPTH_MIN = props.getIntProperty("DEPTH_MIN");
            // DEPTH_DEF = props.getIntProperty("DEPTH_DEF");
            // DEPTH_MAX = props.getIntProperty("DEPTH_MAX");
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

    public void setVarMax(Variable var, float value) {
        if (var == Variable.SSH) {
            MAX_SSH = value;
        } else if (var == Variable.SHF) {
            MAX_SHF = value;
        } else if (var == Variable.SFWF) {
            MAX_SFWF = value;
        } else if (var == Variable.HMXL) {
            MAX_HMXL = value;
        } else if (var == Variable.SALT) {
            MAX_SALT = value;
        } else if (var == Variable.TEMP) {
            MAX_TEMP = value;
        } else if (var == Variable.UVEL) {
            MAX_UVEL = value;
        } else if (var == Variable.VVEL) {
            MAX_VVEL = value;
        } else if (var == Variable.KE) {
            MAX_KE = value;
        } else if (var == Variable.PD) {
            MAX_PD = value;
        } else if (var == Variable.TAUX) {
            MAX_TAUX = value;
        } else if (var == Variable.TAUY) {
            MAX_TAUY = value;
        } else if (var == Variable.H2) {
            MAX_H2 = value;
        }
    }

    public void setVarDiffMax(Variable var, float value) {
        if (var == Variable.SSH) {
            MAX_DIFF_SSH = value;
        } else if (var == Variable.SHF) {
            MAX_DIFF_SHF = value;
        } else if (var == Variable.SFWF) {
            MAX_DIFF_SFWF = value;
        } else if (var == Variable.HMXL) {
            MAX_DIFF_HMXL = value;
        } else if (var == Variable.SALT) {
            MAX_DIFF_SALT = value;
        } else if (var == Variable.TEMP) {
            MAX_DIFF_TEMP = value;
        } else if (var == Variable.UVEL) {
            MAX_DIFF_UVEL = value;
        } else if (var == Variable.VVEL) {
            MAX_DIFF_VVEL = value;
        } else if (var == Variable.KE) {
            MAX_DIFF_KE = value;
        } else if (var == Variable.PD) {
            MAX_DIFF_PD = value;
        } else if (var == Variable.TAUX) {
            MAX_DIFF_TAUX = value;
        } else if (var == Variable.TAUY) {
            MAX_DIFF_TAUY = value;
        } else if (var == Variable.H2) {
            MAX_DIFF_H2 = value;
        }
    }

    public void setVarMin(Variable var, float value) {
        if (var == Variable.SSH) {
            MIN_SSH = value;
        } else if (var == Variable.SHF) {
            MIN_SHF = value;
        } else if (var == Variable.SFWF) {
            MIN_SFWF = value;
        } else if (var == Variable.HMXL) {
            MIN_HMXL = value;
        } else if (var == Variable.SALT) {
            MIN_SALT = value;
        } else if (var == Variable.TEMP) {
            MIN_TEMP = value;
        } else if (var == Variable.UVEL) {
            MIN_UVEL = value;
        } else if (var == Variable.VVEL) {
            MIN_VVEL = value;
        } else if (var == Variable.KE) {
            MIN_KE = value;
        } else if (var == Variable.PD) {
            MIN_PD = value;
        } else if (var == Variable.TAUX) {
            MIN_TAUX = value;
        } else if (var == Variable.TAUY) {
            MIN_TAUY = value;
        } else if (var == Variable.H2) {
            MIN_H2 = value;
        }
    }

    public void setVarDiffMin(Variable var, float value) {
        if (var == Variable.SSH) {
            MIN_DIFF_SSH = value;
        } else if (var == Variable.SHF) {
            MIN_DIFF_SHF = value;
        } else if (var == Variable.SFWF) {
            MIN_DIFF_SFWF = value;
        } else if (var == Variable.HMXL) {
            MIN_DIFF_HMXL = value;
        } else if (var == Variable.SALT) {
            MIN_DIFF_SALT = value;
        } else if (var == Variable.TEMP) {
            MIN_DIFF_TEMP = value;
        } else if (var == Variable.UVEL) {
            MIN_DIFF_UVEL = value;
        } else if (var == Variable.VVEL) {
            MIN_DIFF_VVEL = value;
        } else if (var == Variable.KE) {
            MIN_DIFF_KE = value;
        } else if (var == Variable.PD) {
            MIN_DIFF_PD = value;
        } else if (var == Variable.TAUX) {
            MIN_DIFF_TAUX = value;
        } else if (var == Variable.TAUY) {
            MIN_DIFF_TAUY = value;
        } else if (var == Variable.H2) {
            MIN_DIFF_H2 = value;
        }
    }

    public float getVarMax(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = MAX_SSH;
        } else if (var == Variable.SHF) {
            result = MAX_SHF;
        } else if (var == Variable.SFWF) {
            result = MAX_SFWF;
        } else if (var == Variable.HMXL) {
            result = MAX_HMXL;
        } else if (var == Variable.SALT) {
            result = MAX_SALT;
        } else if (var == Variable.TEMP) {
            result = MAX_TEMP;
        } else if (var == Variable.UVEL) {
            result = MAX_UVEL;
        } else if (var == Variable.VVEL) {
            result = MAX_VVEL;
        } else if (var == Variable.KE) {
            result = MAX_KE;
        } else if (var == Variable.PD) {
            result = MAX_PD;
        } else if (var == Variable.TAUX) {
            result = MAX_TAUX;
        } else if (var == Variable.TAUY) {
            result = MAX_TAUY;
        } else if (var == Variable.H2) {
            result = MAX_H2;
        }
        return result;
    }

    public float getVarDiffMax(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = MAX_DIFF_SSH;
        } else if (var == Variable.SHF) {
            result = MAX_DIFF_SHF;
        } else if (var == Variable.SFWF) {
            result = MAX_DIFF_SFWF;
        } else if (var == Variable.HMXL) {
            result = MAX_DIFF_HMXL;
        } else if (var == Variable.SALT) {
            result = MAX_DIFF_SALT;
        } else if (var == Variable.TEMP) {
            result = MAX_DIFF_TEMP;
        } else if (var == Variable.UVEL) {
            result = MAX_DIFF_UVEL;
        } else if (var == Variable.VVEL) {
            result = MAX_DIFF_VVEL;
        } else if (var == Variable.KE) {
            result = MAX_DIFF_KE;
        } else if (var == Variable.PD) {
            result = MAX_DIFF_PD;
        } else if (var == Variable.TAUX) {
            result = MAX_DIFF_TAUX;
        } else if (var == Variable.TAUY) {
            result = MAX_DIFF_TAUY;
        } else if (var == Variable.H2) {
            result = MAX_DIFF_H2;
        }
        return result;
    }

    public float getVarMin(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = MIN_SSH;
        } else if (var == Variable.SHF) {
            result = MIN_SHF;
        } else if (var == Variable.SFWF) {
            result = MIN_SFWF;
        } else if (var == Variable.HMXL) {
            result = MIN_HMXL;
        } else if (var == Variable.SALT) {
            result = MIN_SALT;
        } else if (var == Variable.TEMP) {
            result = MIN_TEMP;
        } else if (var == Variable.UVEL) {
            result = MIN_UVEL;
        } else if (var == Variable.VVEL) {
            result = MIN_VVEL;
        } else if (var == Variable.KE) {
            result = MIN_KE;
        } else if (var == Variable.PD) {
            result = MIN_PD;
        } else if (var == Variable.TAUX) {
            result = MIN_TAUX;
        } else if (var == Variable.TAUY) {
            result = MIN_TAUY;
        } else if (var == Variable.H2) {
            result = MIN_H2;
        }
        return result;
    }

    public float getVarDiffMin(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = MIN_DIFF_SSH;
        } else if (var == Variable.SHF) {
            result = MIN_DIFF_SHF;
        } else if (var == Variable.SFWF) {
            result = MIN_DIFF_SFWF;
        } else if (var == Variable.HMXL) {
            result = MIN_DIFF_HMXL;
        } else if (var == Variable.SALT) {
            result = MIN_DIFF_SALT;
        } else if (var == Variable.TEMP) {
            result = MIN_DIFF_TEMP;
        } else if (var == Variable.UVEL) {
            result = MIN_DIFF_UVEL;
        } else if (var == Variable.VVEL) {
            result = MIN_DIFF_VVEL;
        } else if (var == Variable.KE) {
            result = MIN_DIFF_KE;
        } else if (var == Variable.PD) {
            result = MIN_DIFF_PD;
        } else if (var == Variable.TAUX) {
            result = MIN_DIFF_TAUX;
        } else if (var == Variable.TAUY) {
            result = MIN_DIFF_TAUY;
        } else if (var == Variable.H2) {
            result = MIN_DIFF_H2;
        }
        return result;
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
        GlobeState result;
        GlobeState state;

        state = globeStateLT;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap());
            globeStateRB = result;
        }
    }

    public void setDepth(int value) {
        GlobeState result;
        GlobeState state;

        state = globeStateLT;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap());
            globeStateRB = result;
        }

        DEPTH_DEF = value;
    }

    public int getDepthMax() {
        return DEPTH_MAX;
    }

    public void setDepthMax(int value) {
        DEPTH_MAX = value;
    }

    public String bandNameToString(Variable var) {
        if (var == Variable.SSH) {
            return "Sea Surface Height";
        } else if (var == Variable.SHF) {
            return "Total Surface Heat Flux";
        } else if (var == Variable.SFWF) {
            return "Virtual Salt Flux ";
        } else if (var == Variable.HMXL) {
            return "Mixed Layer Depth";
        } else if (var == Variable.SALT) {
            return "Salinity";
        } else if (var == Variable.TEMP) {
            return "Potential Temperature";
        } else if (var == Variable.UVEL) {
            return "Velocity in grid-x dir.";
        } else if (var == Variable.VVEL) {
            return "Velocity in grid-y dir.";
        } else if (var == Variable.KE) {
            return "Horizontal Kinetic Energy";
        } else if (var == Variable.PD) {
            return "Potential Density";
        } else if (var == Variable.TAUX) {
            return "Windstress in grid-x dir.";
        } else if (var == Variable.TAUY) {
            return "Windstress in grid-y dir.";
        } else if (var == Variable.H2) {
            return "Sea surface height ^2";
        }

        return "";
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

    public synchronized void setLTDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateLT;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap());
        globeStateLT = result;
    }

    public synchronized void setRTDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap());
        globeStateRT = result;
    }

    public synchronized void setLBDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap());
        globeStateLB = result;
    }

    public synchronized void setRBDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap());
        globeStateRB = result;
    }

    public synchronized void setLTVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateLT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap());
        globeStateLT = result;
    }

    public synchronized void setRTVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap());
        globeStateRT = result;
    }

    public synchronized void setLBVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap());
        globeStateLB = result;
    }

    public synchronized void setRBVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap());
        globeStateRB = result;
    }

    public synchronized GlobeState getLTState() {
        return globeStateLT;
    }

    public synchronized GlobeState getRTState() {
        return globeStateRT;
    }

    public synchronized GlobeState getLBState() {
        return globeStateLB;
    }

    public synchronized GlobeState getRBState() {
        return globeStateRB;
    }

    public synchronized void setLTColorMap(String selectedColorMap) {
        GlobeState state = globeStateLT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap);
        globeStateLT = result;
    }

    public synchronized void setRTColorMap(String selectedColorMap) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap);
        globeStateRT = result;
    }

    public synchronized void setLBColorMap(String selectedColorMap) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap);
        globeStateLB = result;
    }

    public synchronized void setRBColorMap(String selectedColorMap) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap);
        globeStateRB = result;
    }

    public void setDynamicDimensions(boolean value) {
        DYNAMIC_DIMENSIONS = value;

        GlobeState result;
        GlobeState state;

        state = globeStateLT;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap());
            globeStateRB = result;
        }
    }

    public boolean isDynamicDimensions() {
        return DYNAMIC_DIMENSIONS;
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

    public String[] getLatNamePermutations() {
        return POSSIBLE_LAT_AXIS_NAMES;
    }

    public String[] getLonNamePermutations() {
        return POSSIBLE_LON_AXIS_NAMES;
    }

    public String[] getDepthNamePermutations() {
        return POSSIBLE_DEPTH_AXIS_NAMES;
    }

    public float getVarMax(String varName) {
        float result = 0f;
        if (varName.compareTo("SSH") == 0) {
            result = MAX_SSH;
        } else if (varName.compareTo("SHF") == 0) {
            result = MAX_SHF;
        } else if (varName.compareTo("SFWF") == 0) {
            result = MAX_SFWF;
        } else if (varName.compareTo("HMXL") == 0) {
            result = MAX_HMXL;
        } else if (varName.compareTo("SALT") == 0) {
            result = MAX_SALT;
        } else if (varName.compareTo("TEMP") == 0) {
            result = MAX_TEMP;
        } else if (varName.compareTo("UVEL") == 0) {
            result = MAX_UVEL;
        } else if (varName.compareTo("VVEL") == 0) {
            result = MAX_VVEL;
        } else if (varName.compareTo("KE") == 0) {
            result = MAX_KE;
        } else if (varName.compareTo("PD") == 0) {
            result = MAX_PD;
        } else if (varName.compareTo("TAUX") == 0) {
            result = MAX_TAUX;
        } else if (varName.compareTo("TAUY") == 0) {
            result = MAX_TAUY;
        } else if (varName.compareTo("H2") == 0) {
            result = MAX_H2;
        }
        return result;
    }

    public float getVarMin(String varName) {
        float result = 0f;
        if (varName.compareTo("SSH") == 0) {
            result = MIN_SSH;
        } else if (varName.compareTo("SHF") == 0) {
            result = MIN_SHF;
        } else if (varName.compareTo("SFWF") == 0) {
            result = MIN_SFWF;
        } else if (varName.compareTo("HMXL") == 0) {
            result = MIN_HMXL;
        } else if (varName.compareTo("SALT") == 0) {
            result = MIN_SALT;
        } else if (varName.compareTo("TEMP") == 0) {
            result = MIN_TEMP;
        } else if (varName.compareTo("UVEL") == 0) {
            result = MIN_UVEL;
        } else if (varName.compareTo("VVEL") == 0) {
            result = MIN_VVEL;
        } else if (varName.compareTo("KE") == 0) {
            result = MIN_KE;
        } else if (varName.compareTo("PD") == 0) {
            result = MIN_PD;
        } else if (varName.compareTo("TAUX") == 0) {
            result = MIN_TAUX;
        } else if (varName.compareTo("TAUY") == 0) {
            result = MIN_TAUY;
        } else if (varName.compareTo("H2") == 0) {
            result = MIN_H2;
        }
        return result;
    }

    public float getVarDiffMax(String varName) {
        float result = 0f;
        if (varName.compareTo("SSH") == 0) {
            result = MAX_DIFF_SSH;
        } else if (varName.compareTo("SHF") == 0) {
            result = MAX_DIFF_SHF;
        } else if (varName.compareTo("SFWF") == 0) {
            result = MAX_DIFF_SFWF;
        } else if (varName.compareTo("HMXL") == 0) {
            result = MAX_DIFF_HMXL;
        } else if (varName.compareTo("SALT") == 0) {
            result = MAX_DIFF_SALT;
        } else if (varName.compareTo("TEMP") == 0) {
            result = MAX_DIFF_TEMP;
        } else if (varName.compareTo("UVEL") == 0) {
            result = MAX_DIFF_UVEL;
        } else if (varName.compareTo("VVEL") == 0) {
            result = MAX_DIFF_VVEL;
        } else if (varName.compareTo("KE") == 0) {
            result = MAX_DIFF_KE;
        } else if (varName.compareTo("PD") == 0) {
            result = MAX_DIFF_PD;
        } else if (varName.compareTo("TAUX") == 0) {
            result = MAX_DIFF_TAUX;
        } else if (varName.compareTo("TAUY") == 0) {
            result = MAX_DIFF_TAUY;
        } else if (varName.compareTo("H2") == 0) {
            result = MAX_DIFF_H2;
        }
        return result;
    }

    public float getVarDiffMin(String varName) {
        float result = 0f;
        if (varName.compareTo("SSH") == 0) {
            result = MIN_DIFF_SSH;
        } else if (varName.compareTo("SHF") == 0) {
            result = MIN_DIFF_SHF;
        } else if (varName.compareTo("SFWF") == 0) {
            result = MIN_DIFF_SFWF;
        } else if (varName.compareTo("HMXL") == 0) {
            result = MIN_DIFF_HMXL;
        } else if (varName.compareTo("SALT") == 0) {
            result = MIN_DIFF_SALT;
        } else if (varName.compareTo("TEMP") == 0) {
            result = MIN_DIFF_TEMP;
        } else if (varName.compareTo("UVEL") == 0) {
            result = MIN_DIFF_UVEL;
        } else if (varName.compareTo("VVEL") == 0) {
            result = MIN_DIFF_VVEL;
        } else if (varName.compareTo("KE") == 0) {
            result = MIN_DIFF_KE;
        } else if (varName.compareTo("PD") == 0) {
            result = MIN_DIFF_PD;
        } else if (varName.compareTo("TAUX") == 0) {
            result = MIN_DIFF_TAUX;
        } else if (varName.compareTo("TAUY") == 0) {
            result = MIN_DIFF_TAUY;
        } else if (varName.compareTo("H2") == 0) {
            result = MIN_DIFF_H2;
        }
        return result;
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
}
