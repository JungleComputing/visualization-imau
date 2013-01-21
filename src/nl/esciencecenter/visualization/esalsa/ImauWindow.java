package nl.esciencecenter.visualization.esalsa;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;

import nl.esciencecenter.visualization.esalsa.data.ImauTimedPlayer;
import nl.esciencecenter.visualization.esalsa.data.SurfaceTextureDescription;
import nl.esciencecenter.visualization.esalsa.glExt.ByteBufferTexture;
import nl.esciencecenter.visualization.esalsa.glExt.FBO;
import nl.esciencecenter.visualization.esalsa.glExt.GeoSphere;
import nl.esciencecenter.visualization.esalsa.glExt.IntPBO;
import nl.esciencecenter.visualization.esalsa.glExt.Model;
import nl.esciencecenter.visualization.esalsa.glExt.MultiColorText;
import nl.esciencecenter.visualization.esalsa.glExt.Program;
import nl.esciencecenter.visualization.esalsa.glExt.ProgramLoader;
import nl.esciencecenter.visualization.esalsa.glExt.Quad;
import nl.esciencecenter.visualization.esalsa.glExt.Texture2D;
import nl.esciencecenter.visualization.esalsa.jni.SageInterface;
import nl.esciencecenter.visualization.esalsa.util.ImauInputHandler;
import nl.esciencecenter.visualization.openglCommon.datastructures.Material;
import nl.esciencecenter.visualization.openglCommon.exceptions.CompilationFailedException;
import nl.esciencecenter.visualization.openglCommon.exceptions.UninitializedException;
import nl.esciencecenter.visualization.openglCommon.math.Color4;
import nl.esciencecenter.visualization.openglCommon.math.MatF4;
import nl.esciencecenter.visualization.openglCommon.math.MatrixFMath;
import nl.esciencecenter.visualization.openglCommon.math.Point4;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;
import nl.esciencecenter.visualization.openglCommon.math.VecF4;
import nl.esciencecenter.visualization.openglCommon.text.FontFactory;
import nl.esciencecenter.visualization.openglCommon.text.TypecastFont;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImauWindow implements GLEventListener {
    private final static Logger         logger        = LoggerFactory
                                                              .getLogger(ImauWindow.class);
    private final ImauSettings          settings      = ImauSettings
                                                              .getInstance();

    private Quad                        fsq;

    protected final ProgramLoader       loader;
    protected final ImauInputHandler    inputHandler;

    private Program                     shaderProgram_Sphere,
            shaderProgram_Legend, shaderProgram_Atmosphere,
            shaderProgram_GaussianBlur, shaderProgram_FlattenLayers,
            shaderProgram_PostProcess, shaderProgram_Text;

    private Model                       sphereModel, legendModel, atmModel;

    private FBO                         atmosphereFBO, hudTextFBO,
            legendTextureFBO, sphereTextureFBO;

    private IntPBO                      finalPBO;

    private final BufferedImage         currentImage  = null;

    private SageInterface               sage;

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
    private float                       aspect;

    protected int                       fontSet       = FontFactory.UBUNTU;
    protected TypecastFont              font;
    protected int                       canvasWidth, canvasHeight;

    protected final float               radius        = 1.0f;
    protected final float               ftheta        = 0.0f;
    protected final float               phi           = 0.0f;

    protected final float               fovy          = 45.0f;
    protected final float               zNear         = 0.1f;
    protected final float               zFar          = 3000.0f;

    protected boolean                   post_process;

    public ImauWindow(ImauInputHandler inputHandler, boolean post_process) {
        this.loader = new ProgramLoader();
        this.inputHandler = inputHandler;
        this.font = (TypecastFont) FontFactory.get(fontSet).getDefault();
        this.post_process = true;

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

            displayContext(timer);
        }

        // try {
        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        // try {
        // finalPBO.copyToPBO(gl);
        // ByteBuffer bb = finalPBO.getBuffer();
        // sage.display(bb);
        //
        // finalPBO.unBind(gl);
        // } catch (UninitializedException e) {
        // e.printStackTrace();
        // }
        // }
        //
        // if (timer.isScreenshotNeeded()) {
        // try {
        // finalPBO.copyToPBO(gl);
        // ByteBuffer bb = finalPBO.getBuffer();
        // bb.rewind();
        //
        // int pixels = canvasWidth * canvasHeight;
        // int[] array = new int[pixels];
        // IntBuffer ib = IntBuffer.wrap(array);
        //
        // for (int i = 0; i < (pixels * 4); i += 4) {
        // int b = bb.get(i) & 0xFF;
        // int g = bb.get(i + 1) & 0xFF;
        // int r = bb.get(i + 2) & 0xFF;
        // int a = bb.get(i + 3) & 0xFF;
        //
        // int argb = (r << 16) | (g << 8) | b;
        // ib.put(argb);
        // }
        // ib.rewind();
        //
        // int[] destArray = new int[pixels];
        // IntBuffer dest = IntBuffer.wrap(destArray);
        //
        // int[] rowPix = new int[canvasWidth];
        // for (int row = 0; row < canvasHeight; row++) {
        // ib.get(rowPix);
        // dest.position((canvasHeight - row - 1) * canvasWidth);
        // dest.put(rowPix);
        // }
        //
        // BufferedImage bufIm = new BufferedImage(canvasWidth,
        // canvasHeight, BufferedImage.TYPE_INT_RGB);
        // bufIm.setRGB(0, 0, canvasWidth, canvasHeight, dest.array(),
        // 0, canvasWidth);
        // try {
        //
        // ImageIO.write(bufIm, "png",
        // new File(timer.getScreenshotFileName()));
        // } catch (IOException e2) {
        // // TODO Auto-generated catch block
        // e2.printStackTrace();
        // }
        //
        // finalPBO.unBind(gl);
        // } catch (UninitializedException e) {
        // e.printStackTrace();
        // }
        //
        // timer.setScreenshotNeeded(false);
        // }
        // drawable.getContext().release();
        // } catch (final GLException e) {
        // e.printStackTrace();
        // }

        reshaped = false;
    }

    private void displayContext(ImauTimedPlayer timer) {
        final int width = GLContext.getCurrent().getGLDrawable().getWidth();
        final int height = GLContext.getCurrent().getGLDrawable().getHeight();
        aspect = (float) width / (float) height;

        final GL3 gl = GLContext.getCurrentGL().getGL3();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

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

        drawAtmosphere(gl, mv, atmosphereFBO);
        blur(gl, atmosphereFBO, fsq, 1, 2, 4);

        SurfaceTextureDescription currentDesc;
        Texture2D surface, legend;

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
                        settings.getFancyDate(currentDesc.getFrameNumber()),
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
            surface.init(gl);

            legend = new ByteBufferTexture(GL3.GL_TEXTURE5, timer
                    .getTextureStorage().getLegendImage(i), 1, 500);
            legend.init(gl);

            drawSingleWindow(width, height, gl, mv, legend, surface,
                    varNames[i], dates[i], dataSets[i], legendTextsMin[i],
                    legendTextsMax[i], cachedFBOs[i]);

            surface.delete(gl);
            legend.delete(gl);
        }

        if (post_process) {
            logger.debug("Tiling windows");
            renderTexturesToScreen(gl, width, height);
        }
    }

    private void drawSingleWindow(final int width, final int height,
            final GL3 gl, MatF4 mv, Texture2D legend, Texture2D globe,
            MultiColorText varNameText, MultiColorText dateText,
            MultiColorText datasetText, MultiColorText legendTextMin,
            MultiColorText legendTextMax, FBO target) {
        logger.debug("Drawing Text");
        drawHUDText(gl, width, height, varNameText, dateText, datasetText,
                legendTextMin, legendTextMax, hudTextFBO);

        logger.debug("Drawing HUD");
        drawHUDLegend(gl, width, height, legend, legendTextureFBO);

        logger.debug("Drawing Sphere");
        drawSphere(gl, mv, globe, sphereTextureFBO);

        logger.debug("Flattening Layers");
        flattenLayers(gl, width, height, hudTextFBO, legendTextureFBO,
                sphereTextureFBO, atmosphereFBO, target);
    }

    private void drawHUDText(GL3 gl, int width, int height,
            MultiColorText varNameText, MultiColorText dateText,
            MultiColorText datasetText, MultiColorText legendTextMin,
            MultiColorText legendTextMax, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            // Draw text
            int textLength = varNameText.toString().length() * fontSize;

            varNameText.draw(gl, shaderProgram_Text, width, height, 2 * width
                    - textLength - 150, 40);

            textLength = datasetText.toString().length() * fontSize;
            datasetText.draw(gl, shaderProgram_Text, width, height, 10,
                    1.9f * height);

            textLength = dateText.toString().length() * fontSize;
            dateText.draw(gl, shaderProgram_Text, width, height, 10, 40);

            textLength = legendTextMin.toString().length() * fontSize;
            legendTextMin.draw(gl, shaderProgram_Text, width, height, 2 * width
                    - textLength - 100, .2f * height);

            textLength = legendTextMax.toString().length() * fontSize;
            legendTextMax.draw(gl, shaderProgram_Text, width, height, 2 * width
                    - textLength - 100, 1.75f * height);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawHUDLegend(GL3 gl, int width, int height,
            Texture2D legendTexture, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            // Draw legend texture
            shaderProgram_Legend.setUniform("texture_map",
                    legendTexture.getMultitexNumber());
            shaderProgram_Legend.setUniformMatrix("MVMatrix", new MatF4());
            shaderProgram_Legend.setUniformMatrix("PMatrix", new MatF4());

            shaderProgram_Legend.use(gl);
            legendModel.draw(gl, shaderProgram_Legend);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawSphere(GL3 gl, MatF4 mv, Texture2D surfaceTexture,
            FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            final MatF4 p = MatrixFMath.perspective(fovy, aspect, zNear, zFar);
            shaderProgram_Sphere.setUniformMatrix("MVMatrix", mv.clone());
            shaderProgram_Sphere.setUniformMatrix("PMatrix", p.clone());
            // shaderProgram_Sphere.setUniform("height_distortion_intensity",
            // settings.getHeightDistortion());
            shaderProgram_Sphere.setUniform("texture_map",
                    surfaceTexture.getMultitexNumber());
            // shaderProgram_Sphere.setUniform("height_map",
            // surfaceTexture.getMultitexNumber());

            shaderProgram_Sphere.use(gl);
            sphereModel.draw(gl, shaderProgram_Sphere);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void drawAtmosphere(GL3 gl, MatF4 mv, FBO target) {
        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            final MatF4 p = MatrixFMath.perspective(fovy, aspect, zNear, zFar);
            shaderProgram_Atmosphere.setUniformMatrix("MVMatrix", mv.clone());
            shaderProgram_Atmosphere.setUniformMatrix("PMatrix", p.clone());
            shaderProgram_Atmosphere.setUniformMatrix("NormalMatrix",
                    MatrixFMath.getNormalMatrix(mv));

            shaderProgram_Atmosphere.use(gl);
            atmModel.draw(gl, shaderProgram_Atmosphere);

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
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT);
            }

            shaderProgram_FlattenLayers.setUniform("textTex", hudTextFBO
                    .getTexture().getMultitexNumber());
            shaderProgram_FlattenLayers.setUniform("legendTex", hudLegendFBO
                    .getTexture().getMultitexNumber());
            shaderProgram_FlattenLayers.setUniform("dataTex", sphereTextureFBO
                    .getTexture().getMultitexNumber());
            shaderProgram_FlattenLayers.setUniform("atmosphereTex",
                    atmosphereFBO.getTexture().getMultitexNumber());

            shaderProgram_FlattenLayers.setUniformMatrix("MVMatrix",
                    new MatF4());
            shaderProgram_FlattenLayers
                    .setUniformMatrix("PMatrix", new MatF4());

            shaderProgram_FlattenLayers.setUniform("scrWidth", width);
            shaderProgram_FlattenLayers.setUniform("scrHeight", height);

            shaderProgram_FlattenLayers.use(gl);
            fsq.draw(gl, shaderProgram_FlattenLayers);

            if (post_process) {
                target.unBind(gl);
            }
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    private void blur(GL3 gl, FBO target, Quad fullScreenQuad, int passes,
            int blurType, float blurSize) {

        shaderProgram_GaussianBlur.setUniform("Texture", target.getTexture()
                .getMultitexNumber());

        shaderProgram_GaussianBlur.setUniformMatrix("PMatrix", new MatF4());
        shaderProgram_GaussianBlur.setUniformMatrix("MVMatrix", new MatF4());

        shaderProgram_GaussianBlur.setUniform("scrWidth", target.getTexture()
                .getWidth());
        shaderProgram_GaussianBlur.setUniform("scrHeight", target.getTexture()
                .getHeight());

        shaderProgram_GaussianBlur.setUniform("blurDirection", 0);
        shaderProgram_GaussianBlur.setUniform("blurSize", blurSize);
        shaderProgram_GaussianBlur.setUniform("blurType", blurType);

        shaderProgram_GaussianBlur.setUniform("Sigma", 0f);
        shaderProgram_GaussianBlur.setUniform("NumPixelsPerSide", 0f);
        shaderProgram_GaussianBlur.setUniform("Alpha", 1f);

        try {
            if (post_process) {
                target.bind(gl);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            }

            for (int i = 0; i < passes; i++) {
                shaderProgram_GaussianBlur.setUniform("blurDirection", 0);

                shaderProgram_GaussianBlur.use(gl);
                fullScreenQuad.draw(gl, shaderProgram_GaussianBlur);

                shaderProgram_GaussianBlur.setUniform("blurDirection", 1);

                shaderProgram_GaussianBlur.use(gl);
                fullScreenQuad.draw(gl, shaderProgram_GaussianBlur);
            }

            if (post_process) {
                target.unBind(gl);
            }
        } catch (final UninitializedException e) {
            e.printStackTrace();
        }
    }

    public void renderTexturesToScreen(GL3 gl, int width, int height) {
        try {
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            for (int i = 0; i < cachedScreens; i++) {
                shaderProgram_PostProcess.setUniform("sphereTexture_" + i,
                        cachedFBOs[i].getTexture().getMultitexNumber());
            }

            shaderProgram_PostProcess.setUniformMatrix("MVMatrix", new MatF4());
            shaderProgram_PostProcess.setUniformMatrix("PMatrix", new MatF4());

            shaderProgram_PostProcess.setUniform("scrWidth", width);
            shaderProgram_PostProcess.setUniform("scrHeight", height);

            int selection = settings.getWindowSelection();

            shaderProgram_PostProcess.setUniform("divs_x",
                    settings.getNumScreensCols());
            shaderProgram_PostProcess.setUniform("divs_y",
                    settings.getNumScreensRows());
            shaderProgram_PostProcess.setUniform("selection", selection);

            shaderProgram_PostProcess.use(gl);
            fsq.draw(gl, shaderProgram_PostProcess);
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
            if (shaderProgram_PostProcess != null) {
                shaderProgram_PostProcess.delete(gl);
                shaderProgram_PostProcess = loader.createProgram(gl,
                        "postprocess", new File("shaders/vs_postprocess.vp"),
                        new File("shaders/fs_postprocess.fp"));
                // PostprocShaderCreator.generateShaderText(
                // settings.getNumScreensRows(),
                // settings.getNumScreensCols()));
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

        GL3 gl = drawable.getGL().getGL3();
        gl.glViewport(0, 0, w, h);

        canvasWidth = w;
        canvasHeight = h;

        atmosphereFBO.delete(gl);
        hudTextFBO.delete(gl);
        legendTextureFBO.delete(gl);
        sphereTextureFBO.delete(gl);

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);
        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        // finalPBO.delete(gl);
        // finalPBO = new IntPBO(canvasWidth, canvasHeight);
        // finalPBO.init(gl);
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

        final GL3 gl = drawable.getGL().getGL3();

        loader.cleanup(gl);
        timer.close();
        timer.stop();

        sphereModel.delete(gl);
        atmModel.delete(gl);
        legendModel.delete(gl);

        for (int i = 0; i < cachedScreens; i++) {
            cachedFBOs[i].delete(gl);
        }

        inputHandler.close();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
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

        canvasWidth = drawable.getWidth();
        canvasHeight = drawable.getHeight();

        // First, init the 'normal' context
        GL3 gl = drawable.getGL().getGL3();

        // Anti-Aliasing
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);

        // Depth testing
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glClearDepth(1.0f);

        // Culling
        gl.glEnable(GL3.GL_CULL_FACE);
        gl.glCullFace(GL3.GL_BACK);

        // Enable Blending (needed for both Transparency and
        // Anti-Aliasing
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL3.GL_BLEND);

        // Enable Vertical Sync
        gl.setSwapInterval(1);

        // Set black background
        gl.glClearColor(0f, 0f, 0f, 0f);

        logger.debug("W: " + canvasWidth + ", H: " + canvasHeight);

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);
        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        // finalPBO = new IntPBO(canvasWidth, canvasHeight);
        // finalPBO.init(gl);
        // }

        atmosphereFBO.init(gl);
        hudTextFBO.init(gl);
        legendTextureFBO.init(gl);
        sphereTextureFBO.init(gl);

        fsq = new Quad(Material.random(), 2, 2, new VecF3(0, 0, 0.1f));
        fsq.init(gl);

        // sphereModel = new GeoSphereCut(Material.random(), 120, 120, 50f,
        // false);
        sphereModel = new GeoSphere(Material.random(), 60, 60, 50f, false);
        sphereModel.init(gl);

        // cutModel = new GeoSphereCutEdge(Material.random(), 120, 50f);
        // cutModel.init(gl);

        legendModel = new Quad(Material.random(), 1.5f, .1f, new VecF3(1, 0,
                0.1f));
        legendModel.init(gl);

        Color4 atmosphereColor = new Color4(0.0f, 1.0f, 1.0f, 0.005f);
        atmModel = new GeoSphere(new Material(atmosphereColor, atmosphereColor,
                atmosphereColor), 60, 60, 53f, false);
        atmModel.init(gl);

        // Material textMaterial = new Material(Color4.white, Color4.white,
        // Color4.white);

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
            shaderProgram_Sphere = loader.createProgram(gl,
                    "shaderProgram_Sphere", new File("shaders/vs_texture.vp"),
                    new File("shaders/fs_texture.fp"));

            shaderProgram_Legend = loader.createProgram(gl,
                    "shaderProgram_Legend", new File("shaders/vs_texture.vp"),
                    new File("shaders/fs_texture.fp"));

            shaderProgram_Text = loader.createProgram(gl, "shaderProgram_Text",
                    new File("shaders/vs_multiColorTextShader.vp"), new File(
                            "shaders/fs_multiColorTextShader.fp"));

            shaderProgram_Atmosphere = loader.createProgram(gl,
                    "shaderProgram_Atmosphere", new File(
                            "shaders/vs_atmosphere.vp"), new File(
                            "shaders/fs_atmosphere.fp"));

            shaderProgram_GaussianBlur = loader.createProgram(gl,
                    "shaderProgram_GaussianBlur", new File(
                            "shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_gaussian_blur.fp"));

            shaderProgram_PostProcess = loader.createProgram(gl,
                    "shaderProgram_PostProcess", new File(
                            "shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_postprocess.fp"));

            shaderProgram_FlattenLayers = loader.createProgram(gl,
                    "shaderProgram_FlattenLayers", new File(
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

    public ImauInputHandler getInputHandler() {
        return inputHandler;
    }
}
