package imau.visualization.adaptor;

import imau.visualization.ImauSettings.varNames;

public class BandCombination {
    public int selectedDepth;
    public varNames redBand;
    public varNames greenBand;
    public varNames blueBand;

    public BandCombination(int selectedDepth, varNames redBand, varNames greenBand, varNames blueBand) {
        this.selectedDepth = selectedDepth;
        this.redBand = redBand;
        this.greenBand = greenBand;
        this.blueBand = blueBand;
    }

    @Override
    public int hashCode() {
        int hashCode = (int) (selectedDepth + 187 * 3187 + redBand.hashCode() + 23 * 6833 + greenBand.hashCode() + 7
                * 7207 + blueBand.hashCode() + 11 * 7919);
        return hashCode;
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof BandCombination))
            return false;

        // cast to native object is now safe
        BandCombination that = (BandCombination) thatObject;

        // now a proper field-by-field evaluation can be made
        return (selectedDepth == that.selectedDepth && redBand == that.redBand && greenBand == that.greenBand && blueBand == that.blueBand);
    }
}
