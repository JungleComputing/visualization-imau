package imau.visualization.adaptor;

import imau.visualization.ImauSettings;

public class GlobeState {
    private final ImauSettings settings = ImauSettings.getInstance();

    public static enum DataMode {
        FIRST_DATASET, SECOND_DATASET, DIFF
    };

    public static enum Variable {
        SSH, SHF, SFWF, HMXL, SALT, TEMP
    };

    private DataMode currentDataMode    = DataMode.FIRST_DATASET;
    private Variable currentVariable    = Variable.SSH;
    private int      currentFrameNumber = 0;
    private int      currentDepth       = 0;
    private String   currentColorMap    = "default";

    public GlobeState(DataMode dataMode, Variable var, int frameNumber,
            int depth, String colorMap) {
        this.currentDataMode = dataMode;
        this.currentVariable = var;
        this.currentFrameNumber = frameNumber;
        this.currentDepth = depth;
        this.currentColorMap = colorMap;
    }

    @Override
    public int hashCode() {
        int dataModePrime = (currentDataMode.hashCode() + 3) * 23;
        int variablePrime = (currentVariable.hashCode() + 67) * 859;
        int frameNumberPrime = (currentDepth + 131) * 1543;
        int depthPrime = (currentDepth + 251) * 2957;
        int colorMapPrime = (currentColorMap.hashCode() + 919) * 7883;

        int hashCode = frameNumberPrime + depthPrime + dataModePrime
                + variablePrime + colorMapPrime;

        return hashCode;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof GlobeState))
            return false;

        // cast to native object is now safe
        GlobeState that = (GlobeState) thatObject;

        // now a proper field-by-field evaluation can be made
        return (currentDataMode == that.currentDataMode
                && currentVariable == that.currentVariable
                && currentFrameNumber == that.currentFrameNumber
                && currentDepth == that.currentDepth && currentColorMap
                    .compareTo(that.currentColorMap) == 0);
    }

    public int getDataModeIndex() {
        int result = 0;

        if (currentDataMode == DataMode.FIRST_DATASET) {
            result = 0;
        } else if (currentDataMode == DataMode.SECOND_DATASET) {
            result = 1;
        } else if (currentDataMode == DataMode.DIFF) {
            result = 2;
        }

        return result;
    }

    public int getVariableIndex() {
        int result = 0;

        if (currentVariable == Variable.SSH) {
            result = 0;
        } else if (currentVariable == Variable.SHF) {
            result = 1;
        } else if (currentVariable == Variable.SFWF) {
            result = 2;
        } else if (currentVariable == Variable.HMXL) {
            result = 3;
        } else if (currentVariable == Variable.SALT) {
            result = 4;
        } else if (currentVariable == Variable.TEMP) {
            result = 5;
        }

        return result;
    }

    public static String verbalizeDataMode(int index) {
        String result = "";

        if (index == 0) {
            result = "First Dataset";
        } else if (index == 1) {
            result = "Second Dataset";
        } else if (index == 2) {
            result = "Difference";
        }

        return result;
    }

    public static String verbalizeVariable(int index) {
        String result = "";

        if (index == 0) {
            result = "Sea surface height";
        } else if (index == 1) {
            result = "Surface heat flux";
        } else if (index == 2) {
            result = "Virtual salt flux";
        } else if (index == 3) {
            result = "Mixed-layer depth";
        } else if (index == 4) {
            result = "Salinity";
        } else if (index == 5) {
            result = "Potential Temperature";
        }

        return result;
    }

    public static String verbalizeUnits(int index) {
        String result = "";

        if (index == 0) {
            result = "cm";
        } else if (index == 1) {
            result = "W/m\u00b2";
        } else if (index == 2) {
            result = "kg/m\u00b2/s";
        } else if (index == 3) {
            result = "cm";
        } else if (index == 4) {
            result = "g/g";
        } else if (index == 5) {
            result = "degC";
        }

        return result;
    }

    public static DataMode getDataModeByIndex(int index) {
        DataMode result = null;

        if (index == 0) {
            result = DataMode.FIRST_DATASET;
        } else if (index == 1) {
            result = DataMode.SECOND_DATASET;
        } else if (index == 2) {
            result = DataMode.DIFF;
        }

        return result;
    }

    public static Variable getVariableByIndex(int index) {
        Variable result = null;

        if (index == 0) {
            result = Variable.SSH;
        } else if (index == 1) {
            result = Variable.SHF;
        } else if (index == 2) {
            result = Variable.SFWF;
        } else if (index == 3) {
            result = Variable.HMXL;
        } else if (index == 4) {
            result = Variable.SALT;
        } else if (index == 5) {
            result = Variable.TEMP;
        }

        return result;
    }

    public DataMode getDataMode() {
        return currentDataMode;
    }

    public Variable getVariable() {
        return currentVariable;
    }

    public int getFrameNumber() {
        return currentFrameNumber;
    }

    public int getDepth() {
        return currentDepth;
    }

    public String getColorMap() {
        return currentColorMap;
    }

}
