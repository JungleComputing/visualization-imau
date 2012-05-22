package imau.visualization.adaptor;

import imau.visualization.ImauSettings;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

public class NetCDFFrameManager {
    private final ImauSettings settings = ImauSettings.getInstance();
    private final int lowestFrame;

    private NetCDFFrame frame0;
    private NetCDFFrame frame1;
    private HashMap<Integer, NetCDFFrame> frameWindow;

    private final int nThreads;
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    private File ncfile;

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;

            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    r = (Runnable) queue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    r.run();
                } catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }

    public NetCDFFrameManager(int lowestFrame, File ncfile) {
        this.lowestFrame = lowestFrame;
        this.ncfile = ncfile;

        this.nThreads = 5;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].setPriority(Thread.MIN_PRIORITY);
            threads[i].start();
        }

        resetModels();
    }

    public void resetModels() {
        this.frame0 = new NetCDFFrame(lowestFrame, ncfile);
        this.frame1 = new NetCDFFrame(lowestFrame + 1, ncfile);
        this.frameWindow = new HashMap<Integer, NetCDFFrame>();
    }

    private HashMap<Integer, NetCDFFrame> getWindow(int frameNumber) {
        HashMap<Integer, NetCDFFrame> newFrameWindow = new HashMap<Integer, NetCDFFrame>();
        for (int i = frameNumber; i < settings.getPreprocessAmount() + frameNumber; i++) {
            NetCDFFrame frame;

            if (i == 0) {
                frame = frame0;
            } else if (i == 1) {
                frame = frame1;
            } else if (frameWindow.containsKey(i)) {
                frame = frameWindow.get(i);
            } else {
                frame = new NetCDFFrame(i, ncfile);
            }

            newFrameWindow.put(i, frame);
        }

        for (int i = frameNumber; i < settings.getPreprocessAmount() + frameNumber - 1; i++) {

            execute(newFrameWindow.get(i));
        }

        return newFrameWindow;
    }

    public NetCDFFrame getFrame(int frameNumber) {
        frameWindow = getWindow(frameNumber);

        NetCDFFrame frame = frameWindow.get(frameNumber);

        return frame;
    }
}
