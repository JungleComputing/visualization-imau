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

    private static final int IMAGE_WIDTH                = 900;
    private static final int IMAGE_HEIGHT               = 602;

    private boolean          STEREO_RENDERING           = true;
    private boolean          STEREO_SWITCHED            = true;

    private float            STEREO_OCULAR_DISTANCE_MIN = 0f;
    private float            STEREO_OCULAR_DISTANCE_DEF = .2f;
    private float            STEREO_OCULAR_DISTANCE_MAX = 1f;

    // Size settings for default startup and screenshots
    private int              DEFAULT_SCREEN_WIDTH       = 1024;
    private int              DEFAULT_SCREEN_HEIGHT      = 768;

    private int              SCREENSHOT_SCREEN_WIDTH    = 1280;
    private int              SCREENSHOT_SCREEN_HEIGHT   = 720;

    // Settings for the initial view
    private int              INITIAL_SIMULATION_FRAME   = 0;
    private float            INITIAL_ROTATION_X         = 17f;
    private float            INITIAL_ROTATION_Y         = -25f;
    private float            INITIAL_ZOOM               = -390.0f;

    // Setting per movie frame
    private boolean          MOVIE_ROTATE               = true;
    private float            MOVIE_ROTATION_SPEED_MIN   = -1f;
    private float            MOVIE_ROTATION_SPEED_MAX   = 1f;
    private float            MOVIE_ROTATION_SPEED_DEF   = -0.25f;

    // Settings for the gas cloud octree
    private int              MAX_OCTREE_DEPTH           = 25;
    private float            OCTREE_EDGES               = 800f;

    // Settings that should never change, but are listed here to make sure they
    // can be found if necessary
    private int              MAX_EXPECTED_MODELS        = 1000;

    protected String         SCREENSHOT_PATH            = System.getProperty("user.dir")
                                                                + System.getProperty("path.separator");

    private long             WAITTIME_FOR_RETRY         = 10000;
    private long             WAITTIME_FOR_MOVIE         = 1000;
    private int              TIME_STEP_SIZE             = 1;
    private float            EPSILON                    = 1.0E-7f;

    private int              FILE_EXTENSION_LENGTH      = 2;
    private int              FILE_NUMBER_LENGTH         = 4;

    private final String[]   ACCEPTABLE_POSTFIXES       = { ".nc" };

    private String           CURRENT_POSTFIX            = "nc";

    private int              PREPROCESSING_AMOUNT       = 2;

    private final float      MIN_SSH                    = -200f;
    private final float      MAX_SSH                    = 100f;
    private float            CURRENT_MIN_SSH            = -200f;
    private float            CURRENT_MAX_SSH            = 100f;
    private final float      MIN_DIFF_SSH               = -100f;
    private final float      MAX_DIFF_SSH               = 100f;
    private float            CURRENT_MIN_DIFF_SSH       = -100f;
    private float            CURRENT_MAX_DIFF_SSH       = 100f;

    private final float      MIN_SHF                    = -400f;
    private final float      MAX_SHF                    = 250f;
    private float            CURRENT_MIN_SHF            = -400f;
    private float            CURRENT_MAX_SHF            = 250f;
    private final float      MIN_DIFF_SHF               = -150f;
    private final float      MAX_DIFF_SHF               = 150f;
    private float            CURRENT_MIN_DIFF_SHF       = -150f;
    private float            CURRENT_MAX_DIFF_SHF       = 150f;

    private final float      MIN_SFWF                   = -3E-4f;
    private final float      MAX_SFWF                   = 3E-4f;
    private float            CURRENT_MIN_SFWF           = -3E-4f;
    private float            CURRENT_MAX_SFWF           = 3E-4f;
    private final float      MIN_DIFF_SFWF              = -1E-4f;
    private final float      MAX_DIFF_SFWF              = 1E-4f;
    private float            CURRENT_MIN_DIFF_SFWF      = -1E-4f;
    private float            CURRENT_MAX_DIFF_SFWF      = 1E-4f;

    private final float      MIN_HMXL                   = 0f;
    private final float      MAX_HMXL                   = 150000f;
    private float            CURRENT_MIN_HMXL           = 0f;
    private float            CURRENT_MAX_HMXL           = 150000f;
    private final float      MIN_DIFF_HMXL              = -50000f;
    private final float      MAX_DIFF_HMXL              = 50000f;
    private float            CURRENT_MIN_DIFF_HMXL      = -50000f;
    private float            CURRENT_MAX_DIFF_HMXL      = 50000f;

    private final float      MIN_SALT                   = 0.00f;
    private final float      MAX_SALT                   = 0.05f;
    private float            CURRENT_MIN_SALT           = 0.03f;
    private float            CURRENT_MAX_SALT           = 0.04f;
    private final float      MIN_DIFF_SALT              = -0.025f;
    private final float      MAX_DIFF_SALT              = 0.025f;
    private float            CURRENT_MIN_DIFF_SALT      = -0.025f;
    private float            CURRENT_MAX_DIFF_SALT      = 0.025f;

    private final float      MIN_TEMP                   = -2f;
    private final float      MAX_TEMP                   = 30f;
    private float            CURRENT_MIN_TEMP           = -2f;
    private float            CURRENT_MAX_TEMP           = 30f;
    private final float      MIN_DIFF_TEMP              = -15f;
    private final float      MAX_DIFF_TEMP              = 15f;
    private float            CURRENT_MIN_DIFF_TEMP      = -15f;
    private float            CURRENT_MAX_DIFF_TEMP      = 15f;

    private final float      MIN_UVEL                   = -200f;
    private final float      MAX_UVEL                   = 200f;
    private float            CURRENT_MIN_UVEL           = -200f;
    private float            CURRENT_MAX_UVEL           = 200f;
    private final float      MIN_DIFF_UVEL              = -100f;
    private final float      MAX_DIFF_UVEL              = 100f;
    private float            CURRENT_MIN_DIFF_UVEL      = -100f;
    private float            CURRENT_MAX_DIFF_UVEL      = 100f;

    private final float      MIN_VVEL                   = -200f;
    private final float      MAX_VVEL                   = 200f;
    private float            CURRENT_MIN_VVEL           = -200f;
    private float            CURRENT_MAX_VVEL           = 200f;
    private final float      MIN_DIFF_VVEL              = -100f;
    private final float      MAX_DIFF_VVEL              = 100f;
    private float            CURRENT_MIN_DIFF_VVEL      = -100f;
    private float            CURRENT_MAX_DIFF_VVEL      = 100f;

    private final float      MIN_KE                     = 0f;
    private final float      MAX_KE                     = 10000f;
    private float            CURRENT_MIN_KE             = 0f;
    private float            CURRENT_MAX_KE             = 10000f;
    private final float      MIN_DIFF_KE                = -5000f;
    private final float      MAX_DIFF_KE                = 5000f;
    private float            CURRENT_MIN_DIFF_KE        = -5000f;
    private float            CURRENT_MAX_DIFF_KE        = 5000f;

    private final float      MIN_PD                     = 1f;
    private final float      MAX_PD                     = 1.04f;
    private float            CURRENT_MIN_PD             = 1f;
    private float            CURRENT_MAX_PD             = 1.04f;
    private final float      MIN_DIFF_PD                = -0.01f;
    private final float      MAX_DIFF_PD                = 0.01f;
    private float            CURRENT_MIN_DIFF_PD        = -0.01f;
    private float            CURRENT_MAX_DIFF_PD        = 0.01f;

    private final float      MIN_TAUX                   = -1f;
    private final float      MAX_TAUX                   = 1f;
    private float            CURRENT_MIN_TAUX           = -1f;
    private float            CURRENT_MAX_TAUX           = 1f;
    private final float      MIN_DIFF_TAUX              = -.5f;
    private final float      MAX_DIFF_TAUX              = .5f;
    private float            CURRENT_MIN_DIFF_TAUX      = -.5f;
    private float            CURRENT_MAX_DIFF_TAUX      = .5f;

    private final float      MIN_TAUY                   = -1f;
    private final float      MAX_TAUY                   = 1f;
    private float            CURRENT_MIN_TAUY           = -1f;
    private float            CURRENT_MAX_TAUY           = 1f;
    private final float      MIN_DIFF_TAUY              = -.5f;
    private final float      MAX_DIFF_TAUY              = .5f;
    private float            CURRENT_MIN_DIFF_TAUY      = -.5f;
    private float            CURRENT_MAX_DIFF_TAUY      = .5f;

    private final float      MIN_H2                     = 0f;
    private final float      MAX_H2                     = 100000f;
    private float            CURRENT_MIN_H2             = 0f;
    private float            CURRENT_MAX_H2             = 100000f;
    private final float      MIN_DIFF_H2                = -50000f;
    private final float      MAX_DIFF_H2                = 50000f;
    private float            CURRENT_MIN_DIFF_H2        = -50000f;
    private float            CURRENT_MAX_DIFF_H2        = 50000f;

    private int              DEPTH_MIN                  = 0;
    private int              DEPTH_DEF                  = 0;
    private int              DEPTH_MAX                  = 41;

    private int              WINDOW_SELECTION           = 0;

    private boolean          DYNAMIC_DIMENSIONS         = false;

    private boolean          IMAGE_STREAM_OUTPUT        = false;
    private final int        SAGE_FRAMES_PER_SECOND     = 10;
    private boolean          IMAGE_STREAM_GL_ONLY       = true;

    private float            HEIGHT_DISTORION           = 0f;
    private final float      HEIGHT_DISTORION_MIN       = 0f;
    private final float      HEIGHT_DISTORION_MAX       = .01f;

    private String           SAGE_DIRECTORY             = "/home/maarten/sage-code/sage";

    private final boolean    TOUCH_CONNECTED            = false;

    private final String[]   POSSIBLE_LAT_AXIS_NAMES    = { "t_lat", "TLAT",
            "T_LAT", "tlat", "lat_t", "latt", "LATT", "u_lat", "ULAT", "U_LAT",
            "ulat", "lat_u", "latu", "LATU", "nlat", "NLAT", "latn", "LATN",
            "n_lat", "N_LAT", "lat_n", "LAT_N"         };
    private final String[]   POSSIBLE_LON_AXIS_NAMES    = { "t_lon", "TLON",
            "T_LON", "tlon", "lon_t", "lont", "LONT", "t_long", "TLONG",
            "T_LONG", "tlong", "long_t", "longt", "LONGT", "u_lon", "ULON",
            "U_LON", "ulon", "lon_u", "lonu", "LONU", "u_long", "ULONG",
            "U_LONG", "ulong", "long_u", "longu", "LONGU", "nlon", "NLON",
            "lonn", "LONN", "n_lon", "N_LON", "lon_n", "LON_N", "nlong",
            "NLONG", "longn", "LONGN", "n_long", "N_LONG", "long_n", "LONG_N" };
    private final String[]   POSSIBLE_DEPTH_AXIS_NAMES  = { "t_depth",
            "TDEPTH", "T_DEPTH", "tdepth", "depth_t", "deptht", "DEPTHT", "ZT",
            "zt", "Z_T", "z_t", "TZ", "tz", "T_Z", "t_z", "u_depth", "UDEPTH",
            "U_DEPTH", "udepth", "depth_u", "depthu", "DEPTHU", "ZU", "zu",
            "Z_U", "z_u", "UZ", "uz", "U_Z", "u_z"     };

    private final String     LAT_SUBSTRING              = "lat";
    private final String     LON_SUBSTRING              = "lon";

    private GlobeState       globeStateLT               = new GlobeState(
                                                                GlobeState.DataMode.FIRST_DATASET,
                                                                false,
                                                                GlobeState.Variable.TEMP,
                                                                7502,
                                                                0,
                                                                "default",
                                                                CURRENT_MIN_TEMP,
                                                                CURRENT_MAX_TEMP);
    private GlobeState       globeStateRT               = new GlobeState(
                                                                GlobeState.DataMode.FIRST_DATASET,
                                                                false,
                                                                GlobeState.Variable.KE,
                                                                7502, 0,
                                                                "rainbow",
                                                                CURRENT_MIN_KE,
                                                                CURRENT_MAX_KE);
    private GlobeState       globeStateLB               = new GlobeState(
                                                                GlobeState.DataMode.FIRST_DATASET,
                                                                false,
                                                                GlobeState.Variable.SALT,
                                                                7502,
                                                                0,
                                                                "inv_diff",
                                                                CURRENT_MIN_SALT,
                                                                CURRENT_MAX_SALT);
    private GlobeState       globeStateRB               = new GlobeState(
                                                                GlobeState.DataMode.FIRST_DATASET,
                                                                false,
                                                                GlobeState.Variable.HMXL,
                                                                7502,
                                                                0,
                                                                "hotres",
                                                                CURRENT_MIN_HMXL,
                                                                CURRENT_MAX_HMXL);

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

    public float getVarMax(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = MAX_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = MAX_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = MAX_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = MAX_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = MAX_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = MAX_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = MAX_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = MAX_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = MAX_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = MAX_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = MAX_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = MAX_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = MAX_H2;
        }
        return result;
    }

    public float getVarDiffMax(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = MAX_DIFF_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = MAX_DIFF_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = MAX_DIFF_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = MAX_DIFF_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = MAX_DIFF_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = MAX_DIFF_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = MAX_DIFF_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = MAX_DIFF_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = MAX_DIFF_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = MAX_DIFF_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = MAX_DIFF_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = MAX_DIFF_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = MAX_DIFF_H2;
        }
        return result;
    }

    public float getVarMin(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = MIN_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = MIN_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = MIN_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = MIN_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = MIN_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = MIN_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = MIN_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = MIN_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = MIN_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = MIN_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = MIN_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = MIN_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = MIN_H2;
        }
        return result;
    }

    public float getVarDiffMin(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = MIN_DIFF_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = MIN_DIFF_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = MIN_DIFF_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = MIN_DIFF_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = MIN_DIFF_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = MIN_DIFF_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = MIN_DIFF_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = MIN_DIFF_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = MIN_DIFF_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = MIN_DIFF_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = MIN_DIFF_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = MIN_DIFF_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = MIN_DIFF_H2;
        }
        return result;
    }

    public void setCurrentVarMax(Variable var, float value) {
        if (var == Variable.SSH) {
            CURRENT_MAX_SSH = value;
        } else if (var == Variable.SHF) {
            CURRENT_MAX_SHF = value;
        } else if (var == Variable.SFWF) {
            CURRENT_MAX_SFWF = value;
        } else if (var == Variable.HMXL) {
            CURRENT_MAX_HMXL = value;
        } else if (var == Variable.SALT) {
            CURRENT_MAX_SALT = value;
        } else if (var == Variable.TEMP) {
            CURRENT_MAX_TEMP = value;
        } else if (var == Variable.UVEL) {
            CURRENT_MAX_UVEL = value;
        } else if (var == Variable.VVEL) {
            CURRENT_MAX_VVEL = value;
        } else if (var == Variable.KE) {
            CURRENT_MAX_KE = value;
        } else if (var == Variable.PD) {
            CURRENT_MAX_PD = value;
        } else if (var == Variable.TAUX) {
            CURRENT_MAX_TAUX = value;
        } else if (var == Variable.TAUY) {
            CURRENT_MAX_TAUY = value;
        } else if (var == Variable.H2) {
            CURRENT_MAX_H2 = value;
        }
    }

    public void setCurrentVarDiffMax(Variable var, float value) {
        if (var == Variable.SSH) {
            CURRENT_MAX_DIFF_SSH = value;
        } else if (var == Variable.SHF) {
            CURRENT_MAX_DIFF_SHF = value;
        } else if (var == Variable.SFWF) {
            CURRENT_MAX_DIFF_SFWF = value;
        } else if (var == Variable.HMXL) {
            CURRENT_MAX_DIFF_HMXL = value;
        } else if (var == Variable.SALT) {
            CURRENT_MAX_DIFF_SALT = value;
        } else if (var == Variable.TEMP) {
            CURRENT_MAX_DIFF_TEMP = value;
        } else if (var == Variable.UVEL) {
            CURRENT_MAX_DIFF_UVEL = value;
        } else if (var == Variable.VVEL) {
            CURRENT_MAX_DIFF_VVEL = value;
        } else if (var == Variable.KE) {
            CURRENT_MAX_DIFF_KE = value;
        } else if (var == Variable.PD) {
            CURRENT_MAX_DIFF_PD = value;
        } else if (var == Variable.TAUX) {
            CURRENT_MAX_DIFF_TAUX = value;
        } else if (var == Variable.TAUY) {
            CURRENT_MAX_DIFF_TAUY = value;
        } else if (var == Variable.H2) {
            CURRENT_MAX_DIFF_H2 = value;
        }
    }

    public void setCurrentVarMin(Variable var, float value) {
        if (var == Variable.SSH) {
            CURRENT_MIN_SSH = value;
        } else if (var == Variable.SHF) {
            CURRENT_MIN_SHF = value;
        } else if (var == Variable.SFWF) {
            CURRENT_MIN_SFWF = value;
        } else if (var == Variable.HMXL) {
            CURRENT_MIN_HMXL = value;
        } else if (var == Variable.SALT) {
            CURRENT_MIN_SALT = value;
        } else if (var == Variable.TEMP) {
            CURRENT_MIN_TEMP = value;
        } else if (var == Variable.UVEL) {
            CURRENT_MIN_UVEL = value;
        } else if (var == Variable.VVEL) {
            CURRENT_MIN_VVEL = value;
        } else if (var == Variable.KE) {
            CURRENT_MIN_KE = value;
        } else if (var == Variable.PD) {
            CURRENT_MIN_PD = value;
        } else if (var == Variable.TAUX) {
            CURRENT_MIN_TAUX = value;
        } else if (var == Variable.TAUY) {
            CURRENT_MIN_TAUY = value;
        } else if (var == Variable.H2) {
            CURRENT_MIN_H2 = value;
        }
    }

    public void setCurrentVarDiffMin(Variable var, float value) {
        if (var == Variable.SSH) {
            CURRENT_MIN_DIFF_SSH = value;
        } else if (var == Variable.SHF) {
            CURRENT_MIN_DIFF_SHF = value;
        } else if (var == Variable.SFWF) {
            CURRENT_MIN_DIFF_SFWF = value;
        } else if (var == Variable.HMXL) {
            CURRENT_MIN_DIFF_HMXL = value;
        } else if (var == Variable.SALT) {
            CURRENT_MIN_DIFF_SALT = value;
        } else if (var == Variable.TEMP) {
            CURRENT_MIN_DIFF_TEMP = value;
        } else if (var == Variable.UVEL) {
            CURRENT_MIN_DIFF_UVEL = value;
        } else if (var == Variable.VVEL) {
            CURRENT_MIN_DIFF_VVEL = value;
        } else if (var == Variable.KE) {
            CURRENT_MIN_DIFF_KE = value;
        } else if (var == Variable.PD) {
            CURRENT_MIN_DIFF_PD = value;
        } else if (var == Variable.TAUX) {
            CURRENT_MIN_DIFF_TAUX = value;
        } else if (var == Variable.TAUY) {
            CURRENT_MIN_DIFF_TAUY = value;
        } else if (var == Variable.H2) {
            CURRENT_MIN_DIFF_H2 = value;
        }
    }

    public float getCurrentVarMax(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = CURRENT_MAX_SSH;
        } else if (var == Variable.SHF) {
            result = CURRENT_MAX_SHF;
        } else if (var == Variable.SFWF) {
            result = CURRENT_MAX_SFWF;
        } else if (var == Variable.HMXL) {
            result = CURRENT_MAX_HMXL;
        } else if (var == Variable.SALT) {
            result = CURRENT_MAX_SALT;
        } else if (var == Variable.TEMP) {
            result = CURRENT_MAX_TEMP;
        } else if (var == Variable.UVEL) {
            result = CURRENT_MAX_UVEL;
        } else if (var == Variable.VVEL) {
            result = CURRENT_MAX_VVEL;
        } else if (var == Variable.KE) {
            result = CURRENT_MAX_KE;
        } else if (var == Variable.PD) {
            result = CURRENT_MAX_PD;
        } else if (var == Variable.TAUX) {
            result = CURRENT_MAX_TAUX;
        } else if (var == Variable.TAUY) {
            result = CURRENT_MAX_TAUY;
        } else if (var == Variable.H2) {
            result = CURRENT_MAX_H2;
        }
        return result;
    }

    public float getCurrentVarDiffMax(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = CURRENT_MAX_DIFF_SSH;
        } else if (var == Variable.SHF) {
            result = CURRENT_MAX_DIFF_SHF;
        } else if (var == Variable.SFWF) {
            result = CURRENT_MAX_DIFF_SFWF;
        } else if (var == Variable.HMXL) {
            result = CURRENT_MAX_DIFF_HMXL;
        } else if (var == Variable.SALT) {
            result = CURRENT_MAX_DIFF_SALT;
        } else if (var == Variable.TEMP) {
            result = CURRENT_MAX_DIFF_TEMP;
        } else if (var == Variable.UVEL) {
            result = CURRENT_MAX_DIFF_UVEL;
        } else if (var == Variable.VVEL) {
            result = CURRENT_MAX_DIFF_VVEL;
        } else if (var == Variable.KE) {
            result = CURRENT_MAX_DIFF_KE;
        } else if (var == Variable.PD) {
            result = CURRENT_MAX_DIFF_PD;
        } else if (var == Variable.TAUX) {
            result = CURRENT_MAX_DIFF_TAUX;
        } else if (var == Variable.TAUY) {
            result = CURRENT_MAX_DIFF_TAUY;
        } else if (var == Variable.H2) {
            result = CURRENT_MAX_DIFF_H2;
        }
        return result;
    }

    public float getCurrentVarMin(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = CURRENT_MIN_SSH;
        } else if (var == Variable.SHF) {
            result = CURRENT_MIN_SHF;
        } else if (var == Variable.SFWF) {
            result = CURRENT_MIN_SFWF;
        } else if (var == Variable.HMXL) {
            result = CURRENT_MIN_HMXL;
        } else if (var == Variable.SALT) {
            result = CURRENT_MIN_SALT;
        } else if (var == Variable.TEMP) {
            result = CURRENT_MIN_TEMP;
        } else if (var == Variable.UVEL) {
            result = CURRENT_MIN_UVEL;
        } else if (var == Variable.VVEL) {
            result = CURRENT_MIN_VVEL;
        } else if (var == Variable.KE) {
            result = CURRENT_MIN_KE;
        } else if (var == Variable.PD) {
            result = CURRENT_MIN_PD;
        } else if (var == Variable.TAUX) {
            result = CURRENT_MIN_TAUX;
        } else if (var == Variable.TAUY) {
            result = CURRENT_MIN_TAUY;
        } else if (var == Variable.H2) {
            result = CURRENT_MIN_H2;
        }
        return result;
    }

    public float getCurrentVarDiffMin(Variable var) {
        float result = 0f;
        if (var == Variable.SSH) {
            result = CURRENT_MIN_DIFF_SSH;
        } else if (var == Variable.SHF) {
            result = CURRENT_MIN_DIFF_SHF;
        } else if (var == Variable.SFWF) {
            result = CURRENT_MIN_DIFF_SFWF;
        } else if (var == Variable.HMXL) {
            result = CURRENT_MIN_DIFF_HMXL;
        } else if (var == Variable.SALT) {
            result = CURRENT_MIN_DIFF_SALT;
        } else if (var == Variable.TEMP) {
            result = CURRENT_MIN_DIFF_TEMP;
        } else if (var == Variable.UVEL) {
            result = CURRENT_MIN_DIFF_UVEL;
        } else if (var == Variable.VVEL) {
            result = CURRENT_MIN_DIFF_VVEL;
        } else if (var == Variable.KE) {
            result = CURRENT_MIN_DIFF_KE;
        } else if (var == Variable.PD) {
            result = CURRENT_MIN_DIFF_PD;
        } else if (var == Variable.TAUX) {
            result = CURRENT_MIN_DIFF_TAUX;
        } else if (var == Variable.TAUY) {
            result = CURRENT_MIN_DIFF_TAUY;
        } else if (var == Variable.H2) {
            result = CURRENT_MIN_DIFF_H2;
        }
        return result;
    }

    public float getCurrentVarMax(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_H2;
        }
        return result;
    }

    public float getCurrentVarDiffMax(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = CURRENT_MAX_DIFF_H2;
        }
        return result;
    }

    public float getCurrentVarMin(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_H2;
        }
        return result;
    }

    public float getCurrentVarDiffMin(String var) {
        float result = 0f;
        if (Variable.SSH.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_SSH;
        } else if (Variable.SHF.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_SHF;
        } else if (Variable.SFWF.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_SFWF;
        } else if (Variable.HMXL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_HMXL;
        } else if (Variable.SALT.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_SALT;
        } else if (Variable.TEMP.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_TEMP;
        } else if (Variable.UVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_UVEL;
        } else if (Variable.VVEL.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_VVEL;
        } else if (Variable.KE.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_KE;
        } else if (Variable.PD.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_PD;
        } else if (Variable.TAUX.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_TAUX;
        } else if (Variable.TAUY.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_TAUY;
        } else if (Variable.H2.toString().compareTo(var) == 0) {
            result = CURRENT_MIN_DIFF_H2;
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
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.getFrameNumber() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(), value,
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
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
                    state.getFrameNumber(), value, state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.getDepth() != value) {
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), value, state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
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
                state.getFrameNumber(), state.getDepth(), state.getColorMap(),
                state.getLowerBound(), state.getUpperBound());
        globeStateLT = result;
    }

    public synchronized void setRTDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap(),
                state.getLowerBound(), state.getUpperBound());
        globeStateRT = result;
    }

    public synchronized void setLBDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap(),
                state.getLowerBound(), state.getUpperBound());
        globeStateLB = result;
    }

    public synchronized void setRBDataMode(GlobeState.DataMode dataMode) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(dataMode,
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), state.getColorMap(),
                state.getLowerBound(), state.getUpperBound());
        globeStateRB = result;
    }

    public synchronized void setLTVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateLT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap(), state.getLowerBound(),
                state.getUpperBound());
        globeStateLT = result;
    }

    public synchronized void setRTVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap(), state.getLowerBound(),
                state.getUpperBound());
        globeStateRT = result;
    }

    public synchronized void setLBVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap(), state.getLowerBound(),
                state.getUpperBound());
        globeStateLB = result;
    }

    public synchronized void setRBVariable(GlobeState.Variable variable) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), variable, state.getFrameNumber(),
                state.getDepth(), state.getColorMap(), state.getLowerBound(),
                state.getUpperBound());
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
                state.getFrameNumber(), state.getDepth(), selectedColorMap,
                state.getLowerBound(), state.getUpperBound());
        globeStateLT = result;
    }

    public synchronized void setRTColorMap(String selectedColorMap) {
        GlobeState state = globeStateRT;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap,
                state.getLowerBound(), state.getUpperBound());
        globeStateRT = result;
    }

    public synchronized void setLBColorMap(String selectedColorMap) {
        GlobeState state = globeStateLB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap,
                state.getLowerBound(), state.getUpperBound());
        globeStateLB = result;
    }

    public synchronized void setRBColorMap(String selectedColorMap) {
        GlobeState state = globeStateRB;
        GlobeState result = new GlobeState(state.getDataMode(),
                state.isDynamicDimensions(), state.getVariable(),
                state.getFrameNumber(), state.getDepth(), selectedColorMap,
                state.getLowerBound(), state.getUpperBound());
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
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLT = result;
        }

        state = globeStateRT;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateRT = result;
        }

        state = globeStateLB;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
            globeStateLB = result;
        }

        state = globeStateRB;
        if (state.isDynamicDimensions() != DYNAMIC_DIMENSIONS) {
            result = new GlobeState(state.getDataMode(), DYNAMIC_DIMENSIONS,
                    state.getVariable(), state.getFrameNumber(),
                    state.getDepth(), state.getColorMap(),
                    state.getLowerBound(), state.getUpperBound());
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

    public void setVariableRange(int whichglobe, String varName, int minValue,
            int maxValue) {
        float minFloatValue = 0f, maxFloatValue = 0f;
        if (varName.compareTo("SSH") == 0) {
            float diff = (MAX_SSH - MIN_SSH);
            CURRENT_MIN_SSH = (minValue / 100f) * diff + MIN_SSH;
            CURRENT_MAX_SSH = (maxValue / 100f) * diff + MIN_SSH;
            minFloatValue = CURRENT_MIN_SSH;
            maxFloatValue = CURRENT_MAX_SSH;
        } else if (varName.compareTo("SHF") == 0) {
            float diff = (MAX_SHF - MIN_SHF);
            CURRENT_MIN_SHF = (minValue / 100f) * diff + MIN_SHF;
            CURRENT_MAX_SHF = (maxValue / 100f) * diff + MIN_SHF;
            minFloatValue = CURRENT_MIN_SHF;
            maxFloatValue = CURRENT_MAX_SHF;
        } else if (varName.compareTo("SFWF") == 0) {
            float diff = (MAX_SFWF - MIN_SFWF);
            CURRENT_MIN_SFWF = (minValue / 100f) * diff + MIN_SFWF;
            CURRENT_MAX_SFWF = (maxValue / 100f) * diff + MIN_SFWF;
            minFloatValue = CURRENT_MIN_SFWF;
            maxFloatValue = CURRENT_MAX_SFWF;
        } else if (varName.compareTo("HMXL") == 0) {
            float diff = (MAX_HMXL - MIN_HMXL);
            CURRENT_MIN_HMXL = (minValue / 100f) * diff + MIN_HMXL;
            CURRENT_MAX_HMXL = (maxValue / 100f) * diff + MIN_HMXL;
            minFloatValue = CURRENT_MIN_HMXL;
            maxFloatValue = CURRENT_MAX_HMXL;
        } else if (varName.compareTo("SALT") == 0) {
            float diff = (MAX_SALT - MIN_SALT);
            CURRENT_MIN_SALT = (minValue / 100f) * diff + MIN_SALT;
            CURRENT_MAX_SALT = (maxValue / 100f) * diff + MIN_SALT;
            minFloatValue = CURRENT_MIN_SALT;
            maxFloatValue = CURRENT_MAX_SALT;
        } else if (varName.compareTo("TEMP") == 0) {
            float diff = (MAX_TEMP - MIN_TEMP);
            CURRENT_MIN_TEMP = (minValue / 100f) * diff + MIN_TEMP;
            CURRENT_MAX_TEMP = (maxValue / 100f) * diff + MIN_TEMP;
            minFloatValue = CURRENT_MIN_TEMP;
            maxFloatValue = CURRENT_MAX_TEMP;
        } else if (varName.compareTo("UVEL") == 0) {
            float diff = (MAX_UVEL - MIN_UVEL);
            CURRENT_MIN_UVEL = (minValue / 100f) * diff + MIN_UVEL;
            CURRENT_MAX_UVEL = (maxValue / 100f) * diff + MIN_UVEL;
            minFloatValue = CURRENT_MIN_UVEL;
            maxFloatValue = CURRENT_MAX_UVEL;
        } else if (varName.compareTo("VVEL") == 0) {
            float diff = (MAX_VVEL - MIN_VVEL);
            CURRENT_MIN_VVEL = (minValue / 100f) * diff + MIN_VVEL;
            CURRENT_MAX_VVEL = (maxValue / 100f) * diff + MIN_VVEL;
            minFloatValue = CURRENT_MIN_VVEL;
            maxFloatValue = CURRENT_MAX_VVEL;
        } else if (varName.compareTo("KE") == 0) {
            float diff = (MAX_KE - MIN_KE);
            CURRENT_MIN_KE = (minValue / 100f) * diff + MIN_KE;
            CURRENT_MAX_KE = (maxValue / 100f) * diff + MIN_KE;
            minFloatValue = CURRENT_MIN_KE;
            maxFloatValue = CURRENT_MAX_KE;
        } else if (varName.compareTo("PD") == 0) {
            float diff = (MAX_PD - MIN_PD);
            CURRENT_MIN_PD = (minValue / 100f) * diff + MIN_PD;
            CURRENT_MAX_PD = (maxValue / 100f) * diff + MIN_PD;
            minFloatValue = CURRENT_MIN_PD;
            maxFloatValue = CURRENT_MAX_PD;
        } else if (varName.compareTo("TAUX") == 0) {
            float diff = (MAX_TAUX - MIN_TAUX);
            CURRENT_MIN_TAUX = (minValue / 100f) * diff + MIN_TAUX;
            CURRENT_MAX_TAUX = (maxValue / 100f) * diff + MIN_TAUX;
            minFloatValue = CURRENT_MIN_SSH;
            maxFloatValue = CURRENT_MAX_SSH;
        } else if (varName.compareTo("TAUY") == 0) {
            float diff = (MAX_TAUY - MIN_TAUY);
            CURRENT_MIN_TAUY = (minValue / 100f) * diff + MIN_TAUY;
            CURRENT_MAX_TAUY = (maxValue / 100f) * diff + MIN_TAUY;
            minFloatValue = CURRENT_MIN_TAUY;
            maxFloatValue = CURRENT_MAX_TAUY;
        } else if (varName.compareTo("H2") == 0) {
            float diff = (MAX_H2 - MIN_H2);
            CURRENT_MIN_H2 = (minValue / 100f) * diff + MIN_H2;
            CURRENT_MAX_H2 = (maxValue / 100f) * diff + MIN_H2;
            minFloatValue = CURRENT_MIN_H2;
            maxFloatValue = CURRENT_MAX_H2;
        }

        GlobeState result;
        GlobeState state;

        if (whichglobe == 0) {
            state = globeStateLT;
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), state.getDepth(),
                    state.getColorMap(), minFloatValue, maxFloatValue);
            globeStateLT = result;
        } else if (whichglobe == 1) {
            state = globeStateRT;
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), state.getDepth(),
                    state.getColorMap(), minFloatValue, maxFloatValue);
            globeStateRT = result;
        } else if (whichglobe == 2) {
            state = globeStateLB;
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), state.getDepth(),
                    state.getColorMap(), minFloatValue, maxFloatValue);
            globeStateLB = result;
        } else if (whichglobe == 3) {
            state = globeStateRB;
            result = new GlobeState(state.getDataMode(),
                    state.isDynamicDimensions(), state.getVariable(),
                    state.getFrameNumber(), state.getDepth(),
                    state.getColorMap(), minFloatValue, maxFloatValue);
            globeStateRB = result;
        }
    }

    public int getImageWidth() {
        return IMAGE_WIDTH;
    }

    public int getImageHeight() {
        return IMAGE_HEIGHT;
    }

    public int getRangeSliderLowerValue(int whichglobe) {
        GlobeState state = null;

        if (whichglobe == 0) {
            state = globeStateLT;
        } else if (whichglobe == 1) {
            state = globeStateRT;
        } else if (whichglobe == 2) {
            state = globeStateLB;
        } else if (whichglobe == 3) {
            state = globeStateRB;
        }

        float min = getVarMin(state.getVariable());
        float max = getVarMax(state.getVariable());
        float currentMin = getCurrentVarMin(state.getVariable());

        float diff = max - min;
        float result = (currentMin - min) / diff;

        return (int) (result * 100) - 1;
    }

    public int getRangeSliderUpperValue(int whichglobe) {
        GlobeState state = null;

        if (whichglobe == 0) {
            state = globeStateLT;
        } else if (whichglobe == 1) {
            state = globeStateRT;
        } else if (whichglobe == 2) {
            state = globeStateLB;
        } else if (whichglobe == 3) {
            state = globeStateRB;
        }

        float min = getVarMin(state.getVariable());
        float max = getVarMax(state.getVariable());
        float currentMax = getCurrentVarMax(state.getVariable());

        float diff = max - min;
        float result = (currentMax - min) / diff;

        return (int) (result * 100) - 1;
    }
}
