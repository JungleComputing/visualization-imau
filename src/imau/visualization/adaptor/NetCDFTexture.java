package imau.visualization.adaptor;

import java.nio.ByteBuffer;

import openglCommon.textures.Texture2D;

public class NetCDFTexture extends Texture2D {
    public NetCDFTexture(int glMultitexUnit, ByteBuffer pixelBuffer, int width, int height) {
        super(glMultitexUnit);

        this.pixelBuffer = pixelBuffer;
        this.width = width;
        this.height = height;
    }
}
