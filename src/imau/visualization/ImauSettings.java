package imau.visualization;

import imau.visualization.adaptor.BandCombination;
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

    public static enum varNames {
        SSH, SHF, SFWF, HMXL, SALT, TEMP
    };

    private static BandCombination comboLT               = new BandCombination(
                                                                 0,
                                                                 varNames.SALT,
                                                                 "default");
    private static BandCombination comboRT               = new BandCombination(
                                                                 0,
                                                                 varNames.TEMP,
                                                                 "default");
    private static BandCombination comboLB               = new BandCombination(
                                                                 0,
                                                                 varNames.SSH,
                                                                 "default");
    private static BandCombination comboRB               = new BandCombination(
                                                                 0,
                                                                 varNames.SFWF,
                                                                 "default");

    private static int             MAX_GLOBES_ON_SCREEN  = 4;

    private static long            WAITTIME_FOR_RETRY    = 10000;
    private static long            WAITTIME_FOR_MOVIE    = 100;
    private static float           EPSILON               = 1.0E-7f;

    private static int             FILE_EXTENSION_LENGTH = 2;
    private static int             FILE_NUMBER_LENGTH    = 4;

    private static String[]        ACCEPTABLE_POSTFIXES  = { ".nc" };

    private static String          CURRENT_POSTFIX       = ".nc";

    private static int             PREPROCESSING_AMOUNT  = 5;

    private static float           MIN_SSH               = -200f;
    private static float           MAX_SSH               = 200f;
    private static float           MIN_SHF               = -400f;
    private static float           MAX_SHF               = 250f;
    private static float           MIN_SFWF              = -3E-4f;
    private static float           MAX_SFWF              = 3E-4f;
    private static float           MIN_HMXL              = 750f;
    private static float           MAX_HMXL              = 70000f;

    private static float           MIN_SALT              = 0.00f;
    private static float           MAX_SALT              = 0.05f;
    private static float           MIN_TEMP              = -7.5f;
    private static float           MAX_TEMP              = 35f;

    private static int             DEPTH_MIN             = 0;
    private static int             DEPTH_DEF             = 0;
    private static int             DEPTH_MAX             = 41;

    private static int             WINDOW_SELECTION      = 0;

    private static boolean         DYNAMIC_DIMENSIONS    = true;

    public static ImauSettings getInstance() {
        return SingletonHolder.instance;
    }

    private String ltColorMap;

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
            ImauSettings.MAX_SHF = props.getFloatProperty("MAX_SHF");
            ImauSettings.MIN_SHF = props.getFloatProperty("MIN_SHF");
            ImauSettings.MAX_SFWF = props.getFloatProperty("MAX_SFWF");
            ImauSettings.MIN_SFWF = props.getFloatProperty("MIN_SFWF");
            ImauSettings.MAX_HMXL = props.getFloatProperty("MAX_HMXL");
            ImauSettings.MIN_HMXL = props.getFloatProperty("MIN_HMXL");

            ImauSettings.MAX_SALT = props.getFloatProperty("MAX_SALT");
            ImauSettings.MIN_SALT = props.getFloatProperty("MIN_SALT");
            ImauSettings.MAX_TEMP = props.getFloatProperty("MAX_TEMP");
            ImauSettings.MIN_TEMP = props.getFloatProperty("MIN_TEMP");

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

    public void setMaxSalt(float value) {
        MAX_SALT = value;
    }

    public void setMinSalt(float value) {
        MIN_SALT = value;
    }

    public void setMaxTemp(float value) {
        MAX_TEMP = value;
    }

    public void setMinTemp(float value) {
        MIN_TEMP = value;
    }

    public void setMaxSsh(float value) {
        MAX_SSH = value;
    }

    public void setMinSsh(float value) {
        MIN_SSH = value;
    }

    public void setMaxShf(float value) {
        MAX_SHF = value;
    }

    public void setMinShf(float value) {
        MIN_SHF = value;
    }

    public void setMaxSfwf(float value) {
        MAX_SFWF = value;
    }

    public void setMinSfwf(float value) {
        MIN_SFWF = value;
    }

    public void setMaxHmxl(float value) {
        MAX_HMXL = value;
    }

    public void setMinHmxl(float value) {
        MIN_HMXL = value;
    }

    public float getMaxSsh() {
        return MAX_SSH;
    }

    public float getMinSsh() {
        return MIN_SSH;
    }

    public float getMaxShf() {
        return MAX_SHF;
    }

    public float getMinShf() {
        return MIN_SHF;
    }

    public float getMaxSfwf() {
        return MAX_SFWF;
    }

    public float getMinSfwf() {
        return MIN_SFWF;
    }

    public float getMaxHmxl() {
        return MAX_HMXL;
    }

    public float getMinHmxl() {
        return MIN_HMXL;
    }

    public float getMaxSalt() {
        return MAX_SALT;
    }

    public float getMinSalt() {
        return MIN_SALT;
    }

    public float getMaxTemp() {
        return MAX_TEMP;
    }

    public float getMinTemp() {
        return MIN_TEMP;
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

    public void setDepthDef(int value) {
        DEPTH_DEF = value;
    }

    public int getDepthMax() {
        return DEPTH_MAX;
    }

    public void setDepthMax(int value) {
        DEPTH_MAX = value;
    }

    public String bandNameToString(varNames var) {
        if (var == varNames.SSH) {
            return "Sea Surface Height";
        } else if (var == varNames.SHF) {
            return "Total Surface Heat Flux";
        } else if (var == varNames.SFWF) {
            return "Virtual Salt Flux ";
        } else if (var == varNames.HMXL) {
            return "Mixed Layer Depth";
        } else if (var == varNames.SALT) {
            return "Salinity";
        } else if (var == varNames.TEMP) {
            return "Potential Temperature";
        } else {
            return "";
        }
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

    public void setLTBand(varNames band) {
        BandCombination result = new BandCombination(comboLT.selectedDepth,
                band, comboLT.colorMapFileName);
        comboLT = result;
    }

    public void setRTBand(varNames band) {
        BandCombination result = new BandCombination(comboRT.selectedDepth,
                band, comboRT.colorMapFileName);
        comboRT = result;
    }

    public void setLBBand(varNames band) {
        BandCombination result = new BandCombination(comboLB.selectedDepth,
                band, comboLB.colorMapFileName);
        comboLB = result;
    }

    public void setRBBand(varNames band) {
        BandCombination result = new BandCombination(comboRB.selectedDepth,
                band, comboRB.colorMapFileName);
        comboRB = result;
    }

    public BandCombination getBandComboLT() {
        return comboLT;
    }

    public BandCombination getBandComboRT() {
        return comboRT;
    }

    public BandCombination getBandComboLB() {
        return comboLB;
    }

    public BandCombination getBandComboRB() {
        return comboRB;
    }

    public void setLTColorMap(String selectedColorMap) {
        BandCombination result = new BandCombination(comboLT.selectedDepth,
                comboLT.band, selectedColorMap);
        comboLT = result;
    }

    public void setRTColorMap(String selectedColorMap) {
        BandCombination result = new BandCombination(comboRT.selectedDepth,
                comboRT.band, selectedColorMap);
        comboRT = result;
    }

    public void setLBColorMap(String selectedColorMap) {
        BandCombination result = new BandCombination(comboLB.selectedDepth,
                comboLB.band, selectedColorMap);
        comboLB = result;
    }

    public void setRBColorMap(String selectedColorMap) {
        BandCombination result = new BandCombination(comboRB.selectedDepth,
                comboRB.band, selectedColorMap);
        comboRB = result;
    }

    public boolean isDynamicDimensions() {
        return DYNAMIC_DIMENSIONS;
    }
}
