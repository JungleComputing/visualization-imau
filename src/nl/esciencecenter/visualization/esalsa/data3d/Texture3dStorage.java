package nl.esciencecenter.visualization.esalsa.data3d;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import nl.esciencecenter.visualization.esalsa.data.TextureStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.common.nio.Buffers;

public class Texture3dStorage {
    private final static Logger                              logger = LoggerFactory
                                                                            .getLogger(TextureStorage.class);

    private final SurfaceTexture3dDescription[]              oldScreenA;
    private final SurfaceTexture3dDescription[]              newScreenA;
    private HashMap<SurfaceTexture3dDescription, ByteBuffer> surfaceStorage;
    private HashMap<SurfaceTexture3dDescription, ByteBuffer> legendStorage;

    private final Imau3dDatasetManager                       manager;

    private final ByteBuffer                                 EMPTY_SURFACE_BUFFER;
    private final ByteBuffer                                 EMPTY_LEGEND_BUFFER;

    public Texture3dStorage(Imau3dDatasetManager manager, int screens,
            int width, int height) {
        oldScreenA = new SurfaceTexture3dDescription[screens];
        newScreenA = new SurfaceTexture3dDescription[screens];

        surfaceStorage = new HashMap<SurfaceTexture3dDescription, ByteBuffer>();
        legendStorage = new HashMap<SurfaceTexture3dDescription, ByteBuffer>();

        this.manager = manager;

        EMPTY_SURFACE_BUFFER = Buffers.newDirectByteBuffer(width * height * 4);
        EMPTY_LEGEND_BUFFER = Buffers.newDirectByteBuffer(1 * 500 * 4);
    }

    public synchronized ByteBuffer getSurfaceImage(int screenNumber) {
        if (screenNumber < 0 || screenNumber > oldScreenA.length - 1) {
            logger.error("Get request for screen number out of range: "
                    + screenNumber);
        }

        ByteBuffer result = null;
        if (newScreenA[screenNumber] != null) {
            SurfaceTexture3dDescription newDesc = newScreenA[screenNumber];

            if (surfaceStorage.containsKey(newDesc)) {
                result = surfaceStorage.get(newDesc);
            } else {
                result = surfaceStorage.get(oldScreenA[screenNumber]);
            }
        }

        if (result != null) {
            return result;
        } else {
            return EMPTY_SURFACE_BUFFER;
        }

    }

    public synchronized ByteBuffer getLegendImage(int screenNumber) {
        if (screenNumber < 0 || screenNumber > oldScreenA.length - 1) {
            logger.error("Get request for legend number out of range: "
                    + screenNumber);
        }

        ByteBuffer result = null;
        if (newScreenA[screenNumber] != null) {
            SurfaceTexture3dDescription newDesc = newScreenA[screenNumber];

            if (legendStorage.containsKey(newDesc)) {
                result = legendStorage.get(newDesc);
            } else {
                result = legendStorage.get(oldScreenA[screenNumber]);
            }
        }

        if (result != null) {
            return result;
        } else {
            return EMPTY_LEGEND_BUFFER;
        }
    }

    public synchronized void requestNewConfiguration(int screenNumber,
            SurfaceTexture3dDescription newDesc) {
        if (screenNumber < 0 || screenNumber > oldScreenA.length - 1) {
            logger.error("Configuration request for screen number out of range: "
                    + screenNumber);
        }

        SurfaceTexture3dDescription oldDesc = newScreenA[screenNumber];
        oldScreenA[screenNumber] = oldDesc;

        newScreenA[screenNumber] = newDesc;

        // Do some checking to see if the buffers are in sync
        if (surfaceStorage.containsValue(newDesc)
                && !legendStorage.containsValue(newDesc)) {
            surfaceStorage.remove(newDesc);
        }
        if (legendStorage.containsValue(newDesc)
                && !surfaceStorage.containsValue(newDesc)) {
            legendStorage.remove(newDesc);
        }

        // Check if there are textures in the storage that are unused, and
        // remove them if so
        ArrayList<SurfaceTexture3dDescription> usedDescs = new ArrayList<SurfaceTexture3dDescription>();
        for (int i = 0; i < oldScreenA.length; i++) {
            usedDescs.add(oldScreenA[i]);
            usedDescs.add(newScreenA[i]);
        }

        HashMap<SurfaceTexture3dDescription, ByteBuffer> newSurfaceStore = new HashMap<SurfaceTexture3dDescription, ByteBuffer>();
        for (SurfaceTexture3dDescription storedSurfaceDesc : surfaceStorage
                .keySet()) {
            if (usedDescs.contains(storedSurfaceDesc)) {
                newSurfaceStore.put(storedSurfaceDesc,
                        surfaceStorage.get(storedSurfaceDesc));
            }
        }
        surfaceStorage = newSurfaceStore;

        HashMap<SurfaceTexture3dDescription, ByteBuffer> newLegendStore = new HashMap<SurfaceTexture3dDescription, ByteBuffer>();
        for (SurfaceTexture3dDescription storedLegendSurfaceDesc : legendStorage
                .keySet()) {
            if (usedDescs.contains(storedLegendSurfaceDesc)) {
                newLegendStore.put(storedLegendSurfaceDesc,
                        legendStorage.get(storedLegendSurfaceDesc));
            }
        }
        legendStorage = newLegendStore;

        if (!surfaceStorage.containsValue(newDesc)
                && !legendStorage.containsValue(newDesc)) {
            manager.buildImages(newDesc);
        }
    }

    public synchronized void setSurfaceImage(SurfaceTexture3dDescription desc,
            ByteBuffer data) {
        boolean failure = true;

        // Only add this surface texture if it is still needed.
        for (int i = 0; i < newScreenA.length; i++) {
            if (newScreenA[i] == desc) {
                failure = false;
            }
        }

        if (!failure) {
            surfaceStorage.put(desc, data);
        }
    }

    public void setLegendImage(SurfaceTexture3dDescription desc, ByteBuffer data) {
        boolean failure = true;
        // Only add this legend texture if it is still needed.
        for (int i = 0; i < newScreenA.length; i++) {
            if (newScreenA[i] == desc) {
                failure = false;
            }
        }

        if (!failure) {
            legendStorage.put(desc, data);
        }
    }

    public boolean doneWithLastRequest() {
        boolean failure = false;

        for (SurfaceTexture3dDescription desc : newScreenA) {
            if (surfaceStorage.get(desc) == null
                    || legendStorage.get(desc) == null) {
                failure = true;
            }
        }

        return !failure;
    }
}
