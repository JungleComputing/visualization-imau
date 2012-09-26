package imau.visualization.adaptor;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import com.jogamp.common.nio.Buffers;

public class IntRBOTexture extends IntTexture2D {

    public IntRBOTexture(int width, int height, int glMultiTexUnit) {
        super(glMultiTexUnit);
        this.height = height;
        this.width = width;
    }

    @Override
    public void init(GL3 gl) {
        gl.glActiveTexture(glMultiTexUnit);
        gl.glEnable(GL3.GL_TEXTURE_2D);

        // Create new texture pointer and bind it so we can manipulate it.
        pointer = Buffers.newDirectIntBuffer(1);
        gl.glGenTextures(1, pointer);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, pointer.get(0));

        int[] backingArray = new int[width * height];
        pixelBuffer = IntBuffer.wrap(backingArray);

        // Wrap and mipmap parameters.
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER,
                GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER,
                GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S,
                GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T,
                GL3.GL_CLAMP_TO_EDGE);

        gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, // Mipmap level.
                GL3.GL_RGBA, // GL.GL_RGBA, // Internal Texel Format,
                width, height, 0, // Border
                GL3.GL_RGBA, // External format from image,
                GL3.GL_UNSIGNED_BYTE, null // Imagedata as ByteBuffer
        );

        // Unbind, now ready for use
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

        initialized = true;
    }
}
