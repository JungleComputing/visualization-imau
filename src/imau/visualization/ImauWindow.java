package imau.visualization;

import javax.media.opengl.GL;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import openglCommon.CommonWindow;
import openglCommon.datastructures.Picture;
import openglCommon.math.MatF4;
import openglCommon.util.InputHandler;

public class ImauWindow extends CommonWindow {
    private final ImauSettings settings = ImauSettings.getInstance();

    public ImauWindow(InputHandler inputHandler, boolean post_process) {
        super(inputHandler, post_process);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        try {
            final int status = drawable.getContext().makeCurrent();
            if ((status != GLContext.CONTEXT_CURRENT) && (status != GLContext.CONTEXT_CURRENT_NEW)) {
                System.err.println("Error swapping context to onscreen.");
            }
        } catch (final GLException e) {
            System.err.println("Exception while swapping context to onscreen.");
            e.printStackTrace();
        }

        final GL3 gl = drawable.getContext().getGL().getGL3();
        gl.glViewport(0, 0, canvasWidth, canvasHeight);

        try {
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderTexturesToScreen(GL3 arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);
        final GL3 gl = drawable.getGL().getGL3();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();
    }

    @Override
    public void makeSnapshot(String fileName) {
        try {
            final int status = offScreenContext.makeCurrent();
            if ((status != GLContext.CONTEXT_CURRENT) && (status != GLContext.CONTEXT_CURRENT_NEW)) {
                System.err.println("Error swapping context to offscreen.");
            }
        } catch (final GLException e) {
            System.err.println("Exception while swapping context to offscreen.");
            e.printStackTrace();
        }

        final int width = offScreenContext.getGLDrawable().getWidth();
        final int height = offScreenContext.getGLDrawable().getHeight();

        final GL3 gl = offScreenContext.getGL().getGL3();
        gl.glViewport(0, 0, width, height);

        // Anti-Aliasing
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        gl.glEnable(GL2GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);

        // Depth testing
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glClearDepth(1.0f);

        // Culling
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);

        // Enable Blending (needed for both Transparency and
        // Anti-Aliasing
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_BLEND);

        gl.glClearColor(0f, 0f, 0f, 0f);

        // TODO Draw Context

        final Picture p = new Picture(width, height);

        gl.glFinish();

        p.copyFrameBufferToFile(settings.getScreenshotPath(), fileName);

        try {
            offScreenContext.release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderScene(GL3 arg0, MatF4 arg1) {
        // TODO Auto-generated method stub

    }
}
