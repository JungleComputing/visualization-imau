package imau.visualization.adaptor;

import imau.visualization.ImauSettings;

public class GlobeState {
    private final ImauSettings settings = ImauSettings.getInstance();

    public static enum DataMode {
        FIRST_DATASET, SECOND_DATASET, DIFF
    };

    public static enum Variable {
        SSH, SHF, SFWF, HMXL, SALT, TEMP, UVEL, VVEL, KE, PD, TAUX, TAUY, H2
    };

    private DataMode currentDataMode    = DataMode.FIRST_DATASET;
    private boolean  dynamicDimensions  = false;
    private Variable currentVariable    = Variable.SSH;
    private int      currentFrameNumber = 0;
    private int      currentDepth       = 0;
    private String   currentColorMap    = "default";
    private float    lowerBound         = 0f;
    private float    upperBound         = 100f;

    public GlobeState(DataMode dataMode, boolean dynamicDimensions,
            Variable var, int frameNumber, int depth, String colorMap,
            float lowerBound, float upperBound) {
        this.currentDataMode = dataMode;
        this.dynamicDimensions = dynamicDimensions;
        this.currentVariable = var;
        this.currentFrameNumber = frameNumber;
        this.currentDepth = depth;
        this.currentColorMap = colorMap;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public int hashCode() {
        int dataModePrime = (currentDataMode.hashCode() + 3) * 23;
        int dynamicPrime = ((dynamicDimensions ? 1 : 3) + 41) * 313;
        int variablePrime = (currentVariable.hashCode() + 67) * 859;
        int frameNumberPrime = (currentDepth + 131) * 1543;
        int lowerBoundPrime = (int) ((lowerBound + 41) * 1543);
        int upperBoundPrime = (int) ((upperBound + 67) * 2957);
        int depthPrime = (currentDepth + 251) * 2957;
        int colorMapPrime = (currentColorMap.hashCode() + 919) * 7883;

        int hashCode = frameNumberPrime + dynamicPrime + depthPrime
                + dataModePrime + variablePrime + colorMapPrime
                + lowerBoundPrime + upperBoundPrime;

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
                && dynamicDimensions == that.dynamicDimensions
                && currentVariable == that.currentVariable
                && currentFrameNumber == that.currentFrameNumber
                && currentDepth == that.currentDepth
                && lowerBound == that.lowerBound
                && upperBound == that.upperBound && currentColorMap
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
        } else if (currentVariable == Variable.UVEL) {
            result = 6;
        } else if (currentVariable == Variable.VVEL) {
            result = 7;
        } else if (currentVariable == Variable.KE) {
            result = 8;
        } else if (currentVariable == Variable.PD) {
            result = 9;
        } else if (currentVariable == Variable.TAUX) {
            result = 10;
        } else if (currentVariable == Variable.TAUY) {
            result = 11;
        } else if (currentVariable == Variable.H2) {
            result = 12;
        }

        return result;
    }

    public static String verbalizeDataMode(int index) {
        String result = "";

        if (index == 0) {
            result = "Control";
        } else if (index == 1) {
            result = "0.5 Sv";
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
        } else if (index == 6) {
            result = "Velocity in grid-x dir.";
        } else if (index == 7) {
            result = "Velocity in grid-y dir.";
        } else if (index == 8) {
            result = "Horizontal Kinetic Energy";
        } else if (index == 9) {
            result = "Potential Density";
        } else if (index == 10) {
            result = "Windstress in grid-x dir.";
        } else if (index == 11) {
            result = "Windstress in grid-y dir.";
        } else if (index == 12) {
            result = "Sea surface height ^2";
        }

        return result;
    }

    public static String verbalizeUnits(int index) {
        String result = "";

        if (index == 0) {
            result = "cm";
        } else if (index == 1) {
            result = "W / m\u00b2";
        } else if (index == 2) {
            result = "kg / m\u00b2/s";
        } else if (index == 3) {
            result = "cm";
        } else if (index == 4) {
            result = "g / g";
        } else if (index == 5) {
            result = "degC";
        } else if (index == 6) {
            result = "cm / s";
        } else if (index == 7) {
            result = "cm / s";
        } else if (index == 8) {
            result = "cm\u00b2 / s\u00b2";
        } else if (index == 9) {
            result = "g / cm\u00b3";
        } else if (index == 10) {
            result = "dyne / cm\u00b2";
        } else if (index == 11) {
            result = "dyne / cm\u00b2";
        } else if (index == 12) {
            result = "cm\u00b2";
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
        } else if (index == 6) {
            result = Variable.UVEL;
        } else if (index == 7) {
            result = Variable.VVEL;
        } else if (index == 8) {
            result = Variable.KE;
        } else if (index == 9) {
            result = Variable.PD;
        } else if (index == 10) {
            result = Variable.TAUX;
        } else if (index == 11) {
            result = Variable.TAUY;
        } else if (index == 12) {
            result = Variable.H2;
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

    public boolean isDynamicDimensions() {
        return dynamicDimensions;
    }

    public float getLowerBound() {
        return lowerBound;
    }

    public float getUpperBound() {
        return upperBound;
    }

}
