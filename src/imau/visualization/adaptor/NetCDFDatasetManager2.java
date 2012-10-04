package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFNoSuchVariableException;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

public class NetCDFDatasetManager2 {
    private final static Logger           logger = LoggerFactory
                                                         .getLogger(NetCDFDatasetManager2.class);

    private static ArrayList<Integer>     availableFrameSequenceNumbers;

    private final IOPoolWorker[]          ioThreads;
    private final CPUPoolWorker[]         cpuThreads;
    private final LinkedList<Runnable>    cpuQueue;
    private final LinkedList<NetCDFArray> ioQueue;

    private final File                    ncfile1;
    private final File                    ncfile2;

    private final TextureStorage          texStorage;

    private final int                     imageHeight;
    private final int                     blankRows;

    public void IOJobExecute(NetCDFArray r) {
        synchronized (ioQueue) {
            ioQueue.addLast(r);
            ioQueue.notify();
        }
    }

    private class IOPoolWorker extends Thread {
        @Override
        public void run() {
            NetCDFArray netCDFArray;

            while (true) {
                synchronized (ioQueue) {
                    while (ioQueue.isEmpty()) {
                        try {
                            ioQueue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    netCDFArray = ioQueue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    netCDFArray.run();
                    CPUJobExecute(new SurfaceTexture(texStorage, netCDFArray,
                            imageHeight, blankRows));
                    CPUJobExecute(new LegendTexture(texStorage, netCDFArray));
                } catch (RuntimeException e) {
                    // You might want to log something here
                }
            }
        }
    }

    public void CPUJobExecute(Runnable r) {
        synchronized (cpuQueue) {
            cpuQueue.addLast(r);
            cpuQueue.notify();
        }
    }

    private class CPUPoolWorker extends Thread {
        @Override
        public void run() {
            Runnable r;

            while (true) {
                synchronized (cpuQueue) {
                    while (cpuQueue.isEmpty()) {
                        try {
                            cpuQueue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    r = cpuQueue.removeFirst();
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

    public NetCDFDatasetManager2(File file1, File file2, int numIOThreads,
            int numCPUThreads) {
        if (file2 == null) {
            logger.debug("Opening dataset with initial file: "
                    + file1.getAbsolutePath());
        } else {
            logger.debug("Opening dataset with initial files: "
                    + file1.getAbsolutePath() + " and "
                    + file1.getAbsolutePath());
        }
        this.ncfile1 = file1;
        this.ncfile2 = file2;
        this.texStorage = new TextureStorage(this, 900, 643);

        ioQueue = new LinkedList<NetCDFArray>();
        cpuQueue = new LinkedList<Runnable>();

        ioThreads = new IOPoolWorker[numIOThreads];
        cpuThreads = new CPUPoolWorker[numCPUThreads];

        for (int i = 0; i < numIOThreads; i++) {
            ioThreads[i] = new IOPoolWorker();
            ioThreads[i].setPriority(Thread.MIN_PRIORITY);
            ioThreads[i].start();
        }
        for (int i = 0; i < numIOThreads; i++) {
            cpuThreads[i] = new CPUPoolWorker();
            cpuThreads[i].setPriority(Thread.MIN_PRIORITY);
            cpuThreads[i].start();
        }

        NetCDFDatasetManager2.availableFrameSequenceNumbers = new ArrayList<Integer>();

        if (ncfile2 == null) {
            File currentFile = NetCDFUtil.getSeqLowestFile(ncfile1);
            while (currentFile != null) {
                int nr = NetCDFUtil.getFrameNumber(currentFile);
                availableFrameSequenceNumbers.add(nr);

                currentFile = NetCDFUtil.getSeqNextFile(currentFile);
            }
        } else {
            File currentFile1 = NetCDFUtil.getSeqLowestFile(ncfile1);
            int nr = NetCDFUtil.getFrameNumber(currentFile1);
            File currentFile2;
            try {
                currentFile2 = NetCDFUtil.getSeqFile(ncfile2, nr);
            } catch (IOException e) {
                currentFile2 = null;
            }

            while (currentFile1 != null && currentFile2 != null) {
                nr = NetCDFUtil.getFrameNumber(currentFile1);
                try {
                    currentFile2 = NetCDFUtil.getSeqFile(ncfile2, nr);
                } catch (IOException e) {
                    currentFile2 = null;
                    break;
                }

                availableFrameSequenceNumbers.add(nr);

                currentFile1 = NetCDFUtil.getSeqNextFile(currentFile1);
            }
        }

        Array t_lat;
        float latMin = -90f;
        float latMax = 90f;
        int arraySize = 602;
        try {
            t_lat = NetCDFUtil.getData(NetCDFUtil.open(ncfile1), "t_lat");
            arraySize = (int) t_lat.getSize();

            latMin = t_lat.getFloat(0) + 90f;
            latMax = t_lat.getFloat(arraySize - 1) + 90f;
        } catch (NetCDFNoSuchVariableException e) {
            e.printStackTrace();
        }

        imageHeight = (int) Math.floor((180f / (latMax - latMin)) * arraySize);
        blankRows = (int) Math.floor(180f / latMax);
    }

    public void buildImages(SurfaceTextureDescription desc) {
        if (desc.getFrameNumber() < availableFrameSequenceNumbers.size()) {
            int frameNumber = availableFrameSequenceNumbers.get(desc
                    .getFrameNumber());

            NetcdfFile frameFile1;
            try {
                if (!desc.isDiff()) {
                    if (desc.secondSet && ncfile2 != null) {
                        frameFile1 = NetCDFUtil.open(NetCDFUtil.getSeqFile(
                                ncfile2, frameNumber));
                    } else {
                        frameFile1 = NetCDFUtil.open(NetCDFUtil.getSeqFile(
                                ncfile1, frameNumber));
                    }
                    IOJobExecute(new NetCDFArray(frameFile1, desc));
                } else {
                    frameFile1 = NetCDFUtil.open(NetCDFUtil.getSeqFile(ncfile1,
                            frameNumber));
                    NetcdfFile frameFile2 = NetCDFUtil.open(NetCDFUtil
                            .getSeqFile(ncfile2, frameNumber));
                    IOJobExecute(new NetCDFArray(frameFile1, frameFile2, desc));
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public TextureStorage getTextureStorage() {
        return texStorage;
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
