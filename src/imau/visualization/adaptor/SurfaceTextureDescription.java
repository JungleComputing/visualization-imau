package imau.visualization.adaptor;

public class SurfaceTextureDescription {
    protected final int     frameNumber;
    protected final int     depth;
    protected final String  varName;
    protected final String  colorMap;
    protected final boolean dynamicDimensions;
    protected final boolean diff;
    protected final boolean secondSet;

    public SurfaceTextureDescription(int frameNumber, int depth,
            String varName, String colorMap, boolean dynamicDimensions,
            boolean diff, boolean secondSet) {
        this.frameNumber = frameNumber;
        this.depth = depth;
        this.varName = varName;
        this.colorMap = colorMap;
        this.dynamicDimensions = dynamicDimensions;
        this.diff = diff;
        this.secondSet = secondSet;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public int getDepth() {
        return depth;
    }

    public String getVarName() {
        return varName;
    }

    public String getColorMap() {
        return colorMap;
    }

    public boolean isDynamicDimensions() {
        return dynamicDimensions;
    }

    public boolean isDiff() {
        return diff;
    }

    public boolean isSecondSet() {
        return secondSet;
    }

    @Override
    public int hashCode() {
        int dataModePrime = (frameNumber + 3) * 23;
        int dynamicPrime = ((dynamicDimensions ? 1 : 3) + 41) * 313;
        int diffPrime = ((diff ? 3 : 5) + 43) * 313;
        int secondPrime = ((diff ? 5 : 7) + 53) * 313;
        int variablePrime = (varName.hashCode() + 67) * 859;
        int frameNumberPrime = (frameNumber + 131) * 1543;
        int depthPrime = (depth + 251) * 2957;
        int colorMapPrime = (colorMap.hashCode() + 919) * 7883;

        int hashCode = frameNumberPrime + dynamicPrime + diffPrime
                + secondPrime + depthPrime + dataModePrime + variablePrime
                + colorMapPrime;

        return hashCode;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof GlobeState))
            return false;

        // cast to native object is now safe
        SurfaceTextureDescription that = (SurfaceTextureDescription) thatObject;

        // now a proper field-by-field evaluation can be made
        return (dynamicDimensions == that.dynamicDimensions
                && diff == that.diff && secondSet == that.secondSet
                && varName.compareTo(that.varName) == 0
                && frameNumber == that.frameNumber && depth == that.depth && colorMap
                    .compareTo(that.colorMap) == 0);
    }

}
