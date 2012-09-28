package imau.visualization.adaptor;

public class SurfaceTextureDescription {
    protected final int     frameNumber;
    protected final int     depth;
    protected final String  varName;
    protected final String  colorMap;
    protected final boolean dynamicDimensions;

    public SurfaceTextureDescription(int frameNumber, int depth,
            String varName, String colorMap, boolean dynamicDimensions) {
        this.frameNumber = frameNumber;
        this.depth = depth;
        this.varName = varName;
        this.colorMap = colorMap;
        this.dynamicDimensions = dynamicDimensions;
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

    @Override
    public int hashCode() {
        int dataModePrime = (frameNumber + 3) * 23;
        int dynamicPrime = ((dynamicDimensions ? 1 : 3) + 41) * 313;
        int variablePrime = (varName.hashCode() + 67) * 859;
        int frameNumberPrime = (frameNumber + 131) * 1543;
        int depthPrime = (depth + 251) * 2957;
        int colorMapPrime = (colorMap.hashCode() + 919) * 7883;

        int hashCode = frameNumberPrime + dynamicPrime + depthPrime
                + dataModePrime + variablePrime + colorMapPrime;

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
                && varName.compareTo(that.varName) == 0
                && frameNumber == that.frameNumber && depth == that.depth && colorMap
                    .compareTo(that.colorMap) == 0);
    }

}
