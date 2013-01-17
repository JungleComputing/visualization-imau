package nl.esciencecenter.visualization.esalsa.glExt;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import nl.esciencecenter.visualization.openglCommon.exceptions.UninitializedException;
import nl.esciencecenter.visualization.openglCommon.textures.Texture;

import com.jogamp.common.nio.Buffers;

public class Texture2D extends Texture {

    protected ByteBuffer pixelBuffer;
    protected int        width, height;
    protected IntBuffer  pointer;

    protected boolean    initialized = false;

    public Texture2D(int glMultitexUnit) {
        super(glMultitexUnit);
    }

    public void init(GL3 gl) {
        if (!initialized) {
            if (pixelBuffer == null) {
                System.err
                        .println("Add a pixelbuffer first, by using a custom constructor. The Texture2D constructor is only meant to be extended.");
            }

            // Tell OpenGL we want to use textures
            gl.glEnable(GL3.GL_TEXTURE_2D);
            gl.glActiveTexture(this.glMultiTexUnit);

            // Create a Texture Object
            pointer = Buffers.newDirectIntBuffer(1);
            gl.glGenTextures(1, pointer);

            // Tell OpenGL that this texture is 2D and we want to use it
            gl.glBindTexture(GL3.GL_TEXTURE_2D, pointer.get(0));

            // Wrap.
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S,
                    GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T,
                    GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER,
                    GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER,
                    GL3.GL_LINEAR);

            // Specifies the alignment requirements for the start of each pixel
            // row in memory.
            gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);

            gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, // Mipmap level.
                    GL3.GL_RGBA32F, // GL.GL_RGBA, // Internal Texel Format,
                    width, height, 0, // Border
                    GL3.GL_RGBA, // External format from image,
                    GL3.GL_UNSIGNED_BYTE, pixelBuffer // Imagedata as ByteBuffer
            );

            initialized = true;
        }
    }

    public void delete(GL3 gl) {
        gl.glDeleteTextures(1, pointer);
    }

    public void use(GL3 gl) throws UninitializedException {
        if (!initialized) {
            init(gl);
        }

        gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glActiveTexture(glMultiTexUnit);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, getPointer());
    }

    public void unBind(GL3 gl) {
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
    }

    public ByteBuffer getPixelBuffer() {
        return pixelBuffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture2D copy(GL3 gl, int glMultitexUnit) {
        Texture2D result = new Texture2D(glMultitexUnit);
        result.pixelBuffer = pixelBuffer.duplicate();
        result.init(gl);

        return result;
    }

    public int getPointer() throws UninitializedException {
        if (pointer == null)
            throw new UninitializedException();
        return pointer.get(0);
    }

}