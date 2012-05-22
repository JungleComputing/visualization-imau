package imau.visualization;

import openglCommon.util.Settings;
import openglCommon.util.TypedProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImauSettings extends Settings {
    private final static Logger logger = LoggerFactory.getLogger(ImauSettings.class);

    private static class SingletonHolder {
        public final static ImauSettings instance = new ImauSettings();
    }

    private static long WAITTIME_FOR_RETRY = 10000;
    private static long WAITTIME_FOR_MOVIE = 100;
    private static float EPSILON = 1.0E-7f;

    private static int FILE_EXTENSION_LENGTH = 2;
    private static int FILE_NUMBER_LENGTH = 4;

    private static String[] ACCEPTABLE_POSTFIXES = { ".nc" };

    private static String CURRENT_POSTFIX = ".nc";

    private static int PREPROCESSING_AMOUNT = 5;

    public static ImauSettings getInstance() {
        return SingletonHolder.instance;
    }

    private ImauSettings() {
        super();

        try {
            final TypedProperties props = new TypedProperties();
            props.loadFromFile("settings.properties");

            ImauSettings.WAITTIME_FOR_RETRY = props.getLongProperty("WAITTIME_FOR_RETRY");
            ImauSettings.WAITTIME_FOR_MOVIE = props.getLongProperty("WAITTIME_FOR_MOVIE");
            ImauSettings.EPSILON = props.getFloatProperty("EPSILON");

            ImauSettings.FILE_EXTENSION_LENGTH = props.getIntProperty("FILE_EXTENSION_LENGTH");
            ImauSettings.FILE_NUMBER_LENGTH = props.getIntProperty("FILE_NUMBER_LENGTH");

            ImauSettings.PREPROCESSING_AMOUNT = props.getIntProperty("PREPROCESSING_AMOUNT");
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
}
