package imau.visualization.adaptor;

import imau.visualization.adaptor.ImageMaker.Color;
import imau.visualization.adaptor.ImageMaker.Dimensions;

import java.nio.ByteBuffer;

import openglCommon.exceptions.UninitializedException;

public class LegendTexture implements Runnable {
    protected SurfaceTextureDescription description;

    private final NetCDFArray           inputArray;
    private final TextureStorage        texStore;
    private boolean                     initialized;

    public LegendTexture(TextureStorage texStore, NetCDFArray inputArray) {
        this.texStore = texStore;
        this.inputArray = inputArray;
        this.description = inputArray.getDescription();
    }

    @Override
    public void run() {
        if (!initialized) {
            try {
                Dimensions dims;

                if (description.isDynamicDimensions()) {
                    dims = ImageMaker.getDynamicDimensions(
                            description.getVarName(), inputArray.getData());
                } else {
                    dims = ImageMaker
                            .getDimensions(inputArray.getDescription());
                }

                int height = 500;
                int width = 1;
                ByteBuffer outBuf = ByteBuffer.allocate(height * width * 4);

                for (int row = height - 1; row >= 0; row--) {
                    float index = row / (float) height;
                    float var = (index * dims.getDiff()) + dims.min;

                    Color c = ImageMaker.getColor(description.getColorMap(),
                            dims, var);

                    for (int col = 0; col < width; col++) {
                        outBuf.put((byte) (255 * c.red));
                        outBuf.put((byte) (255 * c.green));
                        outBuf.put((byte) (255 * c.blue));
                        outBuf.put((byte) 1);
                    }
                }

                outBuf.flip();

                texStore.setLegendImage(description, outBuf);
            } catch (UninitializedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }

    @Override
    public boolean equals(Object thatObject) {
        if (this == thatObject)
            return true;
        if (!(thatObject instanceof GlobeState))
            return false;

        // cast to native object is now safe
        LegendTexture that = (LegendTexture) thatObject;

        // now a proper field-by-field evaluation can be made
        return (description.equals(that.description));
    }
}
