package imau.visualization;

import imau.visualization.adaptor.GlobeState;
import imau.visualization.adaptor.GlobeState.Variable;
import imau.visualization.adaptor.ImageMaker;
import openglCommon.util.Settings;
import openglCommon.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImauSettings extends Settings {
    private final static Logger logger = LoggerFactory
                                               .getLogger(ImauSettings.class);

    private static class SingletonHolder {
        public final static ImauSettings instance = new ImauSettings();
    }

    public static enum GlobeMode {
        FIRST_DATASET, SECOND_DATASET, DIFF
    };

    private static GlobeState    globeStateLT              = new GlobeState(
                                                                   GlobeState.DataMode.FIRST_DATASET,
                                                                   false,
                                                                   GlobeState.Variable.SALT,
                                                                   75, 0,
                                                                   "default");
    private static GlobeState    globeStateRT              = new GlobeState(
                                                                   GlobeState.DataMode.FIRST_DATASET,
                                                                   false,
                                                                   GlobeState.Variable.TEMP,
                                                                   75, 0,
                                                                   "default");
    private static GlobeState    globeStateLB              = new GlobeState(
                                                                   GlobeState.DataMode.FIRST_DATASET,
                                                                   false,
                                                                   GlobeState.Variable.SSH,
                                                                   75, 0,
                                                                   "default");
    private static GlobeState    globeStateRB              = new GlobeState(
                                                                   GlobeState.DataMode.FIRST_DATASET,
                                                                   false,
                                                                   GlobeState.Variable.SFWF,
                                                                   75, 0,
                                                                   "default");

    private static long          WAITTIME_FOR_RETRY        = 10000;
    private static long          WAITTIME_FOR_MOVIE        = 500;
    private static float         EPSILON                   = 1.0E-7f;

    private static int           FILE_EXTENSION_LENGTH     = 2;
    private static int           FILE_NUMBER_LENGTH        = 4;

    private static String[]      ACCEPTABLE_POSTFIXES      = { ".nc" };

    private static String        CURRENT_POSTFIX           = "nc";

    private static int           PREPROCESSING_AMOUNT      = 2;

    private static float         MIN_SSH                   = -200f;
    private static float         MAX_SSH                   = 200f;
    private static float         MIN_DIFF_SSH              = -125f;
    private static float         MAX_DIFF_SSH              = 125f;

    private static float         MIN_SHF                   = -400f;
    private static float         MAX_SHF                   = 250f;
    private static float         MIN_DIFF_SHF              = -400f;
    private static float         MAX_DIFF_SHF              = 250f;

    private static float         MIN_SFWF                  = -3E-4f;
    private static float         MAX_SFWF                  = 3E-4f;
    private static float         MIN_DIFF_SFWF             = -5E-5f;
    private static float         MAX_DIFF_SFWF             = 3.5E-4f;

    private static float         MIN_HMXL                  = 750f;
    private static float         MAX_HMXL                  = 100000f;
    private static float         MIN_DIFF_HMXL             = 750f;
    private static float         MAX_DIFF_HMXL             = 100000f;

    private static float         MIN_SALT                  = 0.00f;
    private static float         MAX_SALT                  = 0.05f;
    private static float         MIN_DIFF_SALT             = 0.00f;
    private static float         MAX_DIFF_SALT             = 0.05f;

    private static float         MIN_TEMP                  = -7.5f;
    private static float         MAX_TEMP                  = 35f;
    private static float         MIN_DIFF_TEMP             = -7.5f;
    private static float         MAX_DIFF_TEMP             = 35f;

    private static float         MIN_UVEL                  = -200f;
    private static float         MAX_UVEL                  = 200f;
    private static float         MIN_DIFF_UVEL             = -200f;
    private static float         MAX_DIFF_UVEL             = 200f;

    private static float         MIN_VVEL                  = -200f;
    private static float         MAX_VVEL                  = 200f;
    private static float         MIN_DIFF_VVEL             = -200f;
    private static float         MAX_DIFF_VVEL             = 200f;

    private static float         MIN_KE                    = 0f;
    private static float         MAX_KE                    = 17000f;
    private static float         MIN_DIFF_KE               = 0f;
    private static float         MAX_DIFF_KE               = 17000f;

    private static float         MIN_PD                    = 1f;
    private static float         MAX_PD                    = 1.04f;
    private static float         MIN_DIFF_PD               = 1f;
    private static float         MAX_DIFF_PD               = 1.04f;

    private static float         MIN_TAUX                  = -1f;
    private static float         MAX_TAUX                  = 1f;
    private static float         MIN_DIFF_TAUX             = -1f;
    private static float         MAX_DIFF_TAUX             = 1f;

    private static float         MIN_TAUY                  = -1f;
    private static float         MAX_TAUY                  = 1f;
    private static float         MIN_DIFF_TAUY             = -1f;
    private static float         MAX_DIFF_TAUY             = 1f;

    private static float         MIN_H2                    = 0f;
    private static float         MAX_H2                    = 100000f;
    private static float         MIN_DIFF_H2               = 0f;
    private static float         MAX_DIFF_H2               = 100000f;

    private static int           DEPTH_MIN                 = 0;
    private static int           DEPTH_DEF                 = 0;
    private static int           DEPTH_MAX                 = 41;

    private static int           WINDOW_SELECTION          = 0;

    private static boolean       DYNAMIC_DIMENSIONS        = false;

    private static boolean       IMAGE_STREAM_OUTPUT       = true;
    private static boolean       IMAGE_STREAM_GL_ONLY      = true;

    private static float         HEIGHT_DISTORION          = 0f;
    private static float         HEIGHT_DISTORION_MIN      = 0f;
    private static float         HEIGHT_DISTORION_MAX      = .01f;

    private static String        SAGE_DIRECTORY            = "/home/maarten/sage-code/sage3.0";

    private static final boolean TOUCH_CONNECTED           = false;

    private static String[]      POSSIBLE_LAT_AXIS_NAMES   = { "t_lat", "TLAT",
            "T_LAT", "tlat", "lat_t", "latt", "LATT", "u_lat", "ULAT", "U_LAT",
            "ulat", "lat_u", "latu", "LATU", "nlat", "NLAT", "latn", "LATN",
            "n_lat", "N_LAT", "lat_n", "LAT_N"            };
    private static String[]      POSSIBLE_LON_AXIS_NAMES   = { "t_lon", "TLON",
            "T_LON", "tlon", "lon_t", "lont", "LONT", "t_long", "TLONG",
            "T_LONG", "tlong", "long_t", "longt", "LONGT", "u_lon", "ULON",
            "U_LON", "ulon", "lon_u", "lonu", "LONU", "u_long", "ULONG",
            "U_LONG", "ulong", "long_u", "longu", "LONGU", "nlon", "NLON",
            "lonn", "LONN", "n_lon", "N_LON", "lon_n", "LON_N", "nlong",
            "NLONG", "longn", "LONGN", "n_long", "N_LONG", "long_n", "LONG_N" };
    private static String[]      POSSIBLE_DEPTH_AXIS_NAMES = { "t_depth",
            "TDEPTH", "T_DEPTH", "tdepth", "depth_t", "deptht", "DEPTHT", "ZT",
            "zt", "Z_T", "z_t", "TZ", "tz", "T_Z", "t_z", "u_depth", "UDEPTH",
            "U_DEPTH", "udepth", "depth_u", "depthu", "DEPTHU", "ZU", "zu",
            "Z_U", "z_u", "UZ", "uz", "U_Z", "u_z"        };

    private static String        LAT_SUBSTRING             = "lat";
    private static String        LON_SUBSTRING             = "lon";

    public static ImauSettings getInstance() {
        return SingletonHolder.instance;
    }

    private ImauSettings() {
        super();

        try {
            final TypedProperties props = new TypedProperties();
            props.loadFromFile("settings.properties");

            ImauSettings.WAITTIME_FOR_RETRY = props
                    .getLongProperty("WAITTIME_FOR_RETRY");
            ImauSettings.WAITTIME_FOR_MOVIE = props
                    .getLongProperty("WAITTIME_FOR_MOVIE");
            ImauSettings.EPSILON = props.getFloatProperty("EPSILON");

            ImauSettings.FILE_EXTENSION_LENGTH = props
                    .getIntProperty("FILE_EXTENSION_LENGTH");
            ImauSettings.FILE_NUMBER_LENGTH = props
                    .getIntProperty("FILE_NUMBER_LENGTH");

            ImauSettings.PREPROCESSING_AMOUNT = props
                    .getIntProperty("PREPROCESSING_AMOUNT");

            ImauSettings.MAX_SSH = props.getFloatProperty("MAX_SSH");
            ImauSettings.MIN_SSH = props.getFloatProperty("MIN_SSH");
            ImauSettings.MAX_DIFF_SSH = props.getFloatProperty("MAX_DIFF_SSH");
            ImauSettings.MIN_DIFF_SSH = props.getFloatProperty("MIN_DIFF_SSH");

            ImauSettings.MAX_SHF = props.getFloatProperty("MAX_SHF");
            ImauSettings.MIN_SHF = props.getFloatProperty("MIN_SHF");
            ImauSettings.MAX_DIFF_SHF = props.getFloatProperty("MAX_DIFF_SHF");
            ImauSettings.MIN_DIFF_SHF = props.getFloatProperty("MIN_DIFF_SHF");

            ImauSettings.MAX_SFWF = props.getFloatProperty("MAX_SFWF");
            ImauSettings.MIN_SFWF = props.getFloatProperty("MIN_SFWF");
            ImauSettings.MAX_DIFF_SFWF = props
                    .getFloatProperty("MAX_DIFF_SFWF");
            ImauSettings.MIN_DIFF_SFWF = props
                    .getFloatProperty("MIN_DIFF_SFWF");

            ImauSettings.MAX_HMXL = props.getFloatProperty("MAX_HMXL");
            ImauSettings.MIN_HMXL = props.getFloatProperty("MIN_HMXL");
            ImauSettings.MAX_DIFF_HMXL = props
                    .getFloatProperty("MAX_DIFF_HMXL");
            ImauSettings.MIN_DIFF_HMXL = props
                    .getFloatProperty("MIN_DIFF_HMXL");

            ImauSettings.MAX_SALT = props.getFloatProperty("MAX_SALT");
            ImauSettings.MIN_SALT = props.getFloatProperty("MIN_SALT");
            ImauSettings.MAX_DIFF_SALT = props
                    .getFloatProperty("MAX_DIFF_SALT");
            ImauSettings.MIN_DIFF_SALT = props
                    .getFloatProperty("MIN_DIFF_SALT");

            ImauSettings.MAX_TEMP = props.getFloatProperty("MAX_TEMP");
            ImauSettings.MIN_TEMP = props.getFloatProperty("MIN_TEMP");
            ImauSettings.MAX_DIFF_TEMP = props
                    .getFloatProperty("MAX_DIFF_TEMP");
            ImauSettings.MIN_DIFF_TEMP = props
                    .getFloatProperty("MIN_DIFF_TEMP");

            ImauSettings.DEPTH_MIN = props.getIntProperty("DEPTH_MIN");
            ImauSettings.DEPTH_DEF = props.getIntProperty("DEPTH_DEF");
            ImauSettings.DEPTH_MAX = props.getIntProperty("DEPTH_MAX");
        } catch (NumberFormatException e) {
            logger.warn(e.getMessage());
        }
    }

    public void setWaittimeBeforeRetry(long value) {
        ImauSettings.WAITTIME_FOR_RETRY = value;
    }

    public void setWaittimeMovie(long value) {
        ImauSettings.WAITTIME_FOR_MOVIE = value;
    }

    public void setEpsilon(float value) {
        ImauSettings.EPSILON = value;
    }

    public void setFileExtensionLength(int value) {
        ImauSettings.FILE_EXTENSION_LENGTH = value;
    }

    public void setFileNumberLength(int value) {
        ImauSettings.FILE_NUMBER_LENGTH = value;
    }

    public void setCurrentExtension(String value) {
        ImauSettings.CURRENT_POSTFIX = value;
    }

    public long getWaittimeBeforeRetry() {
        return ImauSettings.WAITTIME_FOR_RETRY;
    }

    public long getWaittimeMovie() {
        return ImauSettings.WAITTIME_FOR_MOVIE;
    }

    public float getEpsilon() {
        return ImauSettings.EPSILON;
    }

    public int getFileExtensionLength() {
        return ImauSettings.FILE_EXTENSION_LENGTH;
    }

    public int getFileNumberLength() {
        return ImauSettings.FILE_NUMBER_LENGTH;
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

    public String verbalizeMax(GlobeState state) {
        Variable var = state.getVariable();
        String result = "";

        if (state.isDynamicDimensions()) {
            result = Float.toString(ImageMaker.getDimensions(state).max);
        } else {
            result = Float.toString(getVarMax(var));
        }

        return result;
    }

    public String verbalizeMin(GlobeState state) {
        Variable var = state.getVariable();
        String result = "";

        if (state.isDynamicDimensions()) {
            result = Float.toString(ImageMaker.getDimensions(state).min);
        } else {
            result = Float.toString(getVarMin(var));
        }

        return result;
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
        ImauSettings.WINDOW_SELECTION = i;
    }

    public int getWindowSelection() {
        return ImauSettings.WINDOW_SELECTION;
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

    public void setIMAGE_STREAM_OUTPUT(boolean iMAGE_STREAM_OUTPUT) {
        IMAGE_STREAM_OUTPUT = iMAGE_STREAM_OUTPUT;
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
}
