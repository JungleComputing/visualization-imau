package imau.visualization.adaptor;

import imau.visualization.adaptor.ImageMaker.Dimensions;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class TextureStorage {
    private final HashMap<Integer, SurfaceTextureDescription>    oldScreen;
    private final HashMap<Integer, SurfaceTextureDescription>    newScreen;
    private final HashMap<Integer, SurfaceTextureDescription>    futureScreen;
    private final HashMap<SurfaceTextureDescription, ByteBuffer> surfaceStorage;
    private final HashMap<SurfaceTextureDescription, ByteBuffer> legendStorage;
    private final HashMap<SurfaceTextureDescription, Dimensions> dimensionsStorage;

    private final int                                            width;
    private final int                                            height;

    private final NetCDFDatasetManager2                          manager;

    public TextureStorage(NetCDFDatasetManager2 manager, int width, int height) {
        oldScreen = new HashMap<Integer, SurfaceTextureDescription>();
        newScreen = new HashMap<Integer, SurfaceTextureDescription>();
        futureScreen = new HashMap<Integer, SurfaceTextureDescription>();
        surfaceStorage = new HashMap<SurfaceTextureDescription, ByteBuffer>();
        legendStorage = new HashMap<SurfaceTextureDescription, ByteBuffer>();
        dimensionsStorage = new HashMap<SurfaceTextureDescription, Dimensions>();

        this.width = width;
        this.height = height;
        this.manager = manager;
    }

    public synchronized ByteBuffer getSurfaceImage(int screenNumber) {
        ByteBuffer result = null;
        if (newScreen.containsKey(screenNumber)) {
            if (surfaceStorage.containsKey(newScreen.get(screenNumber))) {
                SurfaceTextureDescription desc = oldScreen.remove(screenNumber);
                surfaceStorage.remove(desc);

                result = surfaceStorage.get(newScreen.get(screenNumber));
            } else {
                result = surfaceStorage.get(oldScreen.get(screenNumber));
            }
        }

        if (result != null) {
            return result;
        } else {
            return ByteBuffer.allocate(width * height * 4);
        }

    }

    public synchronized ByteBuffer getLegendImage(int screenNumber) {
        ByteBuffer result = null;
        if (newScreen.containsKey(screenNumber)) {
            if (legendStorage.containsKey(newScreen.get(screenNumber))) {
                SurfaceTextureDescription desc = oldScreen.remove(screenNumber);
                legendStorage.remove(desc);

                result = legendStorage.get(newScreen.get(screenNumber));
            } else {
                result = legendStorage.get(oldScreen.get(screenNumber));
            }
        }

        if (result != null) {
            return result;
        } else {
            return ByteBuffer.allocate(1 * 500 * 4);
        }
    }

    public synchronized Dimensions getDimensions(int screenNumber) {
        // if (newScreen.containsKey(screenNumber)) {
        // if (dimensionsStorage.containsKey(newScreen.get(screenNumber))) {
        // SurfaceTextureDescription desc = oldScreen.remove(screenNumber);
        // dimensionsStorage.remove(desc);
        //
        // return dimensionsStorage.get(newScreen.get(screenNumber));
        // } else {
        // return dimensionsStorage.get(oldScreen.get(screenNumber));
        // }
        // }

        return new Dimensions(0, 0);
    }

    public synchronized void requestNewConfiguration(int screenNumber,
            SurfaceTextureDescription newDesc) {
        SurfaceTextureDescription oldDesc = newScreen.get(screenNumber);
        oldScreen.put(screenNumber, oldDesc);

        newScreen.put(screenNumber, newDesc);
        if (!surfaceStorage.containsValue(newDesc)
                || !legendStorage.containsValue(newDesc)
                || !dimensionsStorage.containsValue(newDesc)) {
            manager.buildImages(newDesc);
        }
    }

    public synchronized void requestNewFrame(int frameNumber) {
        for (int i = 0; i < 4; i++) {
            if (oldScreen.containsKey(i)) {
                SurfaceTextureDescription oldDesc = newScreen.get(i);
                oldScreen.put(i, oldDesc);

                SurfaceTextureDescription newDesc = new SurfaceTextureDescription(
                        frameNumber, oldDesc.getDepth(), oldDesc.getVarName(),
                        oldDesc.getColorMap(), oldDesc.isDynamicDimensions(),
                        oldDesc.isDiff(), oldDesc.isSecondSet());

                requestNewConfiguration(i, newDesc);

                SurfaceTextureDescription futureDesc = new SurfaceTextureDescription(
                        frameNumber + 1, oldDesc.getDepth(),
                        oldDesc.getVarName(), oldDesc.getColorMap(),
                        oldDesc.isDynamicDimensions(), oldDesc.isDiff(),
                        oldDesc.isSecondSet());

                futureScreen.put(i, futureDesc);
                manager.buildImages(futureDesc);
            }
        }
    }

    public synchronized void setSurfaceImage(SurfaceTextureDescription desc,
            ByteBuffer data) {
        if (newScreen.containsValue(desc)) {
            surfaceStorage.put(desc, data);
        }
    }

    public void setLegendImage(SurfaceTextureDescription desc, ByteBuffer data) {
        if (newScreen.containsValue(desc)) {
            legendStorage.put(desc, data);
        }
    }

    public void setDimensions(SurfaceTextureDescription desc, Dimensions dims) {
        if (newScreen.containsValue(desc)) {
            dimensionsStorage.put(desc, dims);
        }
    }

    public boolean doneWithLastRequest() {
        boolean failure = false;

        for (SurfaceTextureDescription desc : newScreen.values()) {
            if (surfaceStorage.get(desc) == null
                    || legendStorage.get(desc) == null
                    || dimensionsStorage.get(desc) == null) {
                failure = true;
            }
        }

        return !failure;
    }
}
