package imau.visualization.adaptor;

import java.nio.FloatBuffer;

import openglCommon.textures.HDRTexture2D;

public class NetCDFTexture extends HDRTexture2D {
    public NetCDFTexture(int glMultitexUnit, FloatBuffer pixelBuffer,
            int width, int height) {
        super(glMultitexUnit);

        this.pixelBuffer = pixelBuffer;
        this.width = width;
        this.height = height;
    }
}
