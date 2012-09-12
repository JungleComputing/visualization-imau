package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.ImauWindow;

import java.io.File;

import javax.media.opengl.GL3;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;

import openglCommon.exceptions.UninitializedException;
import openglCommon.math.VecF3;
import openglCommon.util.CustomJSlider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ImauInputHandler;

public class NetCDFTimedPlayer implements Runnable {
    public static enum states {
        UNOPENED, UNINITIALIZED, INITIALIZED, STOPPED, REDRAWING, SNAPSHOTTING, MOVIEMAKING, CLEANUP, WAITINGONFRAME, PLAYING
    }

    private final ImauSettings settings = ImauSettings.getInstance();
    private final static Logger logger = LoggerFactory.getLogger(NetCDFTimedPlayer.class);

    private states currentState = states.UNOPENED;
    private int frameNumber;

    private NetCDFFrame currentFrameDS1, currentFrameDS2;

    private final boolean running = true;
    private boolean initialized = false;

    private long startTime, stopTime;

    private final JSlider timeBar;
    private final JFormattedTextField frameCounter;

    private ImauInputHandler inputHandler;

    private ImauWindow imauWindow;
    private NetCDFDatasetManager frameManagerDS1, frameManagerDS2;

    public NetCDFTimedPlayer(CustomJSlider timeBar, JFormattedTextField frameCounter) {
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
    }

    public NetCDFTimedPlayer(ImauWindow window, JSlider timeBar, JFormattedTextField frameCounter) {
        this.imauWindow = window;
        inputHandler = ImauInputHandler.getInstance();
        this.timeBar = timeBar;
        this.frameCounter = frameCounter;
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

    public int getFrameNumber() {
        return frameNumber;
    }

    public NetCDFFrame getFrame() throws UninitializedException {
        if (frameManagerDS1 == null) {
            throw new UninitializedException("Not properly initialized.");
        }
        return currentFrameDS1;
    }

    public NetCDFFrame getFrame2() throws UninitializedException {
        if (frameManagerDS2 == null) {
            throw new UninitializedException("Second frame requested while not twosourced.");
        }
        if (currentFrameDS2 == null) {
            updateFrame(frameNumber, true);
        }
        return currentFrameDS2;
    }

    public synchronized states getState() {
        return currentState;
    }

    public void init(File fileDS1) {
        this.frameManagerDS1 = new NetCDFDatasetManager(fileDS1);

        final int initialMaxBar = frameManagerDS1.getNumFiles() - 1;

        timeBar.setMaximum(initialMaxBar);
        timeBar.setMinimum(0);

        updateFrame(0, true);

        initialized = true;
    }

    public void init(File fileDS1, File fileDS2) {
        this.frameManagerDS1 = new NetCDFDatasetManager(fileDS1);
        this.frameManagerDS2 = new NetCDFDatasetManager(fileDS2);

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
        if (currentFrameDS1 != null && currentFrameDS2 != null) {
            if (newFrameNumber != frameNumber || overrideUpdate) {
                NetCDFFrame frameDS1 = frameManagerDS1.getFrame(newFrameNumber);
                NetCDFFrame frameDS2 = frameManagerDS2.getFrame(newFrameNumber);

                if (!frameDS1.isError() && !frameDS2.isError()) {
                    frameNumber = newFrameNumber;
                    settings.setFrameNumber(newFrameNumber);
                    this.timeBar.setValue(newFrameNumber);
                    this.frameCounter.setValue(newFrameNumber);

                    currentFrameDS1 = frameDS1;
                    currentFrameDS2 = frameDS2;
                } else {
                    logger.warn(frameDS1.getError() + " " + frameDS2.getError());
                    currentState = states.WAITINGONFRAME;
                }
            }
        } else {
            if (currentFrameDS1 == null || newFrameNumber != frameNumber || overrideUpdate) {
                NetCDFFrame frame = frameManagerDS1.getFrame(newFrameNumber);

                if (!frame.isError()) {
                    frameNumber = newFrameNumber;
                    settings.setFrameNumber(newFrameNumber);
                    this.timeBar.setValue(newFrameNumber);
                    this.frameCounter.setValue(newFrameNumber);

                    currentFrameDS1 = frame;
                } else {
                    logger.warn(frame.getError());
                    currentState = states.WAITINGONFRAME;
                }
            }
        }

    }

    public boolean isTwoSourced() {
        if (currentFrameDS1 != null && currentFrameDS2 != null) {
            return true;
        }
        return false;
    }
}
