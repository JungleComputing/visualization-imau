package imau.visualization.adaptor;

import imau.visualization.adaptor.ImageMaker.Dimensions;

public class NetCDFDataGrid {
    private final GlobeState state;
    private final int        width;
    private final int        height;
    private final float[]    data;

    public NetCDFDataGrid(GlobeState state, int width, int height, float[] data) {
        this.state = state;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public float[] getData() {
        return data;
    }

    public GlobeState getState() {
        return state;
    }

    public Dimensions getDynamicDimensions() {
        int pixels = width * height;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        float avg = 0f;

        float total = 0f;
        for (int i = 0; i < pixels; i++) {
            float val = data[i];

            if (val > -1E33) {
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }

                total += val;
            }
        }

        avg = total / pixels;
        float minDiff = avg - min;
        float maxDiff = max - avg;

        if (maxDiff > minDiff) {
            min = avg - maxDiff;
        } else if (minDiff > maxDiff) {
            max = avg + minDiff;
        }

        Dimensions dim = new Dimensions(min, max);

        return dim;
    }

    public Dimensions getDynamicDimensions(NetCDFDataGrid other) {
        float[] otherData = other.getData();
        int pixels = width * height;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        float avg = 0f;

        float total = 0f;
        for (int i = 0; i < pixels; i++) {
            float val1 = data[i];
            float val2 = otherData[i];
            float val = val1 - val2;

            if (val > -1E33) {
                if (val > max) {
                    max = val;
                }
                if (val < min) {
                    min = val;
                }

                total += val;
            }
        }

        avg = total / pixels;
        float minDiff = avg - min;
        float maxDiff = max - avg;

        if (maxDiff > minDiff) {
            min = avg - maxDiff;
        } else if (minDiff > maxDiff) {
            max = avg + minDiff;
        }

        Dimensions dim = new Dimensions(min, max);

        return dim;
    }

    public Dimensions getStaticDimensions() {
        Dimensions dims = ImageMaker.getDimensions(state);

        return dims;
    }
}
