package nl.esciencecenter.visualization.esalsa.glExt;

import nl.esciencecenter.visualization.openglCommon.exceptions.UninitializedException;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;

public class BoundingBox {
    private boolean initialized = false;
    private float minX, minY, minZ;
    private float maxX, maxY, maxZ;

    public BoundingBox() {
        initialized = false;
    }

    public void reset() {
        initialized = false;
    }

    public void resize(VecF3 newEntry) {
        if (initialized) {
            minX = Math.min(minX, newEntry.get(0));
            minY = Math.min(minY, newEntry.get(1));
            minZ = Math.min(minZ, newEntry.get(2));

            maxX = Math.max(maxX, newEntry.get(0));
            maxY = Math.max(maxY, newEntry.get(1));
            maxZ = Math.max(maxZ, newEntry.get(2));
        } else {
            minX = newEntry.get(0);
            minY = newEntry.get(1);
            minZ = newEntry.get(2);

            maxX = newEntry.get(0);
            maxY = newEntry.get(1);
            maxZ = newEntry.get(2);
        }

        initialized = true;
    }

    public VecF3 getMin() throws UninitializedException {
        if (!initialized)
            throw new UninitializedException("BoundingBox not initialized.");
        return new VecF3(minX, minY, minZ);
    }

    public VecF3 getMax() throws UninitializedException {
        if (!initialized)
            throw new UninitializedException("BoundingBox not initialized.");
        return new VecF3(maxX, maxY, maxZ);
    }

    public float getHeight() {
        return maxY - minY;
    }

    public float getWidth() {
        return maxX - minX;
    }

    public float getDepth() {
        return maxZ - minZ;
    }

    public VecF3 getCenter() {
        float x = maxX - (0.5f * getWidth());
        float y = maxY - (0.5f * getHeight());
        float z = maxZ - (0.5f * getDepth());

        return new VecF3(x, y, z);
    }
}
