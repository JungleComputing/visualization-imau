package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.ImauWindow;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;

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

    private final ImauSettings settings = ImauSettings.getInstance();
    private final static Logger logger = LoggerFactory.getLogger(NetCDFTimedPlayer.class);

    private states currentState = states.UNOPENED;
    private int frameNumber;

    private NetCDFFrame currentFrame;

    private boolean running = true, initialized = false;

    private File ncfile = null;

    private long startTime, stopTime;

    private int lowestFrameNumber;
    private final JSlider timeBar;
    private final JFormattedTextField frameCounter;

    private InputHandler inputHandler;

    private ImauWindow imauWindow;
    private NetCDFFrameManager frameManager;

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

    public synchronized states getState() {
        return currentState;
    }

    public void init(File file) {
        this.ncfile = file;
        this.frameManager = new NetCDFFrameManager(0, file);

        if (ncfile == null) {
            logger.error("NetCDFTimer initialized with null file.");
            System.exit(1);
        }

        final int initialMaxBar = NetCDFUtil.getNumFiles(ncfile) - 1;
        lowestFrameNumber = NetCDFUtil.getLowestFileNumber(ncfile);

        timeBar.setMaximum(initialMaxBar + lowestFrameNumber);
        timeBar.setMinimum(lowestFrameNumber);

        updateFrame(lowestFrameNumber, true);

        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized boolean isPlaying() {
        if ((currentState == states.PLAYING) || (currentState == states.MOVIEMAKING)) {
            return true;
        }

        return false;
    }

    public synchronized void movieMode() {
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

    public synchronized void redraw() {
        if (initialized) {
            setFrame(frameNumber, true);
            currentState = states.REDRAWING;
        }
    }

    public void rewind() {
        setFrame(lowestFrameNumber, false);
    }

    @Override
    public void run() {
        if (!initialized) {
            System.err.println("HDFTimer started while not initialized.");
            System.exit(1);
        }

        inputHandler.setRotation(new VecF3(settings.getInitialRotationX(), settings.getInitialRotationY(), 0f));
        inputHandler.setViewDist(settings.getInitialZoom());

        stop();

        while (running) {
            if ((currentState == states.PLAYING) || (currentState == states.REDRAWING)
                    || (currentState == states.MOVIEMAKING)) {
                try {
                    startTime = System.currentTimeMillis();

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

                    // Forward frame
                    if (currentState != states.REDRAWING) {
                        updateFrame(frameNumber + 1, false);
                    }

                    // Wait for the _rest_ of the timeframe
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
        stop();

        updateFrame(value, overrideUpdate);
    }

    public synchronized void start() {
        currentState = states.PLAYING;
    }

    public synchronized void stop() {
        currentState = states.STOPPED;
    }

    private synchronized void updateFrame(int newFrameNumber, boolean overrideUpdate) {
        if (currentFrame == null || newFrameNumber != frameNumber || overrideUpdate) {
            NetCDFFrame frame = frameManager.getFrame(newFrameNumber);

            if (!frame.isError()) {
                frameNumber = newFrameNumber;
                this.timeBar.setValue(newFrameNumber);
                this.frameCounter.setValue(newFrameNumber);

                currentFrame = frame;
            } else {
                currentState = states.WAITINGONFRAME;
            }
        }
    }

    public int getLowestFrameNumber() {
        return lowestFrameNumber;
    }
}
