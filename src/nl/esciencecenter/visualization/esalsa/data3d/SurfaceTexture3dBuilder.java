package nl.esciencecenter.visualization.esalsa.data3d;

import java.nio.ByteBuffer;

import nl.esciencecenter.visualization.esalsa.ImauSettings;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter.Color;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter.Dimensions;
import openglCommon.exceptions.UninitializedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurfaceTexture3dBuilder implements Runnable {
    private final static Logger           logger   = LoggerFactory
                                                           .getLogger(SurfaceTexture3dBuilder.class);
    private final ImauSettings            settings = ImauSettings.getInstance();

    protected SurfaceTexture3dDescription description;

    private final Imau3dDataArray         inputArray;
    private final Texture3dStorage        texStore;
    private boolean                       initialized;
    private final int                     imageHeight;
    private final int                     blankRows;

    public SurfaceTexture3dBuilder(Texture3dStorage texStore,
            Imau3dDataArray inputArray, int imageHeight, int blankRows) {
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
                int dsDepth = inputArray.getDepth();

                int pixels = dsDepth * imageHeight * dsWidth;

                ByteBuffer outBuf = ByteBuffer.allocate(pixels * 4);
                outBuf.clear();
                outBuf.rewind();

                for (int d = 0; d < dsDepth; d++) {
                    for (int i = 0; i < blankRows; i++) {
                        for (int w = 0; w < dsWidth; w++) {
                            outBuf.put((byte) 0);
                            outBuf.put((byte) 0);
                            outBuf.put((byte) 0);
                            outBuf.put((byte) 0);
                        }
                    }

                    Dimensions dims = getDimensions(inputArray.getDescription());

                    String mapName = description.getColorMap();
                    float[] data = inputArray.getData();

                    for (int row = dsHeight - 1; row >= 0; row--) {
                        for (int col = 0; col < dsWidth; col++) {
                            int i = (row * dsWidth + col);
                            Color c = ColormapInterpreter.getColor(mapName,
                                    dims, data[i]);

                            outBuf.put((byte) (255 * c.red));
                            outBuf.put((byte) (255 * c.green));
                            outBuf.put((byte) (255 * c.blue));
                            outBuf.put((byte) 0);
                        }
                    }

                    while (outBuf.hasRemaining()) {
                        outBuf.put((byte) 0);
                    }

                }
                outBuf.flip();

                texStore.setSurfaceImage(description, outBuf);
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
        if (!(thatObject instanceof SurfaceTexture3dBuilder))
            return false;

        // cast to native object is now safe
        SurfaceTexture3dBuilder that = (SurfaceTexture3dBuilder) thatObject;

        // now a proper field-by-field evaluation can be made
        return (description.equals(that.description));
    }

    public Dimensions getDimensions(SurfaceTexture3dDescription desc) {
        float max = 0;
        float min = 0;

        max = settings.getCurrentVarMax(desc.getVarName());
        min = settings.getCurrentVarMin(desc.getVarName());

        return new Dimensions(min, max);
    }
}
