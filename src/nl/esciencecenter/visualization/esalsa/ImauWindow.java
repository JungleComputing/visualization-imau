package nl.esciencecenter.visualization.esalsa;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import nl.esciencecenter.visualization.esalsa.data.ImauTimedPlayer;
import nl.esciencecenter.visualization.esalsa.data.SurfaceTextureDescription;
import nl.esciencecenter.visualization.esalsa.glExt.ByteBufferTexture;
import nl.esciencecenter.visualization.esalsa.glExt.FBO;
import nl.esciencecenter.visualization.esalsa.glExt.IntPBO;
import nl.esciencecenter.visualization.esalsa.glExt.PostprocShaderCreator;
import nl.esciencecenter.visualization.esalsa.glExt.Texture2D;
import nl.esciencecenter.visualization.esalsa.jni.SageInterface;
import nl.esciencecenter.visualization.esalsa.util.ImauInputHandler;
import openglCommon.CommonWindow;
import openglCommon.datastructures.Material;
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
import openglCommon.models.MultiColorText;
import openglCommon.models.Text;
import openglCommon.models.base.Quad;
import openglCommon.models.base.Sphere;
import openglCommon.shaders.Program;

public class ImauWindow extends CommonWindow {

    private final ImauSettings          settings      = ImauSettings
                                                              .getInstance();

    private Quad                        fsq;
    private Program                     texturedSphereProgram, legendProgram,
            atmProgram, gaussianBlurShader, flatten3Shader, postprocessShader,
            textProgram;
    // private Texture2D worldTex;

    private Model                       sphereModel, legendModel, atmModel;

    // private FBO ltFBO, rtFBO, lbFBO, rbFBO;
    private FBO                         atmosphereFBO, hudTextFBO,
            legendTextureFBO, sphereTextureFBO;

    private IntPBO                      finalPBO;

    private final BufferedImage         currentImage  = null;

    private SageInterface               sage;

    // private MultiColorText varNameTextLT, varNameTextRT,
    // varNameTextLB, varNameTextRB, legendTextLTmin, legendTextRTmin,
    // legendTextLBmin, legendTextRBmin, legendTextLTmax, legendTextRTmax,
    // legendTextLBmax, legendTextRBmax, dateTextLT, dateTextRT,
    // dateTextLB, dateTextRB, datasetTextLT, datasetTextRT,
    // datasetTextLB, datasetTextRB;
    //
    // private SurfaceTextureDescription ltDescription, rtDescription,
    // lbDescription, rbDescription;

    private int                         fontSize      = 80;

    private boolean                     reshaped      = false;

    private SurfaceTextureDescription[] cachedTextureDescriptions;
    private FBO[]                       cachedFBOs;
    private MultiColorText[]            varNames;
    private MultiColorText[]            legendTextsMin;
    private MultiColorText[]            legendTextsMax;
    private MultiColorText[]            dates;
    private MultiColorText[]            dataSets;

    private int                         cachedScreens = 1;

    private ImauTimedPlayer             timer;

    public ImauWindow(ImauInputHandler inputHandler, boolean post_process) {
        super(inputHandler, post_process);

        cachedScreens = settings.getNumScreensRows()
                * settings.getNumScreensCols();

        cachedTextureDescriptions = new SurfaceTextureDescription[cachedScreens];
        cachedFBOs = new FBO[cachedScreens];
        varNames = new MultiColorText[cachedScreens];
        legendTextsMin = new MultiColorText[cachedScreens];
        legendTextsMax = new MultiColorText[cachedScreens];
        dates = new MultiColorText[cachedScreens];
        dataSets = new MultiColorText[cachedScreens];
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

        final GL3 gl = drawable.getContext().getGL().getGL3();
        gl.glViewport(0, 0, canvasWidth, canvasHeight);

        ImauTimedPlayer timer = ImauPanel.getTimer();
        if (timer.isInitialized()) {
            this.timer = timer;

            int currentScreens = settings.getNumScreensRows()
                    * settings.getNumScreensCols();
            if (currentScreens != cachedScreens) {
                initDatastores(gl);
                timer.reinitializeDatastores();
            }

            displayContext(timer, atmosphereFBO, hudTextFBO, legendTextureFBO,
                    sphereTextureFBO);
        }

        try {
            if (settings.isIMAGE_STREAM_OUTPUT()) {
                try {
                    finalPBO.copyToPBO(gl);
                    ByteBuffer bb = finalPBO.getBuffer();
                    sage.display(bb);

                    finalPBO.unBind(gl);
                } catch (UninitializedException e) {
                    e.printStackTrace();
                }
            }

            if (timer.isScreenshotNeeded()) {
                try {
                    finalPBO.copyToPBO(gl);
                    ByteBuffer bb = finalPBO.getBuffer();
                    bb.rewind();

                    int pixels = canvasWidth * canvasHeight;
                    int[] array = new int[pixels];
                    IntBuffer ib = IntBuffer.wrap(array);

                    for (int i = 0; i < (pixels * 4); i += 4) {
                        int b = bb.get(i) & 0xFF;
                        int g = bb.get(i + 1) & 0xFF;
                        int r = bb.get(i + 2) & 0xFF;
                        int a = bb.get(i + 3) & 0xFF;

                        int argb = (r << 16) | (g << 8) | b;
                        ib.put(argb);
                    }
                    ib.rewind();

                    int[] destArray = new int[pixels];
                    IntBuffer dest = IntBuffer.wrap(destArray);

                    int[] rowPix = new int[canvasWidth];
                    for (int row = 0; row < canvasHeight; row++) {
                        ib.get(rowPix);
                        dest.position((canvasHeight - row - 1) * canvasWidth);
                        dest.put(rowPix);
                    }

                    BufferedImage bufIm = new BufferedImage(canvasWidth,
                            canvasHeight, BufferedImage.TYPE_INT_RGB);
                    bufIm.setRGB(0, 0, canvasWidth, canvasHeight, dest.array(),
                            0, canvasWidth);
                    try {

                        ImageIO.write(bufIm, "png",
                                new File(timer.getScreenshotFileName()));
                    } catch (IOException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }

                    finalPBO.unBind(gl);
                } catch (UninitializedException e) {
                    e.printStackTrace();
                }

                timer.setScreenshotNeeded(false);
            }
            drawable.getContext().release();
        } catch (final GLException e) {
            e.printStackTrace();
        }

        reshaped = false;
    }

    private void displayContext(ImauTimedPlayer timer, FBO atmosphereFBO,
            FBO hudTextFBO, FBO legendTextureFBO, FBO sphereTextureFBO) {
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

        drawAtmosphere(gl, mv, atmProgram, atmosphereFBO);
        blur(gl, atmosphereFBO, fsq, 1, 2, 4);

        SurfaceTextureDescription currentDesc;
        Texture2D surface, heightMap, legend;

        for (int i = 0; i < cachedScreens; i++) {
            currentDesc = settings.getSurfaceDescription(i);
            if (!currentDesc.equals(cachedTextureDescriptions[i]) || reshaped) {
                timer.getTextureStorage().requestNewConfiguration(i,
                        currentDesc);

                String variableName = currentDesc.getVarName();
                String fancyName = timer.getVariableFancyName(variableName);
                String units = timer.getVariableUnits(variableName);
                fancyName += " in " + units;
                varNames[i].setString(gl, fancyName, Color4.white, fontSize);

                String min, max;
                if (currentDesc.isDiff()) {
                    min = Float.toString(settings
                            .getCurrentVarDiffMin(currentDesc.getVarName()));
                    max = Float.toString(settings
                            .getCurrentVarDiffMax(currentDesc.getVarName()));
                } else {
                    min = Float.toString(settings.getCurrentVarMin(currentDesc
                            .getVarName()));
                    max = Float.toString(settings.getCurrentVarMax(currentDesc
                            .getVarName()));
                }
                dates[i].setString(gl,
                        settings.getMonth(currentDesc.getFrameNumber()),
                        Color4.white, fontSize);
                dataSets[i].setString(gl, currentDesc.verbalizeDataMode(),
                        Color4.white, fontSize);
                legendTextsMin[i].setString(gl, min, Color4.white, fontSize);
                legendTextsMax[i].setString(gl, max, Color4.white, fontSize);

                cachedTextureDescriptions[i] = currentDesc;
            }

            surface = new ByteBufferTexture(GL3.GL_TEXTURE4, timer
                    .getTextureStorage().getSurfaceImage(i),
                    timer.getImageWidth(), timer.getImageHeight());
            heightMap = surface;
            legend = new ByteBufferTexture(GL3.GL_TEXTURE5, timer
                    .getTextureStorage().getLegendImage(i), 1, 500);
            surface.init(gl);
            legend.init(gl);

            drawSingleWindow(atmosphereFBO, hudTextFBO, legendTextureFBO,
                    sphereTextureFBO, width, height, gl, mv, legend, surface,
                    heightMap, varNames[i], dates[i], dataSets[i],
                    legendTextsMin[i], legendTextsMax[i], cachedFBOs[i]);

            surface.delete(gl);
            legend.delete(gl);
        }

        if (post_process) {
            renderTexturesToScreen(gl, width, height);
        }
    }

    private void drawSingleWindow(FBO atmosphereFBO, FBO hudTextFBO,
            FBO hudLegendTextureFBO, FBO sphereTextureFBO, final int width,
            final int height, final GL3 gl, MatF4 mv, Texture2D legend,
            Texture2D globe, Texture2D heightMap, MultiColorText varNameText,
            MultiColorText dateText, MultiColorText datasetText,
            MultiColorText legendTextMin, MultiColorText legendTextMax,
            FBO target) {
        drawHUDText(gl, width, height, varNameText, dateText, datasetText,
                legendTextMin, legendTextMax, textProgram, hudTextFBO);
        drawHUDLegend(gl, width, height, legend, legendProgram,
                hudLegendTextureFBO);
        drawSphere(gl, mv, globe, heightMap, texturedSphereProgram,
                sphereTextureFBO);

        flattenLayers(gl, width, height, hudTextFBO, hudLegendTextureFBO,
                sphereTextureFBO, atmosphereFBO, target);
    }

    private void drawHUDText(GL3 gl, int width, int height,
            MultiColorText varNameText, MultiColorText dateText,
            MultiColorText datasetText, MultiColorText legendTextMin,
            MultiColorText legendTextMax, Program textProgram, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            // Draw text
            int textLength = varNameText.toString().length() * fontSize;

            varNameText.draw(
                    gl,
                    textProgram,
                    Text.getPMVForHUD(width, height, 2 * width - textLength
                            - 150, 40));

            textLength = datasetText.toString().length() * fontSize;
            datasetText.draw(gl, textProgram,
                    Text.getPMVForHUD(width, height, 10, 1.9f * height));

            textLength = dateText.toString().length() * fontSize;
            dateText.draw(gl, textProgram,
                    Text.getPMVForHUD(width, height, 10, 40));

            textLength = legendTextMin.toString().length() * fontSize;
            legendTextMin.draw(
                    gl,
                    textProgram,
                    Text.getPMVForHUD(width, height, 2 * width - textLength
                            - 100, .2f * height));

            textLength = legendTextMax.toString().length() * fontSize;
            legendTextMax.draw(
                    gl,
                    textProgram,
                    Text.getPMVForHUD(width, height, 2 * width - textLength
                            - 100, 1.75f * height));

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawHUDLegend(GL3 gl, int width, int height,
            Texture2D legendTexture, Program legendProgram, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            // Draw legend texture
            legendProgram.setUniform("texture_map",
                    legendTexture.getMultitexNumber());
            legendProgram.setUniformMatrix("PMatrix", new MatF4());

            legendModel.draw(gl, legendProgram, new MatF4());

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawSphere(GL3 gl, MatF4 mv, Texture2D surfaceTexture,
            Texture2D heightMap, Program program, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            program.setUniform("height_distortion_intensity",
                    settings.getHeightDistortion());
            program.setUniform("texture_map",
                    surfaceTexture.getMultitexNumber());
            program.setUniform("height_map", surfaceTexture.getMultitexNumber());

            sphereModel.draw(gl, program, mv);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawAtmosphere(GL3 gl, MatF4 mv, Program program, FBO target) {
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
            e.printStackTrace();
        }
    }

    private void flattenLayers(GL3 gl, int width, int height, FBO hudTextFBO,
            FBO hudLegendFBO, FBO sphereTextureFBO, FBO atmosphereFBO,
            FBO target) {
        try {
            target.bind(gl);
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);

            flatten3Shader.setUniform("textTex", hudTextFBO.getTexture()
                    .getMultitexNumber());
            flatten3Shader.setUniform("legendTex", hudLegendFBO.getTexture()
                    .getMultitexNumber());
            flatten3Shader.setUniform("dataTex", sphereTextureFBO.getTexture()
                    .getMultitexNumber());
            flatten3Shader.setUniform("atmosphereTex", atmosphereFBO
                    .getTexture().getMultitexNumber());

            flatten3Shader.setUniformMatrix("MVMatrix", new MatF4());
            flatten3Shader.setUniformMatrix("PMatrix", new MatF4());

            flatten3Shader.setUniform("scrWidth", width);
            flatten3Shader.setUniform("scrHeight", height);

            flatten3Shader.use(gl);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            fsq.draw(gl, flatten3Shader, new MatF4());

            target.unBind(gl);
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void blur(GL3 gl, FBO target, Quad fullScreenQuad, int passes,
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
        gaussianBlurShader.setUniform("NumPixelsPerSide", 0f);
        gaussianBlurShader.setUniform("Sigma", 0f);

        try {
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
    public void renderTexturesToScreen(GL3 gl, int width, int height) {
        try {
            for (int i = 0; i < cachedScreens; i++) {
                postprocessShader.setUniform("sphereTexture_" + i,
                        cachedFBOs[i].getTexture().getMultitexNumber());
            }

            postprocessShader.setUniform("sphereBrightness", 1f);

            postprocessShader.setUniformMatrix("MVMatrix", new MatF4());
            postprocessShader.setUniformMatrix("PMatrix", new MatF4());

            postprocessShader.setUniform("scrWidth", width);
            postprocessShader.setUniform("scrHeight", height);

            int selection = settings.getWindowSelection();

            postprocessShader.setUniform("selection", selection);

            postprocessShader.use(gl);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            fsq.draw(gl, postprocessShader, new MatF4());
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void initDatastores(GL3 gl) {
        cachedScreens = settings.getNumScreensRows()
                * settings.getNumScreensCols();

        cachedTextureDescriptions = new SurfaceTextureDescription[cachedScreens];
        cachedFBOs = new FBO[cachedScreens];
        varNames = new MultiColorText[cachedScreens];
        legendTextsMin = new MultiColorText[cachedScreens];
        legendTextsMax = new MultiColorText[cachedScreens];
        dates = new MultiColorText[cachedScreens];
        dataSets = new MultiColorText[cachedScreens];

        Material textMaterial = new Material(Color4.white, Color4.white,
                Color4.white);

        for (int i = 0; i < cachedScreens; i++) {
            cachedTextureDescriptions[i] = settings.getSurfaceDescription(i);

            if (cachedFBOs[i] != null) {
                cachedFBOs[i].delete(gl);
            }
            cachedFBOs[i] = new FBO(canvasWidth, canvasHeight,
                    (GL.GL_TEXTURE6 + i));
            cachedFBOs[i].init(gl);

            varNames[i] = new MultiColorText(textMaterial, font);
            legendTextsMin[i] = new MultiColorText(textMaterial, font);
            legendTextsMin[i] = new MultiColorText(textMaterial, font);
            legendTextsMax[i] = new MultiColorText(textMaterial, font);
            dates[i] = new MultiColorText(textMaterial, font);
            dataSets[i] = new MultiColorText(textMaterial, font);
        }

        try {
            if (postprocessShader != null) {
                postprocessShader.delete(gl);
                postprocessShader = loader.createProgram(
                        gl,
                        "postprocess",
                        new File("shaders/vs_postprocess.vp"),
                        PostprocShaderCreator.generateShaderText(
                                settings.getNumScreensRows(),
                                settings.getNumScreensCols()));
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CompilationFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);
        final GL3 gl = drawable.getGL().getGL3();

        atmosphereFBO.delete(gl);

        hudTextFBO.delete(gl);
        legendTextureFBO.delete(gl);

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);

        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        finalPBO.delete(gl);
        finalPBO = new IntPBO(canvasWidth, canvasHeight);
        finalPBO.init(gl);
        // }

        atmosphereFBO.init(gl);

        hudTextFBO.init(gl);
        legendTextureFBO.init(gl);
        sphereTextureFBO.init(gl);

        fontSize = (int) Math.round(w / 37.5);

        initDatastores(gl);

        reshaped = true;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();

        loader.cleanup(gl);
        sphereModel.delete(gl);
        atmModel.delete(gl);

        for (int i = 0; i < cachedScreens; i++) {
            cachedFBOs[i].delete(gl);
        }

        ((ImauInputHandler) inputHandler).close();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        System.out.println("W: " + canvasWidth + ", H: " + canvasHeight);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);
        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        finalPBO = new IntPBO(canvasWidth, canvasHeight);
        finalPBO.init(gl);
        // }

        atmosphereFBO.init(gl);
        hudTextFBO.init(gl);
        legendTextureFBO.init(gl);
        sphereTextureFBO.init(gl);

        fsq = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        fsq.init(gl);

        // sphereModel = new GeoSphereCut(Material.random(), 120, 120, 50f,
        // false);
        sphereModel = new GeoSphere(Material.random(), 120, 120, 50f, false);
        sphereModel.init(gl);

        // cutModel = new GeoSphereCutEdge(Material.random(), 120, 50f);
        // cutModel.init(gl);

        legendModel = new Quad(Material.random(), 1.5f, .1f, new VecF3(1, 0,
                0.1f));
        legendModel.init(gl);

        Color4 atmosphereColor = new Color4(0.0f, 1.0f, 1.0f, 0.005f);

        atmModel = new Sphere(new Material(atmosphereColor, atmosphereColor,
                atmosphereColor), 5, 53f, new VecF3(), false);
        atmModel.init(gl);

        Material textMaterial = new Material(Color4.white, Color4.white,
                Color4.white);

        initDatastores(gl);

        // varNameTextLT.finalizeColorScheme(gl);
        // varNameTextRT.finalizeColorScheme(gl);
        // varNameTextLB.finalizeColorScheme(gl);
        // varNameTextRB.finalizeColorScheme(gl);

        // worldTex = new ImageTexture("textures/4_no_ice_clouds_mts_8k.jpg",
        // 4096, 0, GL3.GL_TEXTURE2);
        // bumpTex = new BumpTexture("textures/elev_bump_8k.jpg", 4096, 0,
        // GL3.GL_TEXTURE3);
        // worldTex.init(gl);
        // bumpTex.init(gl);

        // atmModel = new GeoSphere(new Material(atmosphereColor,
        // atmosphereColor, atmosphereColor), 50, 50, 55f, false);

        inputHandler.setViewDist(-130f);

        try {
            texturedSphereProgram = loader.createProgram(gl,
                    "texturedSphereProgram", new File("shaders/vs_pplTex2.vp"),
                    new File("shaders/fs_pplTex2.fp"));

            legendProgram = loader
                    .createProgram(gl, "legendProgram", new File(
                            "shaders/vs_texture.vp"), new File(
                            "shaders/fs_texture.fp"));

            textProgram = loader.createProgram(gl, "textProgram", new File(
                    "shaders/vs_multiColorTextShader.vp"), new File(
                    "shaders/fs_multiColorTextShader.fp"));

            atmProgram = loader.createProgram(gl, "atmProgram", new File(
                    "shaders/vs_atmosphere.vp"), new File(
                    "shaders/fs_atmosphere.fp"));

            gaussianBlurShader = loader.createProgram(gl, "gaussianBlur",
                    new File("shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_gaussian_blur.fp"));

            postprocessShader = loader.createProgram(
                    gl,
                    "postprocess",
                    new File("shaders/vs_postprocess.vp"),
                    PostprocShaderCreator.generateShaderText(
                            settings.getNumScreensRows(),
                            settings.getNumScreensCols()));

            flatten3Shader = loader.createProgram(gl, "flatten3", new File(
                    "shaders/vs_flatten3.vp"), new File(
                    "shaders/fs_flatten3.fp"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CompilationFailedException e) {
            e.printStackTrace();
        }

        System.out.println("window sage init? :"
                + (settings.isIMAGE_STREAM_OUTPUT() ? "true" : "false"));
        if (settings.isIMAGE_STREAM_OUTPUT()) {
            Dimension frameDim = ImauApp.getFrameSize();

            if (settings.isIMAGE_STREAM_GL_ONLY()) {
                frameDim = new Dimension(canvasWidth, canvasHeight);
            }

            sage = new SageInterface(frameDim.width, frameDim.height,
                    settings.getSageFramesPerSecond());
        }
    }

    public void makeSnapshot() {
        if (timer != null) {
            timer.setScreenshotNeeded(true);
        }
    }

    private int[] knitImages(BufferedImage glCanvasImage) {
        int[] frameRGB = null;

        int glWidth = glCanvasImage.getWidth();
        int glHeight = glCanvasImage.getHeight();

        int[] glRGB = new int[glWidth * glHeight];
        glCanvasImage.getRGB(0, 0, glWidth, glHeight, glRGB, 0, glWidth);

        if (settings.isIMAGE_STREAM_GL_ONLY()) {
            frameRGB = glRGB;
        } else {
            BufferedImage frame = ImauApp.getFrameImage();

            int frameWidth = frame.getWidth();
            int frameHeight = frame.getHeight();

            frameRGB = new int[frameWidth * frameHeight];
            frame.getRGB(0, 0, frameWidth, frameHeight, frameRGB, 0, frameWidth);

            Point p = ImauApp.getCanvaslocation();

            for (int y = p.y; y < p.y + glHeight; y++) {
                int offset = (y - p.y) * glWidth;
                System.arraycopy(glRGB, offset, frameRGB, y * frameWidth + p.x,
                        glWidth);
            }
        }

        return frameRGB;
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

        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();

        int[] frameRGB = new int[frameWidth * frameHeight];
        frame.getRGB(0, 0, frameWidth, frameHeight, frameRGB, 0, frameWidth);

        int glWidth = currentImage.getWidth();
        int glHeight = currentImage.getHeight();

        int[] glRGB = new int[glWidth * glHeight];
        currentImage.getRGB(0, 0, glWidth, glHeight, glRGB, 0, glWidth);

        Point p = ImauApp.getCanvaslocation();

        for (int y = p.y; y < p.y + glHeight; y++) {
            int offset = (y - p.y) * glWidth;
            System.arraycopy(glRGB, offset, frameRGB, y * frameWidth + p.x,
                    glWidth);
        }

        BufferedImage result = new BufferedImage(frame.getWidth(),
                frame.getHeight(), BufferedImage.TYPE_INT_RGB);

        result.setRGB(0, 0, result.getWidth(), result.getHeight(), frameRGB, 0,
                result.getWidth());

        return result;
    }

    @Override
    public void renderScene(GL3 arg0, MatF4 arg1) {
    }
}
