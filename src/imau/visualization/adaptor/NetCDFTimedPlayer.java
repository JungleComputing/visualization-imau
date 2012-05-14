package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.ImauWindow;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL3;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;

import openglCommon.math.VecF3;
import openglCommon.util.CustomJSlider;
import openglCommon.util.InputHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetCDFTimedPlayer implements Runnable {
    public static enum states {
        UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, REDRAWING, SNAPSHOTTING, MOVIEMAKING, CLEANUP, WAITINGONFRAME, PLAYING
    }

    private final ImauSettings        settings     = ImauSettings.getInstance();
    private final static Logger       logger       = LoggerFactory.getLogger(NetCDFTimedPlayer.class);

    private states                    currentState = states.UNOPENED;
    private int                       frameNumber;

    private NetCDFFrame               currentFrame;

    private boolean                   running      = true, initialized = false;

    private File                      ncfile       = null;

    private long                      startTime, stopTime;

    private int                       lowestFrameNumber;
    private final JSlider             timeBar;
    private final JFormattedTextField frameCounter;

    private InputHandler              inputHandler;

    private ImauWindow                imauWindow;

    public NetCDFTimedPlayer(CustomJSlider timeBar, JFormattedTextField frameCounter) {
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
    }

    public NetCDFTimedPlayer(ImauWindow window, JSlider timeBar, JFormattedTextField frameCounter) {
        this.imauWindow = window;
        inputHandler = InputHandler.getInstance();
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
    }

    public void close() {
        running = false;
        initialized = false;
        frameNumber = 0;
        timeBar.setValue(0);
        frameCounter.setValue(0);
        timeBar.setMaximum(0);
    }

    public void delete(GL3 gl) {
        // TODO
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public NetCDFFrame getFrame() {
        return currentFrame;
    }

    public states getState() {
        return currentState;
    }

    public void init() {
        if (ncfile == null) {
            logger.error("HDFTimer initialized with no open file.");
            System.exit(1);
        }

        frameNumber = NetCDFUtil.getFrameNumber(ncfile);

        try {
            currentFrame = updateFrame(ncfile, true);
        } catch (IOException e) {
            logger.error("Initial simulation frame (settings) not found. Trying again from lowest frame.");
            frameNumber = NetCDFUtil.getLowestFileNumber(ncfile);
            try {
                currentFrame = updateFrame(ncfile, true);
            } catch (IOException e1) {
                logger.error("Frame " + frameNumber + " also not found. Exiting.");
                System.exit(1);
            }
        }

        final int initialMaxBar = NetCDFUtil.getNumFiles(ncfile);
        timeBar.setMaximum(initialMaxBar);
        lowestFrameNumber = NetCDFUtil.getLowestFileNumber(ncfile);

        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isPlaying() {
        if ((currentState == states.PLAYING) || (currentState == states.MOVIEMAKING)) {
            return true;
        }

        return false;
    }

    public void movieMode() {
        currentState = states.MOVIEMAKING;
    }

    public void oneBack() {
        stop();
        setFrame(frameNumber - 1, false);
    }

    public void oneForward() {
        stop();
        setFrame(frameNumber + 1, false);
    }

    public void open(File file) {
        this.ncfile = file;
        init();
    }

    public void redraw() {
        if (initialized) {
            setFrame(frameNumber, true);
            currentState = states.REDRAWING;
        }
    }

    public void rewind() {
        setFrame(0, false);
    }

    @Override
    public void run() {
        if (!initialized) {
            System.err.println("HDFTimer started while not initialized.");
            System.exit(1);
        }

        inputHandler.setRotation(new VecF3(settings.getInitialRotationX(), settings.getInitialRotationY(), 0f));
        inputHandler.setViewDist(settings.getInitialZoom());

        timeBar.setValue(frameNumber - lowestFrameNumber);
        frameCounter.setValue(frameNumber - lowestFrameNumber);

        currentState = states.STOPPED;

        while (running) {
            if ((currentState == states.PLAYING) || (currentState == states.REDRAWING)
                    || (currentState == states.MOVIEMAKING)) {
                try {
                    startTime = System.currentTimeMillis();

                    if (currentState != states.REDRAWING) {
                        try {
                            currentFrame = updateFrame(
                                    NetCDFUtil.getNextFile(ncfile, NetCDFUtil.getFrameNumber(ncfile)), false);
                        } catch (final IOException e) {
                            setFrame(frameNumber - 1, false);
                            currentState = states.WAITINGONFRAME;
                            System.err.println(e);
                            System.err.println(" run File not found, retrying from frame " + frameNumber + ".");
                            continue;
                        }
                    }

                    if (currentState == states.MOVIEMAKING) {
                        if (settings.getMovieRotate()) {
                            final VecF3 rotation = inputHandler.getRotation();
                            System.out.println("Simulation frame: " + frameNumber + ", Rotation x: " + rotation.get(0)
                                    + " y: " + rotation.get(1));
                            imauWindow.makeSnapshot(String.format("%05d", (frameNumber)));

                            rotation.set(1, rotation.get(1) + settings.getMovieRotationSpeedDef());
                            inputHandler.setRotation(rotation);
                        } else {
                            imauWindow.makeSnapshot(String.format("%05d", frameNumber));
                        }
                    }

                    timeBar.setValue(frameNumber - lowestFrameNumber);
                    frameCounter.setValue(frameNumber - lowestFrameNumber);

                    stopTime = System.currentTimeMillis();
                    if (((startTime - stopTime) < settings.getWaittimeMovie()) && (currentState != states.MOVIEMAKING)) {
                        Thread.sleep(settings.getWaittimeMovie() - (startTime - stopTime));
                    }
                } catch (final InterruptedException e) {
                    System.err.println("Interrupted while playing.");
                }
            } else if (currentState == states.STOPPED) {
                try {
                    Thread.sleep(settings.getWaittimeBeforeRetry());
                } catch (final InterruptedException e) {
                    System.err.println("Interrupted while stopped.");
                }
            } else if (currentState == states.REDRAWING) {
                currentState = states.STOPPED;
            } else if (currentState == states.WAITINGONFRAME) {
                try {
                    Thread.sleep(settings.getWaittimeBeforeRetry());
                    currentState = states.PLAYING;
                } catch (final InterruptedException e) {
                    System.err.println("Interrupted while waiting.");
                }
            }
        }
    }

    public void setFrame(int value, boolean overrideUpdate) {
        // System.out.println("setValue?");
        currentState = states.STOPPED;

        try {
            currentFrame = updateFrame(NetCDFUtil.getFile(ncfile, value), overrideUpdate);
            timeBar.setValue(frameNumber - lowestFrameNumber);
            frameCounter.setValue(frameNumber - lowestFrameNumber);
        } catch (final IOException e) {
            logger.warn("setFrame File not found, retrying from frame " + (value - 1) + ".");

            if (value - 1 < 0) {
                setFrame(NetCDFUtil.getLowestFileNumber(ncfile), overrideUpdate);
            } else {
                setFrame(value - 1, overrideUpdate);
            }
            currentState = states.WAITINGONFRAME;
        } catch (final Throwable t) {
            logger.error("Got error in setFrame!");
            t.printStackTrace(System.err);
        }
    }

    public void start() {
        currentState = states.PLAYING;
    }

    public void stop() {
        currentState = states.STOPPED;
    }

    private synchronized NetCDFFrame updateFrame(File ncfile, boolean overrideUpdate) throws IOException {
        int newFrameNumber = NetCDFUtil.getFrameNumber(ncfile);

        if (currentFrame == null || newFrameNumber != frameNumber || overrideUpdate) {
            NetCDFFrame frame = new NetCDFFrame(newFrameNumber);

            System.out.println("Updating file number: " + NetCDFUtil.getFrameNumber(ncfile));

            // TODO: read and process

            frameNumber = newFrameNumber;
            return frame;
        }

        return currentFrame;
    }

    public int getLowestFrameNumber() {
        return lowestFrameNumber;
    }
}
