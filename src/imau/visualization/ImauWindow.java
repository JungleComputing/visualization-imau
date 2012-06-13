package imau.visualization;

import imau.visualization.adaptor.BumpTexture;
import imau.visualization.adaptor.NetCDFFrame;
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
import openglCommon.datastructures.FBO;
import openglCommon.datastructures.Material;
import openglCommon.datastructures.Picture;
import openglCommon.exceptions.CompilationFailedException;
import openglCommon.exceptions.UninitializedException;
import openglCommon.math.Color4;
import openglCommon.math.MatF4;
import openglCommon.math.MatrixFMath;
import openglCommon.math.Point4;
import openglCommon.math.VecF3;
import openglCommon.math.VecF4;
import openglCommon.models.GeoSphere;
import openglCommon.models.Model;
import openglCommon.models.base.Quad;
import openglCommon.models.base.Sphere;
import openglCommon.shaders.Program;
import openglCommon.textures.Texture2D;
import util.ImauInputHandler;

public class ImauWindow extends CommonWindow {
    private final ImauSettings settings = ImauSettings.getInstance();

    private Quad fsq;
    private Program fsqProgram, texturedSphereProgram, atmProgram, gaussianBlurShader, postprocessShader;
    // private Texture2D worldTex;

    BumpTexture bumpTex;

    private Model testModel, atmModel;

    private FBO sphereFBO00, sphereFBO01, sphereFBO10, sphereFBO11, atmFBO;

    private NetCDFFrame currentFrame;

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

        // final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        // final int height =
        // GLContext.getCurrent().getGLDrawable().getHeight();

        final GL3 gl = drawable.getContext().getGL().getGL3();
        gl.glViewport(0, 0, canvasWidth, canvasHeight);

        NetCDFTimedPlayer timer = ImauPanel.getTimer();
        if (timer.isInitialized()) {
            currentFrame = timer.getFrame();
            displayContext(currentFrame, sphereFBO00, sphereFBO01, sphereFBO10, sphereFBO11, atmFBO);
        }

        try {
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    private void displayContext(NetCDFFrame frame, FBO sphereFBO00, FBO sphereFBO01, FBO sphereFBO10, FBO sphereFBO11,
            FBO atmFBO) {
        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();
        final float aspect = (float) width / (float) height;

        final GL3 gl = GLContext.getCurrentGL().getGL3();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

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
        // loader.setUniformMatrix("SMatrix", MatrixFMath.scale(1));

        // DataTexture dataTex = timer.getFrame().getTexCoords();

        // dataTex.init(gl);
        // bumpTex.use(gl);
        // worldTex.use(gl);

        // loader.setUniform("world_bump", bumpTex.getMultitexNumber());
        // loader.setUniform("world_texture",
        // worldTex.getMultitexNumber());
        // loader.setUniform("mapping_texture",
        // dataTex.getMultitexNumber());
        // loader.setUniform("my_texture", texture.getMultitexNumber());

        settings.getBandComboLT();
        Texture2D texture00 = frame.getImage2(GL3.GL_TEXTURE11, settings.getBandComboLB());
        Texture2D texture01 = frame.getImage2(GL3.GL_TEXTURE12, settings.getBandComboRB());
        Texture2D texture10 = frame.getImage2(GL3.GL_TEXTURE13, settings.getBandComboLT());
        Texture2D texture11 = frame.getImage2(GL3.GL_TEXTURE14, settings.getBandComboRT());
        texture00.init(gl);
        texture01.init(gl);
        texture10.init(gl);
        texture11.init(gl);

        drawSphere(gl, mv, texture00, texturedSphereProgram, sphereFBO00);
        drawSphere(gl, mv, texture01, texturedSphereProgram, sphereFBO01);
        drawSphere(gl, mv, texture10, texturedSphereProgram, sphereFBO10);
        drawSphere(gl, mv, texture11, texturedSphereProgram, sphereFBO11);

        drawAtmosphere(gl, mv, atmProgram, atmFBO);

        if (post_process) {
            renderTexturesToScreen(gl, width, height, sphereFBO00, sphereFBO01, sphereFBO10, sphereFBO11, atmFBO);
        }

        // fsqProgram.setUniform("scrWidth", width);
        // fsqProgram.setUniform("scrHeight", height);
        // fsqProgram.setUniformMatrix("PMatrix", new MatF4());
        // fsq.draw(gl, fsqProgram, new MatF4());

    }

    private void drawSphere(GL3 gl, MatF4 mv, Texture2D texture, Program program, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            program.setUniform("texture_map", texture.getMultitexNumber());
            program.setUniformVector("LightPos", new VecF3(100f, 100f, 0f));
            program.setUniform("Shininess", 100f);
            program.setUniformVector("lDiffuse", new VecF4(1, 1, 1, 1));
            program.setUniformVector("lAmbient", new VecF4(1, 1, 1, 1));

            testModel.draw(gl, program, mv);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawAtmosphere(GL3 gl, MatF4 mv, Program program, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            program.setUniformMatrix("NormalMatrix", MatrixFMath.getNormalMatrix(mv));
            atmModel.draw(gl, program, mv);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void renderTexturesToScreen(GL3 gl, int width, int height, FBO sphereFBO00, FBO sphereFBO01,
            FBO sphereFBO10, FBO sphereFBO11, FBO atmFBO) {
        postprocessShader.setUniform("sphereTexture00", sphereFBO00.getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTexture01", sphereFBO01.getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTexture10", sphereFBO10.getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTexture11", sphereFBO11.getTexture().getMultitexNumber());

        postprocessShader.setUniform("atmTexture", atmFBO.getTexture().getMultitexNumber());

        postprocessShader.setUniform("sphereBrightness", 1f);
        postprocessShader.setUniform("atmBrightness", 1f);

        postprocessShader.setUniformMatrix("MVMatrix", new MatF4());
        postprocessShader.setUniformMatrix("PMatrix", new MatF4());

        postprocessShader.setUniform("scrWidth", width);
        postprocessShader.setUniform("scrHeight", height);

        int selection = settings.getWindowSelection();
        // System.out.println("Selection: " + selection);
        if (selection == 0) {
            postprocessShader.setUniform("divs", 2);
        } else {
            postprocessShader.setUniform("divs", 1);
        }

        postprocessShader.setUniform("selection", selection);

        // postprocessShader.setUniform("amountY", 2);

        try {
            postprocessShader.use(gl);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            fsq.draw(gl, postprocessShader, new MatF4());
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void blur(GL3 gl, FBO target, Quad fullScreenQuad, int passes, int blurType, float blurSize) {
        gaussianBlurShader.setUniform("Texture", target.getTexture().getMultitexNumber());

        gaussianBlurShader.setUniformMatrix("PMatrix", new MatF4());
        gaussianBlurShader.setUniformMatrix("MVMatrix", new MatF4());

        gaussianBlurShader.setUniform("blurType", blurType);
        gaussianBlurShader.setUniform("blurSize", blurSize);
        gaussianBlurShader.setUniform("scrWidth", target.getTexture().getWidth());
        gaussianBlurShader.setUniform("scrHeight", target.getTexture().getHeight());
        gaussianBlurShader.setUniform("Alpha", 1f);

        gaussianBlurShader.setUniform("blurDirection", 0);

        try {
            // gaussianBlurShader.use(gl);

            for (int i = 0; i < passes; i++) {
                target.bind(gl);

                gaussianBlurShader.setUniform("blurDirection", 0);
                fullScreenQuad.draw(gl, gaussianBlurShader, new MatF4());

                gaussianBlurShader.setUniform("blurDirection", 1);
                fullScreenQuad.draw(gl, gaussianBlurShader, new MatF4());
                target.unBind(gl);
            }
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderTexturesToScreen(GL3 arg0, int arg1, int arg2) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);
        final GL3 gl = drawable.getGL().getGL3();

        sphereFBO00.delete(gl);
        sphereFBO01.delete(gl);
        sphereFBO10.delete(gl);
        sphereFBO11.delete(gl);

        atmFBO.delete(gl);

        sphereFBO00 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);
        sphereFBO01 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        sphereFBO10 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereFBO11 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        atmFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE8);

        sphereFBO00.init(gl);
        sphereFBO01.init(gl);
        sphereFBO10.init(gl);
        sphereFBO11.init(gl);

        atmFBO.init(gl);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();

        loader.cleanup(gl);
        testModel.delete(gl);
        atmModel.delete(gl);

        sphereFBO00.delete(gl);
        sphereFBO01.delete(gl);
        sphereFBO10.delete(gl);
        sphereFBO11.delete(gl);

        atmFBO.delete(gl);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        sphereFBO00 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);
        sphereFBO01 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        sphereFBO10 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereFBO11 = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        atmFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE8);

        sphereFBO00.init(gl);
        sphereFBO01.init(gl);
        sphereFBO10.init(gl);
        sphereFBO11.init(gl);

        atmFBO.init(gl);

        fsq = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        fsq.init(gl);

        testModel = new GeoSphere(Material.random(), 60, 90, 50f, false);
        testModel.init(gl);

        Color4 atmosphereColor = new Color4(0.0f, 1.0f, 1.0f, 0.005f);

        atmModel = new Sphere(new Material(atmosphereColor, atmosphereColor, atmosphereColor), 5, 53f, new VecF3(),
                false);

        // worldTex = new ImageTexture("textures/4_no_ice_clouds_mts_8k.jpg",
        // 4096, 0, GL3.GL_TEXTURE2);
        // bumpTex = new BumpTexture("textures/elev_bump_8k.jpg", 4096, 0,
        // GL3.GL_TEXTURE3);
        // worldTex.init(gl);
        // bumpTex.init(gl);

        // atmModel = new GeoSphere(new Material(atmosphereColor,
        // atmosphereColor, atmosphereColor), 50, 50, 55f, false);

        atmModel.init(gl);

        inputHandler.setViewDist(-130f);

        try {
            fsqProgram = loader.createProgram(gl, "fsqProgram", new File("shaders/vs_texture.vp"), new File(
                    "shaders/fs_texture.fp"));

            texturedSphereProgram = loader.createProgram(gl, "texturedSphereProgram",
                    new File("shaders/vs_pplTex2.vp"), new File("shaders/fs_pplTex2.fp"));

            atmProgram = loader.createProgram(gl, "atmProgram", new File("shaders/vs_atmosphere.vp"), new File(
                    "shaders/fs_atmosphere.fp"));

            gaussianBlurShader = loader.createProgram(gl, "gaussianBlur", new File("shaders/vs_postprocess.vp"),
                    new File("shaders/fs_gaussian_blur.fp"));

            postprocessShader = loader.createProgram(gl, "postprocess", new File("shaders/vs_postprocess.vp"),
                    new File("shaders/fs_eSalsaPostprocess.fp"));
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

        displayContext(currentFrame, sphereFBO00, sphereFBO01, sphereFBO10, sphereFBO11, atmFBO);

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
    }
}
