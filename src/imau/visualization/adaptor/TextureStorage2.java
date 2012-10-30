package imau.visualization.adaptor;

import imau.visualization.adaptor.ImageMaker.Dimensions;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class TextureStorage2 {
    private final HashMap<Integer, SurfaceTextureDescription>    oldScreen;
    private final HashMap<Integer, SurfaceTextureDescription>    newScreen;
    private final HashMap<SurfaceTextureDescription, ByteBuffer> surfaceStorage;
    private final HashMap<SurfaceTextureDescription, ByteBuffer> legendStorage;
    private final HashMap<SurfaceTextureDescription, Dimensions> dimensionsStorage;

    private final int                                            width;
    private final int                                            height;

    private final NetCDFDatasetManager2                          manager;

    public TextureStorage2(NetCDFDatasetManager2 manager, int width, int height) {
        oldScreen = new HashMap<Integer, SurfaceTextureDescription>();
        newScreen = new HashMap<Integer, SurfaceTextureDescription>();
        surfaceStorage = new HashMap<SurfaceTextureDescription, ByteBuffer>();
        legendStorage = new HashMap<SurfaceTextureDescription, ByteBuffer>();
        dimensionsStorage = new HashMap<SurfaceTextureDescription, Dimensions>();

        this.width = width;
        this.height = height;
        this.manager = manager;
    }

    public synchronized ByteBuffer getSurfaceImage(int screenNumber) {
        SurfaceTextureDescription newDesc = newScreen.get(screenNumber);
        SurfaceTextureDescription oldDesc = oldScreen.get(screenNumber);

        ByteBuffer result = null;
        if (newScreen.containsKey(screenNumber)) {
            if (surfaceStorage.containsKey(newDesc)) {
                if (!newDesc.equals(oldDesc)) {
                    surfaceStorage.remove(oldDesc);
                }

                result = surfaceStorage.get(newDesc);
            } else {
                result = surfaceStorage.get(oldDesc);
            }
        } else if (oldScreen.containsKey(screenNumber)) {
            result = surfaceStorage.get(oldDesc);
        }

        if (result != null) {
            return result;
        } else {
            return ByteBuffer.allocate(width * height * 4);
        }
    }

    public synchronized ByteBuffer getLegendImage(int screenNumber) {
        SurfaceTextureDescription newDesc = newScreen.get(screenNumber);
        SurfaceTextureDescription oldDesc = oldScreen.get(screenNumber);

        ByteBuffer result = null;
        if (newScreen.containsKey(screenNumber)) {
            if (legendStorage.containsKey(newDesc)) {
                if (!newDesc.equals(oldDesc)) {
                    legendStorage.remove(oldDesc);
                }

                result = legendStorage.get(newDesc);
            } else {
                result = legendStorage.get(oldDesc);
            }
        } else if (oldScreen.containsKey(screenNumber)) {
            result = legendStorage.get(oldDesc);
        }

        if (result != null) {
            return result;
        } else {
            return ByteBuffer.allocate(1 * 500 * 4);
        }
    }

    public synchronized Dimensions getDimensions(int screenNumber) {
        SurfaceTextureDescription newDesc = newScreen.get(screenNumber);
        SurfaceTextureDescription oldDesc = oldScreen.get(screenNumber);

        Dimensions result = null;
        if (newScreen.containsKey(screenNumber)) {
            if (dimensionsStorage.containsKey(newDesc)) {
                if (!newDesc.equals(oldDesc)) {
                    dimensionsStorage.remove(oldDesc);
                }

                result = dimensionsStorage.get(newDesc);
            } else {
                result = dimensionsStorage.get(oldDesc);
            }
        } else if (oldScreen.containsKey(screenNumber)) {
            result = dimensionsStorage.get(oldDesc);
        }

        if (result != null) {
            return result;
        } else {
            return new Dimensions(0, 0);
        }
    }

    public synchronized void requestNewConfiguration(int screenNumber,
            SurfaceTextureDescription newDesc) {
        if (newDesc != null && !surfaceStorage.containsValue(newDesc)
                && !legendStorage.containsValue(newDesc)
                && !dimensionsStorage.containsValue(newDesc)) {

            if (newScreen.containsKey(screenNumber)) {
                SurfaceTextureDescription oldDesc = newScreen.get(screenNumber);
                oldScreen.put(screenNumber, oldDesc);
            } else {
                oldScreen.put(screenNumber, newDesc);
            }

            newScreen.put(screenNumber, newDesc);
            manager.buildImages(newDesc);
        }
    }

    public synchronized void requestNewFrame(int frameNumber) {
        for (int i = 0; i < 4; i++) {
            if (oldScreen.containsKey(i)) {
                SurfaceTextureDescription oldDesc = newScreen.get(i);
                SurfaceTextureDescription newDesc = new SurfaceTextureDescription(
                        frameNumber, oldDesc.getDepth(), oldDesc.getVarName(),
                        oldDesc.getColorMap(), oldDesc.isDynamicDimensions(),
                        oldDesc.isDiff(), oldDesc.isSecondSet(),
                        oldDesc.getLowerBound(), oldDesc.getUpperBound());

                requestNewConfiguration(i, newDesc);
            }
        }
    }

    public synchronized void setSurfaceImage(SurfaceTextureDescription desc,
            ByteBuffer data) {
        if (newScreen.containsValue(desc) || oldScreen.containsValue(desc)) {
            surfaceStorage.put(desc, data);
        }
    }

    public void setLegendImage(SurfaceTextureDescription desc, ByteBuffer data) {
        if (newScreen.containsValue(desc) || oldScreen.containsValue(desc)) {
            legendStorage.put(desc, data);
        }
    }

    public void setDimensions(SurfaceTextureDescription desc, Dimensions dims) {
        if (newScreen.containsValue(desc) || oldScreen.containsValue(desc)) {
            dimensionsStorage.put(desc, dims);
        }
    }
}
