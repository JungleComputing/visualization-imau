package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetCDFDatasetManager {
    private final ImauSettings            settings = ImauSettings.getInstance();
    private final static Logger           logger   = LoggerFactory
                                                           .getLogger(NetCDFDatasetManager.class);

    private NetCDFFrame                   frame0;
    private HashMap<Integer, NetCDFFrame> frameWindow;
    private static ArrayList<Integer>     availableFrames;

    private final int                     nThreads;
    private final PoolWorker[]            threads;
    private final LinkedList<Runnable>    queue;

    private final File                    ncfile;

    public void execute(Runnable r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        @Override
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

                    r = queue.removeFirst();
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

    public NetCDFDatasetManager(File ncfile) {
        logger.debug("Opening dataset with initial file: "
                + ncfile.getAbsolutePath());
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
        NetCDFDatasetManager.availableFrames = new ArrayList<Integer>();

        File currentFile = NetCDFUtil.getSeqLowestFile(ncfile);
        while (currentFile != null) {
            int nr = NetCDFUtil.getFrameNumber(currentFile);
            availableFrames.add(nr);

            currentFile = NetCDFUtil.getSeqNextFile(currentFile);
        }

        this.frame0 = new NetCDFFrame(NetCDFUtil.getSeqLowestFile(ncfile));
        this.frameWindow = new HashMap<Integer, NetCDFFrame>();

    }

    private HashMap<Integer, NetCDFFrame> getWindow(int index) {
        HashMap<Integer, NetCDFFrame> newFrameWindow = new HashMap<Integer, NetCDFFrame>();

        for (int i = index - settings.getPreprocessAmount(); i < settings
                .getPreprocessAmount() + index; i++) {
            try {
                NetCDFFrame frame = null;
                if (i == 0) {
                    frame = frame0;
                } else if (frameWindow.containsKey(i)) {
                    frame = frameWindow.get(i);
                } else if (i > 0 && i < availableFrames.size()) {
                    frame = new NetCDFFrame(NetCDFUtil.getSeqFile(ncfile,
                            availableFrames.get(i)));
                }
                if (frame != null) {
                    newFrameWindow.put(i, frame);
                }
            } catch (IOException e) {
                // Ignore
            }
        }

        for (NetCDFFrame f : newFrameWindow.values()) {
            execute(f);
        }

        return newFrameWindow;
    }

    public NetCDFFrame getFrame(int index) {
        frameWindow = getWindow(index);

        NetCDFFrame frame = frameWindow.get(index);

        return frame;
    }

    public static int getFrameNumberOfIndex(int index) {
        return availableFrames.get(index);
    }

    public static int getIndexOfFrameNumber(int frameNumber) {
        return availableFrames.indexOf(frameNumber);
    }

    public int getNumFiles() {
        return availableFrames.size();
    }
}
