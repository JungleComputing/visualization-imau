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

    public int getRedBandIndex() {
        int result = 0;

        if (redBand == varNames.SSH) {
            result = 0;
        } else if (redBand == varNames.SHF) {
            result = 1;
        } else if (redBand == varNames.SFWF) {
            result = 2;
        } else if (redBand == varNames.HMXL) {
            result = 3;
        } else if (redBand == varNames.SALT) {
            result = 4;
        } else if (redBand == varNames.TEMP) {
            result = 5;
        }

        return result;
    }

    public int getGreenBandIndex() {
        int result = 0;

        if (greenBand == varNames.SSH) {
            result = 0;
        } else if (greenBand == varNames.SHF) {
            result = 1;
        } else if (greenBand == varNames.SFWF) {
            result = 2;
        } else if (greenBand == varNames.HMXL) {
            result = 3;
        } else if (greenBand == varNames.SALT) {
            result = 4;
        } else if (greenBand == varNames.TEMP) {
            result = 5;
        }

        return result;
    }

    public int getBlueBandIndex() {
        int result = 0;

        if (blueBand == varNames.SSH) {
            result = 0;
        } else if (blueBand == varNames.SHF) {
            result = 1;
        } else if (blueBand == varNames.SFWF) {
            result = 2;
        } else if (blueBand == varNames.HMXL) {
            result = 3;
        } else if (blueBand == varNames.SALT) {
            result = 4;
        } else if (blueBand == varNames.TEMP) {
            result = 5;
        }

        return result;
    }
}
