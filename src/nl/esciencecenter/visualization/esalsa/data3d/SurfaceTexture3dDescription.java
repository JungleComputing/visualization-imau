package nl.esciencecenter.visualization.esalsa.data3d;

public class SurfaceTexture3dDescription {
    protected final int    frameNumber;
    protected final String varName;
    protected final String colorMap;
    protected final float  lowerBound;
    protected final float  upperBound;

    public SurfaceTexture3dDescription(int frameNumber, String varName,
            String colorMap, boolean dynamicDimensions, boolean diff,
            float lowerBound, float upperBound) {
        this.frameNumber = frameNumber;
        this.varName = varName;
        this.colorMap = colorMap;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public String getVarName() {
        return varName;
    }

    public String getColorMap() {
        return colorMap;
    }

    public float getLowerBound() {
        return lowerBound;
    }

    public float getUpperBound() {
        return upperBound;
    }

    @Override
    public int hashCode() {
        int dataModePrime = (frameNumber + 3) * 23;
        int variablePrime = (varName.hashCode() + 67) * 859;
        int frameNumberPrime = (frameNumber + 131) * 1543;
        int colorMapPrime = (colorMap.hashCode() + 919) * 7883;
        int lowerBoundPrime = (int) ((lowerBound + 41) * 1543);
        int upperBoundPrime = (int) ((upperBound + 67) * 2957);

        int hashCode = frameNumberPrime + dataModePrime + variablePrime
                + colorMapPrime + lowerBoundPrime + upperBoundPrime;

        return hashCode;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof SurfaceTexture3dDescription))
            return false;

        // cast to native object is now safe
        SurfaceTexture3dDescription that = (SurfaceTexture3dDescription) thatObject;

        // now a proper field-by-field evaluation can be made
        return (varName.compareTo(that.varName) == 0
                && frameNumber == that.frameNumber
                && lowerBound == that.lowerBound
                && upperBound == that.upperBound && colorMap
                    .compareTo(that.colorMap) == 0);
    }

    public int getDataModeIndex() {
        return 0;
    }

    public String verbalizeDataMode() {
        return "Control";
    }

    public static String[] getDataModes() {
        return new String[] { "Control" };
    }
}
