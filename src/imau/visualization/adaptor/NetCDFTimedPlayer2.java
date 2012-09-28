package imau.visualization.adaptor;

import imau.visualization.ImauSettings;

import java.io.File;

import javax.media.opengl.GL3;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;

import openglCommon.math.VecF3;
import openglCommon.util.CustomJSlider;
import util.ImauInputHandler;

public class NetCDFTimedPlayer2 implements Runnable {
    public static enum states {
        UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, REDRAWING, SNAPSHOTTING, MOVIEMAKING, CLEANUP, WAITINGONFRAME, PLAYING
    }

    private final ImauSettings        settings           = ImauSettings
                                                                 .getInstance();

    private states                    currentState       = states.UNOPENED;
    private int                       frameNumber;

    private final boolean             running            = true;
    private boolean                   initialized        = false;

    private long                      startTime, stopTime;

    private final JSlider             timeBar;
    private final JFormattedTextField frameCounter;

    private final ImauInputHandler    inputHandler;

    private NetCDFDatasetManager2     frameManagerDS1;
    private TextureStorage            texStorage;

    private boolean                   needsScreenshot    = false;
    private String                    screenshotFilename = "";

    public NetCDFTimedPlayer2(CustomJSlider timeBar,
            JFormattedTextField frameCounter) {
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
        this.inputHandler = ImauInputHandler.getInstance();
    }

    public void close() {
        initialized = false;
        frameNumber = 0;
        timeBar.setValue(0);
        frameCounter.setValue(0);
        timeBar.setMaximum(0);
    }

    public void delete(GL3 gl) {
        // TODO
    }

    public void init(File fileDS1) {
        this.frameManagerDS1 = new NetCDFDatasetManager2(fileDS1, 1, 4);
        this.texStorage = frameManagerDS1.getTextureStorage();

        final int initialMaxBar = frameManagerDS1.getNumFiles() - 1;

        timeBar.setMaximum(initialMaxBar);
        timeBar.setMinimum(0);

        updateFrame(0, true);

        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized boolean isPlaying() {
        if ((currentState == states.PLAYING)
                || (currentState == states.MOVIEMAKING)) {
            return true;
        }

        return false;
    }

    public synchronized void movieMode() {
        currentState = states.MOVIEMAKING;
    }

    public synchronized void oneBack() {
        stop();
        setFrame(frameNumber - 1, false);
    }

    public synchronized void oneForward() {
        stop();
        setFrame(frameNumber + 1, false);
    }

    public synchronized void redraw() {
        if (initialized) {
            setFrame(frameNumber, true);
            currentState = states.REDRAWING;
        }
    }

    public synchronized void rewind() {
        setFrame(0, false);
    }

    public synchronized void setScreenshotNeeded(boolean value) {
        needsScreenshot = value;
    }

    public synchronized boolean isScreenshotNeeded() {
        return needsScreenshot;
    }

    public String getScreenshotFileName() {
        return screenshotFilename;
    }

    @Override
    public void run() {
        if (!initialized) {
            System.err.println("HDFTimer started while not initialized.");
            System.exit(1);
        }

        inputHandler.setRotation(new VecF3(settings.getInitialRotationX(),
                settings.getInitialRotationY(), 0f));
        inputHandler.setViewDist(settings.getInitialZoom());

        stop();

        while (running) {
            if ((currentState == states.PLAYING)
                    || (currentState == states.REDRAWING)
                    || (currentState == states.MOVIEMAKING)) {
                try {
                    if (!isScreenshotNeeded()) {
                        startTime = System.currentTimeMillis();

                        if (currentState == states.MOVIEMAKING) {
                            if (settings.getMovieRotate()) {
                                final VecF3 rotation = inputHandler
                                        .getRotation();
                                System.out.println("Simulation frame: "
                                        + frameNumber + ", Rotation x: "
                                        + rotation.get(0) + " y: "
                                        + rotation.get(1));
                                // imauWindow.makeSnapshot(String.format("%05d",
                                // (frameNumber)));
                                screenshotFilename = String.format("%05d",
                                        (frameNumber));
                                setScreenshotNeeded(true);

                                rotation.set(
                                        1,
                                        rotation.get(1)
                                                + settings
                                                        .getMovieRotationSpeedDef());
                                inputHandler.setRotation(rotation);
                            } else {
                                screenshotFilename = String.format("%05d",
                                        (frameNumber));
                                setScreenshotNeeded(true);
                                // imauWindow.makeSnapshot(String.format("%05d",
                                // frameNumber));
                            }
                        }

                        // Forward frame
                        if (currentState != states.REDRAWING) {
                            updateFrame(frameNumber + 1, false);
                        }

                        // Wait for the _rest_ of the timeframe
                        stopTime = System.currentTimeMillis();
                        long spentTime = stopTime - startTime;

                        if (spentTime < settings.getWaittimeMovie()) {
                            Thread.sleep(settings.getWaittimeMovie()
                                    - spentTime);
                        }
                    }
                } catch (final InterruptedException e) {
                    System.err.println("Interrupted while playing.");
                }
            } else if (currentState == states.STOPPED) {
                try {
                    Thread.sleep(100);
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

    public synchronized void setFrame(int value, boolean overrideUpdate) {
        stop();

        updateFrame(value, overrideUpdate);
    }

    public synchronized void start() {
        currentState = states.PLAYING;
    }

    public synchronized void stop() {
        currentState = states.STOPPED;
    }

    private synchronized void updateFrame(int newFrameNumber,
            boolean overrideUpdate) {
        if (frameManagerDS1 != null) {
            if (newFrameNumber != frameNumber || overrideUpdate) {
                texStorage.requestNewFrame(newFrameNumber);

                frameNumber = newFrameNumber;
                settings.setFrameNumber(newFrameNumber);
                this.timeBar.setValue(newFrameNumber);
                this.frameCounter.setValue(newFrameNumber);
            }
        }
    }

    public TextureStorage getTextureStorage() {
        return texStorage;
    }
}
