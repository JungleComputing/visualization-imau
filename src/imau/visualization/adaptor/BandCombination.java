package imau.visualization.adaptor;

import imau.visualization.ImauSettings.varNames;

public class BandCombination {
    public int      selectedDepth;
    public varNames band;
    public String   colorMapFileName;

    public BandCombination(int selectedDepth, varNames band,
            String ColorMapFileName) {
        this.selectedDepth = selectedDepth;
        this.band = band;
        this.colorMapFileName = ColorMapFileName;
    }

    @Override
    public int hashCode() {
        int hashCode = (selectedDepth + 187 * 3187 + band.hashCode() + 23
                * 6833 + colorMapFileName.hashCode());
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
        return (selectedDepth == that.selectedDepth && band == that.band && colorMapFileName
                .compareTo(that.colorMapFileName) == 0);
    }

    public int getBandIndex() {
        int result = 0;

        if (band == varNames.SSH) {
            result = 0;
        } else if (band == varNames.SHF) {
            result = 1;
        } else if (band == varNames.SFWF) {
            result = 2;
        } else if (band == varNames.HMXL) {
            result = 3;
        } else if (band == varNames.SALT) {
            result = 4;
        } else if (band == varNames.TEMP) {
            result = 5;
        }

        return result;
    }
}
