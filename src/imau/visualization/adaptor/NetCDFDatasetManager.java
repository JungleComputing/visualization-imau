package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.netcdf.NetCDFNoSuchVariableException;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFDatasetManager {
    private final ImauSettings            settings = ImauSettings.getInstance();
    private final static Logger           logger   = LoggerFactory.getLogger(NetCDFDatasetManager.class);

    private NetCDFFrame                   frame0;
    private HashMap<Integer, NetCDFFrame> frameWindow;
    private static ArrayList<Integer>     availableFrameSequenceNumbers;

    private final int                     nThreads;
    private final PoolWorker[]            threads;
    private final LinkedList<Runnable>    queue;

    private final File                    ncfile;

    private Variable[]                    variables;

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

    public NetCDFDatasetManager(File file) {
        logger.debug("Opening dataset with initial file: " + file.getAbsolutePath());
        this.ncfile = file;

        this.nThreads = 5;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i = 0; i < nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].setPriority(Thread.MIN_PRIORITY);
            threads[i].start();
        }

        NetcdfFile ncfile = NetCDFUtil.open(file);

        try {
            Dimension[] yDims = NetCDFUtil.getUsedDimensionsBySubstring(ncfile, "lat");
            Dimension[] xDims = NetCDFUtil.getUsedDimensionsBySubstring(ncfile, "lon");

            variables = NetCDFUtil.getQualifyingVariables(ncfile, yDims, xDims);
        } catch (NetCDFNoSuchVariableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        resetModels();
    }

    public void resetModels() {
        NetCDFDatasetManager.availableFrameSequenceNumbers = new ArrayList<Integer>();

        File currentFile = NetCDFUtil.getSeqLowestFile(ncfile);
        while (currentFile != null) {
            int nr = NetCDFUtil.getFrameNumber(currentFile);
            availableFrameSequenceNumbers.add(nr);

            currentFile = NetCDFUtil.getSeqNextFile(currentFile);
        }

        this.frame0 = new NetCDFFrame(NetCDFUtil.getSeqLowestFile(ncfile), 0, variables);
        this.frameWindow = new HashMap<Integer, NetCDFFrame>();

    }

    private HashMap<Integer, NetCDFFrame> getWindow(int index) {
        HashMap<Integer, NetCDFFrame> newFrameWindow = new HashMap<Integer, NetCDFFrame>();

        for (int i = index - settings.getPreprocessAmount(); i < settings.getPreprocessAmount() + index; i++) {
            try {
                NetCDFFrame frame = null;
                if (i == 0) {
                    frame = frame0;
                } else if (frameWindow.containsKey(i)) {
                    frame = frameWindow.get(i);
                } else if (i > 0 && i < availableFrameSequenceNumbers.size()) {
                    frame = new NetCDFFrame(NetCDFUtil.getSeqFile(ncfile, availableFrameSequenceNumbers.get(i)), i,
                            variables);
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

    public int getFrameNumberOfIndex(int index) {
        return availableFrameSequenceNumbers.get(index);
    }

    public int getIndexOfFrameNumber(int frameNumber) {
        return availableFrameSequenceNumbers.indexOf(frameNumber);
    }

    public int getNumFiles() {
        return availableFrameSequenceNumbers.size();
    }
}
