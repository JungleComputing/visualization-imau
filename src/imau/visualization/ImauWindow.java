package imau.visualization;

import imau.visualization.adaptor.NetCDFTimedPlayer;

import java.io.File;
import java.io.FileNotFoundException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import openglCommon.CommonWindow;
import openglCommon.datastructures.Material;
import openglCommon.datastructures.Picture;
import openglCommon.exceptions.CompilationFailedException;
import openglCommon.exceptions.UninitializedException;
import openglCommon.math.Color4;
import openglCommon.math.MatF3;
import openglCommon.math.MatF4;
import openglCommon.math.MatrixFMath;
import openglCommon.math.Point4;
import openglCommon.math.VecF3;
import openglCommon.math.VecF4;
import openglCommon.models.GeoSphere;
import openglCommon.models.Model;
import openglCommon.models.base.Quad;
import openglCommon.shaders.Program;
import openglCommon.textures.Texture2D;
import util.ImauInputHandler;

public class ImauWindow extends CommonWindow {
    private final ImauSettings settings = ImauSettings.getInstance();

    private Quad fsq;
    private Program fsqProgram, texturedSphereProgram, atmProgram;

    private Model testModel, atmModel;

    public ImauWindow(ImauInputHandler inputHandler, boolean post_process) {
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

        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();

        final GL3 gl = drawable.getContext().getGL().getGL3();
        gl.glViewport(0, 0, width, height);

        displayContext();

        try {
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    private void displayContext() {
        NetCDFTimedPlayer timer = ImauPanel.getTimer();

        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();
        final GL3 gl = GLContext.getCurrentGL().getGL3();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        final float aspect = (float) width / (float) height;

        final MatF3 n = new MatF3();
        final MatF4 p = MatrixFMath.perspective(fovy, aspect, zNear, zFar);

        final Point4 eye = new Point4((float) (radius * Math.sin(ftheta) * Math.cos(phi)), (float) (radius
                * Math.sin(ftheta) * Math.sin(phi)), (float) (radius * Math.cos(ftheta)), 1.0f);
        final Point4 at = new Point4(0.0f, 0.0f, 0.0f, 1.0f);
        final VecF4 up = new VecF4(0.0f, 1.0f, 0.0f, 0.0f);

        MatF4 mv = MatrixFMath.lookAt(eye, at, up);
        mv = mv.mul(MatrixFMath.translate(new VecF3(0f, 0f, inputHandler.getViewDist())));
        mv = mv.mul(MatrixFMath.rotationX(inputHandler.getRotation().get(0)));
        mv = mv.mul(MatrixFMath.rotationY(inputHandler.getRotation().get(1)));

        loader.setUniformMatrix("NormalMatrix", MatrixFMath.getNormalMatrix(mv));
        loader.setUniformMatrix("PMatrix", p);
        loader.setUniformMatrix("SMatrix", MatrixFMath.scale(1));

        loader.setUniform("scrWidth", width);
        loader.setUniform("scrHeight", height);

        if (timer.isInitialized()) {
            Texture2D texture = timer.getFrame().getImage();
            try {
                texture.init(gl);
                texture.use(gl);
                loader.setUniform("my_texture", texture.getMultitexNumber());
                fsqProgram.setUniformMatrix("PMatrix", new MatF4());

                texturedSphereProgram.setUniformVector("LightPos", new VecF3(100f, 100f, 0f));
                texturedSphereProgram.setUniform("Shininess", 100f);
                texturedSphereProgram.setUniformVector("lDiffuse", new VecF4(1, 1, 1, 1));
                texturedSphereProgram.setUniformVector("lAmbient", new VecF4(1, 1, 1, 1));
                texturedSphereProgram.setUniformVector("lSpecular", new VecF4(1, 1, 1, 1));

                // testModel.draw(gl, texturedSphereProgram, mv);

                // atmProgram.setUniformMatrix("NormalMatrix", new MatF3());
                atmModel.draw(gl, atmProgram, mv);

                // fsq.draw(gl, fsqProgram, new MatF4());

            } catch (UninitializedException e1) {
                e1.printStackTrace();
            }
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

        loader.cleanup(gl);
        testModel.delete(gl);
        atmModel.delete(gl);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        fsq = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        fsq.init(gl);

        testModel = new GeoSphere(Material.random(), 60, 90, 50f, false);
        testModel.init(gl);

        Color4 atmosphereColor = new Color4(0.0f, 1.0f, 1.0f, 0.005f);
        atmModel = new GeoSphere(new Material(atmosphereColor, atmosphereColor, atmosphereColor), 60, 90, 55f, false);
        atmModel.init(gl);

        inputHandler.setViewDist(-130f);

        try {
            fsqProgram = loader.createProgram(gl, "fsqProgram", new File("shaders/vs_texture.vp"), new File(
                    "shaders/fs_texture.fp"));

            texturedSphereProgram = loader.createProgram(gl, "texturedSphereProgram",
                    new File("shaders/vs_pplTex2.vp"), new File("shaders/fs_pplTex2.fp"));

            atmProgram = loader.createProgram(gl, "atmProgram", new File("shaders/vs_atmosphere.vp"), new File(
                    "shaders/fs_atmosphere.fp"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }
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
        displayContext();

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
