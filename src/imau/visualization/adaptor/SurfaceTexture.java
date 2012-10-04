package imau.visualization.adaptor;

import imau.visualization.adaptor.ImageMaker.Color;
import imau.visualization.adaptor.ImageMaker.Dimensions;

import java.nio.ByteBuffer;

import openglCommon.exceptions.UninitializedException;

public class SurfaceTexture implements Runnable {
    protected SurfaceTextureDescription description;

    private final NetCDFArray           inputArray;
    private final TextureStorage        texStore;
    private boolean                     initialized;
    private final int                   imageHeight;
    private final int                   blankRows;

    public SurfaceTexture(TextureStorage texStore, NetCDFArray inputArray,
            int imageHeight, int blankRows) {
        this.texStore = texStore;
        this.inputArray = inputArray;
        this.description = inputArray.getDescription();

        this.imageHeight = imageHeight;
        this.blankRows = blankRows;
    }

    @Override
    public void run() {
        if (!initialized) {
            try {
                int dsWidth = inputArray.getWidth();
                int dsHeight = inputArray.getHeight();

                int pixels = imageHeight * dsWidth;

                ByteBuffer outBuf = ByteBuffer.allocate(pixels * 4);
                outBuf.clear();
                outBuf.rewind();

                for (int i = 0; i < blankRows; i++) {
                    for (int w = 0; w < dsWidth; w++) {
                        outBuf.put((byte) 0);
                        outBuf.put((byte) 0);
                        outBuf.put((byte) 0);
                        outBuf.put((byte) 0);
                    }
                }

                Dimensions dims;

                if (description.isDynamicDimensions()) {
                    dims = ImageMaker.getDynamicDimensions(
                            description.getVarName(), inputArray.getData());
                } else {
                    dims = ImageMaker
                            .getDimensions(inputArray.getDescription());
                }

                String mapName = description.getColorMap();
                float[] data = inputArray.getData();

                for (int row = dsHeight - 1; row >= 0; row--) {
                    for (int col = 0; col < dsWidth; col++) {
                        int i = (row * dsWidth + col);
                        Color c = ImageMaker.getColor(mapName, dims, data[i]);

                        outBuf.put((byte) (255 * c.red));
                        outBuf.put((byte) (255 * c.green));
                        outBuf.put((byte) (255 * c.blue));
                        outBuf.put((byte) 0);
                    }
                }

                while (outBuf.hasRemaining()) {
                    outBuf.put((byte) 0);
                }

                outBuf.flip();

                texStore.setSurfaceImage(description, outBuf);
                texStore.setDimensions(description, dims);

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
        SurfaceTexture that = (SurfaceTexture) thatObject;

        // now a proper field-by-field evaluation can be made
        return (description.equals(that.description));
    }
}
