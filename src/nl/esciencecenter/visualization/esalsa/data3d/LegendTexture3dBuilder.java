package nl.esciencecenter.visualization.esalsa.data3d;

import java.nio.ByteBuffer;

import nl.esciencecenter.visualization.esalsa.ImauSettings;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter.Color;
import nl.esciencecenter.visualization.esalsa.util.ColormapInterpreter.Dimensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendTexture3dBuilder implements Runnable {
    private final static Logger           logger   = LoggerFactory
                                                           .getLogger(LegendTexture3dBuilder.class);
    private final ImauSettings            settings = ImauSettings.getInstance();

    protected SurfaceTexture3dDescription description;

    private final Imau3dDataArray         inputArray;
    private final Texture3dStorage        texStore;
    private boolean                       initialized;

    public LegendTexture3dBuilder(Texture3dStorage texStore,
            Imau3dDataArray inputArray) {
        this.texStore = texStore;
        this.inputArray = inputArray;
        this.description = inputArray.getDescription();
    }

    @Override
    public void run() {
        if (!initialized) {
            Dimensions dims = getDimensions(inputArray.getDescription());

            int height = 500;
            int width = 1;
            ByteBuffer outBuf = ByteBuffer.allocate(height * width * 4);

            for (int row = height - 1; row >= 0; row--) {
                float index = row / (float) height;
                float var = (index * dims.getDiff()) + dims.min;

                Color c = ColormapInterpreter.getColor(
                        description.getColorMap(), dims, var);

                for (int col = 0; col < width; col++) {
                    outBuf.put((byte) (255 * c.red));
                    outBuf.put((byte) (255 * c.green));
                    outBuf.put((byte) (255 * c.blue));
                    outBuf.put((byte) 1);
                }
            }

            outBuf.flip();

            texStore.setLegendImage(description, outBuf);
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
        if (!(thatObject instanceof LegendTexture3dBuilder))
            return false;

        // cast to native object is now safe
        LegendTexture3dBuilder that = (LegendTexture3dBuilder) thatObject;

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
