package imau.visualization;

import imau.visualization.adaptor.GlobeState;
import imau.visualization.adaptor.NetCDFFrame;
import imau.visualization.adaptor.NetCDFTimedPlayer;
import imau.visualization.jni.SageInterface;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import openglCommon.CommonWindow;
import openglCommon.datastructures.HDRFBO;
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
import openglCommon.textures.HDRTexture2D;
import util.ImauInputHandler;

import com.jogamp.opengl.util.awt.Screenshot;

public class ImauWindow extends CommonWindow {
    private final ImauSettings settings     = ImauSettings.getInstance();

    private Quad               fsq;
    private Program            fsqProgram, texturedSphereProgram, atmProgram,
            gaussianBlurShader, postprocessShader;
    // private Texture2D worldTex;

    private Model              testModel, atmModel;

    private HDRFBO             sphereHDRFBO00, sphereHDRFBO01, sphereHDRFBO10,
            sphereHDRFBO11, atmHDRFBO;

    private NetCDFFrame        currentFrame1, currentFrame2;

    private BufferedImage      currentImage = null;

    private SageInterface      sage;

    public ImauWindow(ImauInputHandler inputHandler, boolean post_process) {
        super(inputHandler, post_process);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        try {
            final int status = drawable.getContext().makeCurrent();
            if ((status != GLContext.CONTEXT_CURRENT)
                    && (status != GLContext.CONTEXT_CURRENT_NEW)) {
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
            currentFrame1 = timer.getFrame();

            if (!timer.isTwoSourced()) {
                currentFrame2 = null;
                displayContext(currentFrame1, null, sphereHDRFBO00,
                        sphereHDRFBO01, sphereHDRFBO10, sphereHDRFBO11,
                        atmHDRFBO);
            } else {
                currentFrame2 = timer.getFrame2();
                displayContext(currentFrame1, currentFrame2, sphereHDRFBO00,
                        sphereHDRFBO01, sphereHDRFBO10, sphereHDRFBO11,
                        atmHDRFBO);
            }
        }

        try {
            if (settings.isIMAGE_STREAM_OUTPUT()) {
                currentImage = Screenshot.readToBufferedImage(canvasWidth,
                        canvasHeight);

                int[] rgb = knitImages();
                sage.display(rgb);
            }
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    private void displayContext(NetCDFFrame frame1, NetCDFFrame frame2,
            HDRFBO sphereHDRFBOLT, HDRFBO sphereHDRFBORT,
            HDRFBO sphereHDRFBOLB, HDRFBO sphereHDRFBORB, HDRFBO atmHDRFBO) {
        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();
        final float aspect = (float) width / (float) height;

        final GL3 gl = GLContext.getCurrentGL().getGL3();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        final MatF4 p = MatrixFMath.perspective(fovy, aspect, zNear, zFar);

        final Point4 eye = new Point4(
                (float) (radius * Math.sin(ftheta) * Math.cos(phi)),
                (float) (radius * Math.sin(ftheta) * Math.sin(phi)),
                (float) (radius * Math.cos(ftheta)), 1.0f);
        final Point4 at = new Point4(0.0f, 0.0f, 0.0f, 1.0f);
        final VecF4 up = new VecF4(0.0f, 1.0f, 0.0f, 0.0f);

        MatF4 mv = MatrixFMath.lookAt(eye, at, up);
        mv = mv.mul(MatrixFMath.translate(new VecF3(0f, 0f, inputHandler
                .getViewDist())));
        mv = mv.mul(MatrixFMath.rotationX(inputHandler.getRotation().get(0)));
        mv = mv.mul(MatrixFMath.rotationY(inputHandler.getRotation().get(1)));

        loader.setUniformMatrix("NormalMatrix", MatrixFMath.getNormalMatrix(mv));
        loader.setUniformMatrix("PMatrix", p);

        HDRTexture2D textureLT = null, legendLT = null;
        HDRTexture2D textureRT = null;
        HDRTexture2D textureLB = null;
        HDRTexture2D textureRB = null;

        if (frame1 != null && frame2 != null) {
            if (settings.getLTState().getDataMode() == GlobeState.DataMode.DIFF) {
                textureLT = frame1.getImage(gl, frame2, GL3.GL_TEXTURE11,
                        settings.getLTState());
            }
            if (settings.getRTState().getDataMode() == GlobeState.DataMode.DIFF) {
                textureRT = frame1.getImage(gl, frame2, GL3.GL_TEXTURE12,
                        settings.getRTState());
            }
            if (settings.getLBState().getDataMode() == GlobeState.DataMode.DIFF) {
                textureLB = frame1.getImage(gl, frame2, GL3.GL_TEXTURE13,
                        settings.getLBState());
            }
            if (settings.getRBState().getDataMode() == GlobeState.DataMode.DIFF) {
                textureRB = frame1.getImage(gl, frame2, GL3.GL_TEXTURE14,
                        settings.getRBState());
            }
        }

        if (frame2 != null) {
            if (settings.getLTState().getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
                textureLT = frame2.getImage(gl, GL3.GL_TEXTURE11,
                        settings.getLTState());
            } else {

            }
            if (settings.getRTState().getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
                textureRT = frame2.getImage(gl, GL3.GL_TEXTURE12,
                        settings.getRTState());
            }
            if (settings.getLBState().getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
                textureLB = frame2.getImage(gl, GL3.GL_TEXTURE13,
                        settings.getLBState());
            }
            if (settings.getRBState().getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
                textureRB = frame2.getImage(gl, GL3.GL_TEXTURE14,
                        settings.getRBState());
            }
        }

        if (frame1 != null) {
            if (textureLT == null
                    || settings.getLTState().getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
                textureLT = frame1.getImage(gl, GL3.GL_TEXTURE11,
                        settings.getLTState());
                // legendLT = frame1.get
            }
            if (textureRT == null
                    || settings.getRTState().getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
                textureRT = frame1.getImage(gl, GL3.GL_TEXTURE12,
                        settings.getRTState());
            }
            if (textureLB == null
                    || settings.getLBState().getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
                textureLB = frame1.getImage(gl, GL3.GL_TEXTURE13,
                        settings.getLBState());
            }
            if (textureRB == null
                    || settings.getRBState().getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
                textureRB = frame1.getImage(gl, GL3.GL_TEXTURE14,
                        settings.getRBState());
            }
        }

        if (textureLT != null && textureRT != null && textureLB != null
                && textureRB != null) {
            textureLT.init(gl);
            textureRT.init(gl);
            textureLB.init(gl);
            textureRB.init(gl);

            drawSphere(gl, mv, textureLT, texturedSphereProgram, sphereHDRFBOLT);
            drawSphere(gl, mv, textureRT, texturedSphereProgram, sphereHDRFBORT);
            drawSphere(gl, mv, textureLB, texturedSphereProgram, sphereHDRFBOLB);
            drawSphere(gl, mv, textureRB, texturedSphereProgram, sphereHDRFBORB);

            drawAtmosphere(gl, mv, atmProgram, atmHDRFBO);

            blur(gl, atmHDRFBO, fsq, 1, 2, 4);

            if (post_process) {
                renderTexturesToScreen(gl, width, height, sphereHDRFBOLT,
                        sphereHDRFBORT, sphereHDRFBOLB, sphereHDRFBORB,
                        atmHDRFBO);
            }
        }
    }

    private void drawSphere(GL3 gl, MatF4 mv, HDRTexture2D texture,
            Program program, HDRFBO target) {
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

    private void drawAtmosphere(GL3 gl, MatF4 mv, Program program, HDRFBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            program.setUniformMatrix("NormalMatrix",
                    MatrixFMath.getNormalMatrix(mv));
            atmModel.draw(gl, program, mv);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void renderTexturesToScreen(GL3 gl, int width, int height,
            HDRFBO sphereHDRFBOLT, HDRFBO sphereHDRFBORT,
            HDRFBO sphereHDRFBOLB, HDRFBO sphereHDRFBORB, HDRFBO atmHDRFBO) {
        postprocessShader.setUniform("sphereTextureLT", sphereHDRFBOLT
                .getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTextureRT", sphereHDRFBORT
                .getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTextureLB", sphereHDRFBOLB
                .getTexture().getMultitexNumber());
        postprocessShader.setUniform("sphereTextureRB", sphereHDRFBORB
                .getTexture().getMultitexNumber());

        postprocessShader.setUniform("atmTexture", atmHDRFBO.getTexture()
                .getMultitexNumber());

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

    private void blur(GL3 gl, HDRFBO target, Quad fullScreenQuad, int passes,
            int blurType, float blurSize) {
        gaussianBlurShader.setUniform("Texture", target.getTexture()
                .getMultitexNumber());

        gaussianBlurShader.setUniformMatrix("PMatrix", new MatF4());
        gaussianBlurShader.setUniformMatrix("MVMatrix", new MatF4());

        gaussianBlurShader.setUniform("blurType", blurType);
        gaussianBlurShader.setUniform("blurSize", blurSize);
        gaussianBlurShader.setUniform("scrWidth", target.getTexture()
                .getWidth());
        gaussianBlurShader.setUniform("scrHeight", target.getTexture()
                .getHeight());
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

        sphereHDRFBO00.delete(gl);
        sphereHDRFBO01.delete(gl);
        sphereHDRFBO10.delete(gl);
        sphereHDRFBO11.delete(gl);

        atmHDRFBO.delete(gl);

        sphereHDRFBO00 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);
        sphereHDRFBO01 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        sphereHDRFBO10 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereHDRFBO11 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        atmHDRFBO = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE8);

        sphereHDRFBO00.init(gl);
        sphereHDRFBO01.init(gl);
        sphereHDRFBO10.init(gl);
        sphereHDRFBO11.init(gl);

        atmHDRFBO.init(gl);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();

        loader.cleanup(gl);
        testModel.delete(gl);
        atmModel.delete(gl);

        sphereHDRFBO00.delete(gl);
        sphereHDRFBO01.delete(gl);
        sphereHDRFBO10.delete(gl);
        sphereHDRFBO11.delete(gl);

        atmHDRFBO.delete(gl);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        sphereHDRFBO00 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);
        sphereHDRFBO01 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        sphereHDRFBO10 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereHDRFBO11 = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        atmHDRFBO = new HDRFBO(canvasWidth, canvasHeight, GL.GL_TEXTURE8);

        sphereHDRFBO00.init(gl);
        sphereHDRFBO01.init(gl);
        sphereHDRFBO10.init(gl);
        sphereHDRFBO11.init(gl);

        atmHDRFBO.init(gl);

        fsq = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        fsq.init(gl);

        testModel = new GeoSphere(Material.random(), 60, 90, 50f, false);
        testModel.init(gl);

        Color4 atmosphereColor = new Color4(0.0f, 1.0f, 1.0f, 0.005f);

        atmModel = new Sphere(new Material(atmosphereColor, atmosphereColor,
                atmosphereColor), 5, 53f, new VecF3(), false);

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
            fsqProgram = loader
                    .createProgram(gl, "fsqProgram", new File(
                            "shaders/vs_texture.vp"), new File(
                            "shaders/fs_texture.fp"));

            texturedSphereProgram = loader.createProgram(gl,
                    "texturedSphereProgram", new File("shaders/vs_pplTex2.vp"),
                    new File("shaders/fs_pplTex2.fp"));

            atmProgram = loader.createProgram(gl, "atmProgram", new File(
                    "shaders/vs_atmosphere.vp"), new File(
                    "shaders/fs_atmosphere.fp"));

            gaussianBlurShader = loader.createProgram(gl, "gaussianBlur",
                    new File("shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_gaussian_blur.fp"));

            postprocessShader = loader.createProgram(gl, "postprocess",
                    new File("shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_eSalsaPostprocess.fp"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }

        if (settings.isIMAGE_STREAM_OUTPUT()) {
            sage = new SageInterface(canvasWidth, canvasHeight, 10);
        }
    }

    @Override
    public void makeSnapshot(String fileName) {
        try {
            final int status = offScreenContext.makeCurrent();
            if ((status != GLContext.CONTEXT_CURRENT)
                    && (status != GLContext.CONTEXT_CURRENT_NEW)) {
                System.err.println("Error swapping context to offscreen.");
            }
        } catch (final GLException e) {
            System.err
                    .println("Exception while swapping context to offscreen.");
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

        displayContext(currentFrame1, currentFrame2, sphereHDRFBO00,
                sphereHDRFBO01, sphereHDRFBO10, sphereHDRFBO11, atmHDRFBO);

        final Picture p = new Picture(width, height);

        gl.glFinish();

        p.copyFrameBufferToFile(settings.getScreenshotPath(), fileName);

        try {
            offScreenContext.release();
        } catch (final GLException e) {
            e.printStackTrace();
        }
    }

    private int[] knitImages() {
        BufferedImage frame = ImauApp.getFrameImage();
        Point p = ImauApp.getCanvaslocation();

        int[] rgb = new int[frame.getWidth() * frame.getHeight()];

        for (int y = 0; y < frame.getHeight(); y++) {
            int glCanvasY = y - p.y;

            if (glCanvasY >= 0 && glCanvasY < currentImage.getHeight()) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int glCanvasX = x - p.x;

                    if (glCanvasX >= 0 && glCanvasX < currentImage.getWidth()) {
                        rgb[y * frame.getWidth() + x] = currentImage.getRGB(
                                glCanvasX, glCanvasY);
                    } else {
                        rgb[y * frame.getWidth() + x] = frame.getRGB(x, y);
                    }
                }
            } else {
                for (int x = 0; x < frame.getWidth(); x++) {
                    rgb[y * frame.getWidth() + x] = frame.getRGB(x, y);
                }
            }
        }

        return rgb;
    }

    private BufferedImage produceBufferedImage(int[] input, int width,
            int height) {
        BufferedImage result = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);

        result.setRGB(0, 0, width, height, input, 0, width);

        return result;
    }

    public BufferedImage getScreenshot() {
        BufferedImage frame = ImauApp.getFrameImage();
        Point p = ImauApp.getCanvaslocation();

        int[] rgb = new int[frame.getWidth() * frame.getHeight()];

        for (int y = 0; y < frame.getHeight(); y++) {
            int glCanvasY = y - p.y;

            if (glCanvasY >= 0 && glCanvasY < currentImage.getHeight()) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int glCanvasX = x - p.x;

                    if (glCanvasX >= 0 && glCanvasX < currentImage.getWidth()) {
                        rgb[y * frame.getWidth() + x] = currentImage.getRGB(
                                glCanvasX, glCanvasY);
                    } else {
                        rgb[y * frame.getWidth() + x] = frame.getRGB(x, y);
                    }
                }
            } else {
                for (int x = 0; x < frame.getWidth(); x++) {
                    rgb[y * frame.getWidth() + x] = frame.getRGB(x, y);
                }
            }
        }

        BufferedImage result = new BufferedImage(frame.getWidth(),
                frame.getHeight(), BufferedImage.TYPE_INT_RGB);

        result.setRGB(0, 0, result.getWidth(), result.getHeight(), rgb, 0,
                result.getWidth());

        return result;
    }

    @Override
    public void renderScene(GL3 arg0, MatF4 arg1) {
    }
}
