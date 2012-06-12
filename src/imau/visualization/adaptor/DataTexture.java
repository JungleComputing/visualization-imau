package imau.visualization.adaptor;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import openglCommon.exceptions.UninitializedException;
import openglCommon.textures.Texture;

import com.jogamp.common.nio.Buffers;

public class DataTexture extends Texture {
    protected FloatBuffer pixelBuffer;
    protected int width, height;
    protected IntBuffer pointer;

    private boolean initialized = false;

    public DataTexture(int gLMultiTexUnit, FloatBuffer pixelBuffer, int width, int height) {
        super(gLMultiTexUnit);
        this.pixelBuffer = pixelBuffer;
        this.width = width;
        this.height = height;
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
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);

            // Specifies the alignment requirements for the start of each pixel
            // row in memory.
            gl.glPixelStorei(GL3.GL_UNPACK_ALIGNMENT, 1);

            gl.glTexImage2D(GL3.GL_TEXTURE_2D, 0, // Mipmap level.
                    GL3.GL_ALPHA, // GL.GL_RGBA, // Internal Texel Format,
                    width, height, 0, // Border
                    GL3.GL_ALPHA, // External format from image,
                    GL3.GL_FLOAT, pixelBuffer // Imagedata as ByteBuffer
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

    public FloatBuffer getPixelBuffer() {
        return pixelBuffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPointer() throws UninitializedException {
        if (pointer == null)
            throw new UninitializedException();
        return pointer.get(0);
    }

}
