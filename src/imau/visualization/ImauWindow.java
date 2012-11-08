package imau.visualization;

import imau.visualization.adaptor.FBO;
import imau.visualization.adaptor.GlobeState;
import imau.visualization.adaptor.GlobeState.DataMode;
import imau.visualization.adaptor.ImageMaker;
import imau.visualization.adaptor.IntPBO;
import imau.visualization.adaptor.NetCDFTexture;
import imau.visualization.adaptor.NetCDFTimedPlayer2;
import imau.visualization.adaptor.SurfaceTextureDescription;
import imau.visualization.adaptor.Texture2D;
import imau.visualization.adaptor.WrongFrameException;
import imau.visualization.jni.SageInterface;

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
import util.ImauInputHandler;

public class ImauWindow extends CommonWindow {

    private final ImauSettings  settings     = ImauSettings.getInstance();

    private Quad                fsq;
    private Program             texturedSphereProgram, legendProgram,
            atmProgram, gaussianBlurShader, flatten3Shader, postprocessShader,
            textProgram;
    // private Texture2D worldTex;

    private Model               sphereModel, legendModel, atmModel;

    private FBO                 ltFBO, rtFBO, lbFBO, rbFBO, atmosphereFBO,
            hudTextFBO, legendTextureFBO, sphereTextureFBO;

    private IntPBO              finalPBO;

    private final BufferedImage currentImage = null;

    private SageInterface       sage;

    private MultiColorText      varNameTextLT, varNameTextRT, varNameTextLB,
            varNameTextRB, legendTextLTmin, legendTextRTmin, legendTextLBmin,
            legendTextRBmin, legendTextLTmax, legendTextRTmax, legendTextLBmax,
            legendTextRBmax, dateTextLT, dateTextRT, dateTextLB, dateTextRB,
            datasetTextLT, datasetTextRT, datasetTextLB, datasetTextRB;

    private int                 fontSize     = 80;

    private boolean             reshaped     = false;

    private GlobeState          ltState, rtState, lbState, rbState;

    private NetCDFTimedPlayer2  timer;

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

        NetCDFTimedPlayer2 timer = ImauPanel.getTimer();
        if (timer.isInitialized()) {
            this.timer = timer;

            try {
                displayContext(timer, ltFBO, rtFBO, lbFBO, rbFBO,
                        atmosphereFBO, hudTextFBO, legendTextureFBO,
                        sphereTextureFBO);
            } catch (WrongFrameException e) {
                System.out.println(e.getMessage());
            }
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

    private void displayContext(NetCDFTimedPlayer2 timer, FBO ltFBO, FBO rtFBO,
            FBO lbFBO, FBO rbFBO, FBO atmosphereFBO, FBO hudTextFBO,
            FBO legendTextureFBO, FBO sphereTextureFBO)
            throws WrongFrameException {
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

        GlobeState state;
        Texture2D surface, heightMap, legend;

        state = settings.getLTState();
        if (state != ltState || reshaped) {
            SurfaceTextureDescription desc = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(), state
                            .getVariable().toString(), state.getColorMap(),
                    settings.isDynamicDimensions(),
                    state.getDataMode() == DataMode.DIFF ? true : false,
                    state.getDataMode() == DataMode.SECOND_DATASET ? true
                            : false, state.getLowerBound(),
                    state.getUpperBound());

            timer.getTextureStorage().requestNewConfiguration(0, desc);

            String variableName = settings
                    .bandNameToString(state.getVariable());
            String units = GlobeState.verbalizeUnits(state.getVariableIndex());
            variableName += " in " + units;
            String min, max;
            if (desc.isDiff()) {
                min = Float.toString(settings.getCurrentVarDiffMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarDiffMax(state
                        .getVariable()));
            } else {
                min = Float.toString(settings.getCurrentVarMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarMax(state
                        .getVariable()));
            }
            varNameTextLT.setString(gl, variableName, Color4.white, fontSize);
            dateTextLT.setString(gl, settings.getMonth(state.getFrameNumber()),
                    Color4.white, fontSize);
            datasetTextLT.setString(gl,
                    GlobeState.verbalizeDataMode(state.getDataModeIndex()),
                    Color4.white, fontSize);
            legendTextLTmin.setString(gl, min, Color4.white, fontSize);
            legendTextLTmax.setString(gl, max, Color4.white, fontSize);

            ltState = state;
        }

        // setHUDVars(gl, timer.getTextureStorage().getDimensions(0), desc,
        // varNameTextLT, legendTextLTmin,
        // legendTextLTmax);

        surface = new NetCDFTexture(GL3.GL_TEXTURE8, timer.getTextureStorage()
                .getSurfaceImage(0), settings.getImageWidth(),
                settings.getImageHeight());
        heightMap = surface;
        legend = new NetCDFTexture(GL3.GL_TEXTURE9, timer.getTextureStorage()
                .getLegendImage(0), 1, 500);
        surface.init(gl);
        legend.init(gl);

        drawSingleWindow(atmosphereFBO, hudTextFBO, legendTextureFBO,
                sphereTextureFBO, width, height, gl, mv, legend, surface,
                heightMap, varNameTextLT, dateTextLT, datasetTextLT,
                legendTextLTmin, legendTextLTmax, ltFBO);

        surface.delete(gl);
        legend.delete(gl);

        state = settings.getRTState();
        if (state != rtState || reshaped) {
            SurfaceTextureDescription desc = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(), state
                            .getVariable().toString(), state.getColorMap(),
                    settings.isDynamicDimensions(),
                    state.getDataMode() == DataMode.DIFF ? true : false,
                    state.getDataMode() == DataMode.SECOND_DATASET ? true
                            : false, state.getLowerBound(),
                    state.getUpperBound());
            timer.getTextureStorage().requestNewConfiguration(1, desc);

            // setHUDVars(gl, timer.getTextureStorage().getDimensions(1), desc,
            // varNameTextRT, legendTextRTmin,
            // legendTextRTmax);

            String variableName = settings
                    .bandNameToString(state.getVariable());
            String units = GlobeState.verbalizeUnits(state.getVariableIndex());
            variableName += " in " + units;
            String min, max;
            if (desc.isDiff()) {
                min = Float.toString(settings.getCurrentVarDiffMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarDiffMax(state
                        .getVariable()));
            } else {
                min = Float.toString(settings.getCurrentVarMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarMax(state
                        .getVariable()));
            }
            varNameTextRT.setString(gl, variableName, Color4.white, fontSize);
            dateTextRT.setString(gl, settings.getMonth(state.getFrameNumber()),
                    Color4.white, fontSize);
            datasetTextRT.setString(gl,
                    GlobeState.verbalizeDataMode(state.getDataModeIndex()),
                    Color4.white, fontSize);
            legendTextRTmin.setString(gl, min, Color4.white, fontSize);
            legendTextRTmax.setString(gl, max, Color4.white, fontSize);
            rtState = state;
        }

        surface = new NetCDFTexture(GL3.GL_TEXTURE8, timer.getTextureStorage()
                .getSurfaceImage(1), settings.getImageWidth(),
                settings.getImageHeight());
        heightMap = surface;
        legend = new NetCDFTexture(GL3.GL_TEXTURE9, timer.getTextureStorage()
                .getLegendImage(1), 1, 500);
        surface.init(gl);
        legend.init(gl);

        drawSingleWindow(atmosphereFBO, hudTextFBO, legendTextureFBO,
                sphereTextureFBO, width, height, gl, mv, legend, surface,
                heightMap, varNameTextRT, dateTextRT, datasetTextRT,
                legendTextRTmin, legendTextRTmax, rtFBO);

        surface.delete(gl);
        legend.delete(gl);

        state = settings.getLBState();
        if (state != lbState || reshaped) {
            SurfaceTextureDescription desc = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(), state
                            .getVariable().toString(), state.getColorMap(),
                    settings.isDynamicDimensions(),
                    state.getDataMode() == DataMode.DIFF ? true : false,
                    state.getDataMode() == DataMode.SECOND_DATASET ? true
                            : false, state.getLowerBound(),
                    state.getUpperBound());
            timer.getTextureStorage().requestNewConfiguration(2, desc);

            String variableName = settings
                    .bandNameToString(state.getVariable());
            String units = GlobeState.verbalizeUnits(state.getVariableIndex());
            variableName += " in " + units;
            String min, max;
            if (desc.isDiff()) {
                min = Float.toString(settings.getCurrentVarDiffMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarDiffMax(state
                        .getVariable()));
            } else {
                min = Float.toString(settings.getCurrentVarMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarMax(state
                        .getVariable()));
            }
            varNameTextLB.setString(gl, variableName, Color4.white, fontSize);
            dateTextLB.setString(gl, settings.getMonth(state.getFrameNumber()),
                    Color4.white, fontSize);
            datasetTextLB.setString(gl,
                    GlobeState.verbalizeDataMode(state.getDataModeIndex()),
                    Color4.white, fontSize);
            legendTextLBmin.setString(gl, min, Color4.white, fontSize);
            legendTextLBmax.setString(gl, max, Color4.white, fontSize);

            lbState = state;
            // setHUDVars(gl, timer.getTextureStorage().getDimensions(2), desc,
            // varNameTextLB, legendTextLBmin, legendTextLBmax);
        }

        surface = new NetCDFTexture(GL3.GL_TEXTURE8, timer.getTextureStorage()
                .getSurfaceImage(2), settings.getImageWidth(),
                settings.getImageHeight());
        heightMap = surface;
        legend = new NetCDFTexture(GL3.GL_TEXTURE9, timer.getTextureStorage()
                .getLegendImage(2), 1, 500);
        surface.init(gl);
        legend.init(gl);

        drawSingleWindow(atmosphereFBO, hudTextFBO, legendTextureFBO,
                sphereTextureFBO, width, height, gl, mv, legend, surface,
                heightMap, varNameTextLB, dateTextLB, datasetTextLB,
                legendTextLBmin, legendTextLBmax, lbFBO);

        surface.delete(gl);
        legend.delete(gl);

        state = settings.getRBState();
        if (state != rbState || reshaped) {
            SurfaceTextureDescription desc = new SurfaceTextureDescription(
                    state.getFrameNumber(), state.getDepth(), state
                            .getVariable().toString(), state.getColorMap(),
                    settings.isDynamicDimensions(),
                    state.getDataMode() == DataMode.DIFF ? true : false,
                    state.getDataMode() == DataMode.SECOND_DATASET ? true
                            : false, state.getLowerBound(),
                    state.getUpperBound());
            timer.getTextureStorage().requestNewConfiguration(3, desc);

            String variableName = settings
                    .bandNameToString(state.getVariable());
            String units = GlobeState.verbalizeUnits(state.getVariableIndex());
            variableName += " in " + units;
            String min, max;
            if (desc.isDiff()) {
                min = Float.toString(settings.getCurrentVarDiffMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarDiffMax(state
                        .getVariable()));
            } else {
                min = Float.toString(settings.getCurrentVarMin(state
                        .getVariable()));
                max = Float.toString(settings.getCurrentVarMax(state
                        .getVariable()));
            }
            varNameTextRB.setString(gl, variableName, Color4.white, fontSize);
            dateTextRB.setString(gl, settings.getMonth(state.getFrameNumber()),
                    Color4.white, fontSize);
            datasetTextRB.setString(gl,
                    GlobeState.verbalizeDataMode(state.getDataModeIndex()),
                    Color4.white, fontSize);
            legendTextRBmin.setString(gl, min, Color4.white, fontSize);
            legendTextRBmax.setString(gl, max, Color4.white, fontSize);

            rbState = state;
            // setHUDVars(gl, timer.getTextureStorage().getDimensions(3), desc,
            // varNameTextRB, legendTextRBmin, legendTextRBmax);
        }

        surface = new NetCDFTexture(GL3.GL_TEXTURE8, timer.getTextureStorage()
                .getSurfaceImage(3), settings.getImageWidth(),
                settings.getImageHeight());
        heightMap = surface;
        legend = new NetCDFTexture(GL3.GL_TEXTURE9, timer.getTextureStorage()
                .getLegendImage(3), 1, 500);
        surface.init(gl);
        legend.init(gl);

        drawSingleWindow(atmosphereFBO, hudTextFBO, legendTextureFBO,
                sphereTextureFBO, width, height, gl, mv, legend, surface,
                heightMap, varNameTextRB, dateTextRB, datasetTextRB,
                legendTextRBmin, legendTextRBmax, rbFBO);

        surface.delete(gl);
        legend.delete(gl);

        if (post_process) {
            renderTexturesToScreen(gl, width, height, ltFBO, rtFBO, lbFBO,
                    rbFBO);
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

    // private HDRTexture2D getGlobeTexture(NetCDFFrame frame1,
    // NetCDFFrame frame2, final GL3 gl, int glTexUnit, GlobeState state)
    // throws WrongFrameException {
    // HDRTexture2D globeTex = null;
    // if (state.getDataMode() == GlobeState.DataMode.DIFF) {
    // if (frame1 != null && frame2 != null) {
    // globeTex = frame1.getImage(gl, frame2, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
    // if (frame1 != null) {
    // globeTex = frame1.getImage(gl, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
    // if (frame2 != null) {
    // globeTex = frame2.getImage(gl, glTexUnit, state);
    // }
    // }
    // return globeTex;
    // }

    // private HDRTexture2D getDepthTexture(NetCDFFrame frame1, NetCDFFrame
    // frame2, final GL3 gl, int glTexUnit,
    // GlobeState state) throws WrongFrameException {
    // HDRTexture2D depthTex = null;
    // if (state.getDataMode() == GlobeState.DataMode.DIFF) {
    // if (frame1 != null && frame2 != null) {
    // depthTex = frame1.getDepthImage(gl, frame2, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
    // if (frame1 != null) {
    // depthTex = frame1.getDepthImage(gl, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
    // if (frame2 != null) {
    // depthTex = frame2.getDepthImage(gl, glTexUnit, state);
    // }
    // }
    // return depthTex;
    // }

    // private HDRTexture2D getLegendTexture(NetCDFFrame frame1,
    // NetCDFFrame frame2, final GL3 gl, int glTexUnit, GlobeState state)
    // throws WrongFrameException {
    // HDRTexture2D legendTex = null;
    // if (state.getDataMode() == GlobeState.DataMode.DIFF) {
    // if (frame1 != null && frame2 != null) {
    // legendTex = frame1.getLegendImage(gl, frame2, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.FIRST_DATASET) {
    // if (frame1 != null) {
    // legendTex = frame1.getLegendImage(gl, glTexUnit, state);
    // }
    // } else if (state.getDataMode() == GlobeState.DataMode.SECOND_DATASET) {
    // if (frame2 != null) {
    // legendTex = frame2.getLegendImage(gl, glTexUnit, state);
    // }
    // }
    // return legendTex;
    // }

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

    private void renderTexturesToScreen(GL3 gl, int width, int height,
            FBO sphereFBOLT, FBO sphereFBORT, FBO sphereFBOLB, FBO sphereFBORB) {
        try {
            postprocessShader.setUniform("sphereTextureLT", sphereFBOLT
                    .getTexture().getMultitexNumber());
            postprocessShader.setUniform("sphereTextureRT", sphereFBORT
                    .getTexture().getMultitexNumber());
            postprocessShader.setUniform("sphereTextureLB", sphereFBOLB
                    .getTexture().getMultitexNumber());
            postprocessShader.setUniform("sphereTextureRB", sphereFBORB
                    .getTexture().getMultitexNumber());

            postprocessShader.setUniform("sphereBrightness", 1f);

            postprocessShader.setUniformMatrix("MVMatrix", new MatF4());
            postprocessShader.setUniformMatrix("PMatrix", new MatF4());

            postprocessShader.setUniform("scrWidth", width);
            postprocessShader.setUniform("scrHeight", height);

            int selection = settings.getWindowSelection();

            if (selection == 0) {
                postprocessShader.setUniform("divs", 2);
            } else {
                postprocessShader.setUniform("divs", 1);
            }

            postprocessShader.setUniform("selection", selection);

            postprocessShader.use(gl);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            fsq.draw(gl, postprocessShader, new MatF4());
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
    public void renderTexturesToScreen(GL3 arg0, int arg1, int arg2) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        super.reshape(drawable, x, y, w, h);
        final GL3 gl = drawable.getGL().getGL3();

        ltFBO.delete(gl);
        rtFBO.delete(gl);
        lbFBO.delete(gl);
        rbFBO.delete(gl);

        atmosphereFBO.delete(gl);

        hudTextFBO.delete(gl);
        legendTextureFBO.delete(gl);

        ltFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);
        rtFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        lbFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        rbFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);

        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        finalPBO.delete(gl);
        finalPBO = new IntPBO(canvasWidth, canvasHeight);
        finalPBO.init(gl);
        // }

        ltFBO.init(gl);
        rtFBO.init(gl);
        lbFBO.init(gl);
        rbFBO.init(gl);

        atmosphereFBO.init(gl);

        hudTextFBO.init(gl);
        legendTextureFBO.init(gl);
        sphereTextureFBO.init(gl);

        fontSize = (int) Math.round(w / 37.5);

        reshaped = true;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        super.dispose(drawable);
        final GL3 gl = drawable.getGL().getGL3();

        loader.cleanup(gl);
        sphereModel.delete(gl);
        atmModel.delete(gl);

        ltFBO.delete(gl);
        rtFBO.delete(gl);
        lbFBO.delete(gl);
        rbFBO.delete(gl);

        atmosphereFBO.delete(gl);

        ((ImauInputHandler) inputHandler).close();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        super.init(drawable);

        System.out.println("W: " + canvasWidth + ", H: " + canvasHeight);

        drawable.getContext().makeCurrent();
        final GL3 gl = drawable.getGL().getGL3();

        ltFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE0);
        rtFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE1);
        lbFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE2);
        rbFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE3);

        atmosphereFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE4);
        hudTextFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE5);
        legendTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE6);
        sphereTextureFBO = new FBO(canvasWidth, canvasHeight, GL.GL_TEXTURE7);

        // if (settings.isIMAGE_STREAM_OUTPUT()) {
        finalPBO = new IntPBO(canvasWidth, canvasHeight);
        finalPBO.init(gl);
        // }

        ltFBO.init(gl);
        rtFBO.init(gl);
        lbFBO.init(gl);
        rbFBO.init(gl);

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

        varNameTextLT = new MultiColorText(textMaterial, font);
        varNameTextRT = new MultiColorText(textMaterial, font);
        varNameTextLB = new MultiColorText(textMaterial, font);
        varNameTextRB = new MultiColorText(textMaterial, font);

        legendTextLTmin = new MultiColorText(textMaterial, font);
        legendTextRTmin = new MultiColorText(textMaterial, font);
        legendTextLBmin = new MultiColorText(textMaterial, font);
        legendTextRBmin = new MultiColorText(textMaterial, font);

        legendTextLTmax = new MultiColorText(textMaterial, font);
        legendTextRTmax = new MultiColorText(textMaterial, font);
        legendTextLBmax = new MultiColorText(textMaterial, font);
        legendTextRBmax = new MultiColorText(textMaterial, font);

        dateTextLT = new MultiColorText(textMaterial, font);
        dateTextRT = new MultiColorText(textMaterial, font);
        dateTextLB = new MultiColorText(textMaterial, font);
        dateTextRB = new MultiColorText(textMaterial, font);

        datasetTextLT = new MultiColorText(textMaterial, font);
        datasetTextRT = new MultiColorText(textMaterial, font);
        datasetTextLB = new MultiColorText(textMaterial, font);
        datasetTextRB = new MultiColorText(textMaterial, font);

        varNameTextLT.init(gl);
        varNameTextRT.init(gl);
        varNameTextLB.init(gl);
        varNameTextRB.init(gl);

        legendTextLTmin.init(gl);
        legendTextRTmin.init(gl);
        legendTextLBmin.init(gl);
        legendTextRBmin.init(gl);

        legendTextLTmax.init(gl);
        legendTextRTmax.init(gl);
        legendTextLBmax.init(gl);
        legendTextRBmax.init(gl);

        dateTextLT.init(gl);
        dateTextRT.init(gl);
        dateTextLB.init(gl);
        dateTextRB.init(gl);

        datasetTextLT.init(gl);
        datasetTextRT.init(gl);
        datasetTextLB.init(gl);
        datasetTextRB.init(gl);

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

            postprocessShader = loader.createProgram(gl, "postprocess",
                    new File("shaders/vs_postprocess.vp"), new File(
                            "shaders/fs_eSalsaPostprocess.fp"));

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

    private void setHUDVars(final GL3 gl, ImageMaker.Dimensions dims,
            SurfaceTextureDescription desc, MultiColorText txtVar,
            MultiColorText txtMin, MultiColorText txtMax)
            throws WrongFrameException {
        String variableName = desc.getVarName();
        // String units = GlobeState.verbalizeUnits(state.getVariableIndex());
        // variableName += " in " + units;
        String min = Float.toString(dims.min);
        String max = Float.toString(dims.max);
        txtVar.setString(gl, variableName, Color4.white, fontSize);
        txtMin.setString(gl, min, Color4.white, fontSize);
        txtMax.setString(gl, max, Color4.white, fontSize);
    }

    public void makeSnapshot() {
        // try {
        // final int status = offScreenContext.makeCurrent();
        // if ((status != GLContext.CONTEXT_CURRENT)
        // && (status != GLContext.CONTEXT_CURRENT_NEW)) {
        // System.err.println("Error swapping context to offscreen.");
        // }
        // } catch (final GLException e) {
        // System.err
        // .println("Exception while swapping context to offscreen.");
        // e.printStackTrace();
        // }
        //
        // final int width = offScreenContext.getGLDrawable().getWidth();
        // final int height = offScreenContext.getGLDrawable().getHeight();
        //
        // final GL3 gl = offScreenContext.getGL().getGL3();
        // gl.glViewport(0, 0, width, height);
        //
        // // Anti-Aliasing
        // gl.glEnable(GL.GL_LINE_SMOOTH);
        // gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
        // gl.glEnable(GL2GL3.GL_POLYGON_SMOOTH);
        // gl.glHint(GL2GL3.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
        //
        // // Depth testing
        // gl.glEnable(GL.GL_DEPTH_TEST);
        // gl.glDepthFunc(GL.GL_LEQUAL);
        // gl.glClearDepth(1.0f);
        //
        // // Culling
        // gl.glEnable(GL.GL_CULL_FACE);
        // gl.glCullFace(GL.GL_BACK);
        //
        // // Enable Blending (needed for both Transparency and
        // // Anti-Aliasing
        // gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        // gl.glEnable(GL.GL_BLEND);
        //
        // gl.glClearColor(0f, 0f, 0f, 0f);
        //
        // boolean sync = false;
        // // while (!sync) {
        // // try {
        // // displayContext(timer, ltFBO, rtFBO, lbFBO, rbFBO, atmosphereFBO,
        // // hudTextFBO,
        // // legendTextureFBO, sphereTextureFBO);
        // // sync = true;
        // // } catch (WrongFrameException e) {
        // // System.out.println("Screenshotter: " + e.getMessage());
        // // }
        // // }
        //
        // final Picture p = new Picture(width, height);
        //
        // gl.glFinish();
        //
        // p.copyFrameBufferToFile(settings.getScreenshotPath(), fileName);
        //
        // try {
        // offScreenContext.release();
        // } catch (final GLException e) {
        // e.printStackTrace();
        // }

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
