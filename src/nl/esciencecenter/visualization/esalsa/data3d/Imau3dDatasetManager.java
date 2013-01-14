package nl.esciencecenter.visualization.esalsa.data3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import nl.esciencecenter.visualization.esalsa.ImauSettings;
import nl.esciencecenter.visualization.esalsa.netcdf.NetCDFNoSuchVariableException;
import nl.esciencecenter.visualization.esalsa.netcdf.NetCDFUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

public class Imau3dDatasetManager {
    private final static Logger           logger   = LoggerFactory
                                                           .getLogger(Imau3dDatasetManager.class);
    private final ImauSettings            settings = ImauSettings.getInstance();

    private final ArrayList<Integer>      availableFrameSequenceNumbers;
    private final ArrayList<String>       availableVariables;
    private final HashMap<String, String> variableUnits;
    private final HashMap<String, String> variableFancyNames;

    private final IOPoolWorker[]          ioThreads;
    private final CPUPoolWorker[]         cpuThreads;
    private final LinkedList<Runnable>    cpuQueue;
    private final LinkedList<Runnable>    ioQueue;

    private final File                    file1;

    private final Texture3dStorage        texStorage;

    private final int                     imageHeight;
    private final int                     blankRows;

    private int                           latArraySize;
    private int                           lonArraySize;

    public void IOJobExecute(Runnable r) {
        synchronized (ioQueue) {
            ioQueue.addLast(r);
            ioQueue.notify();
        }
    }

    private class IOPoolWorker extends Thread {
        @Override
        public void run() {
            Imau3dDataArray netCDFArray;

            while (true) {
                synchronized (ioQueue) {
                    while (ioQueue.isEmpty()) {
                        try {
                            ioQueue.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    netCDFArray = (Imau3dDataArray) ioQueue.removeFirst();
                }

                // If we don't catch RuntimeException,
                // the pool could leak threads
                try {
                    netCDFArray.run();
                    CPUJobExecute(new SurfaceTexture3dBuilder(texStorage,
                            netCDFArray, imageHeight, blankRows));
                    CPUJobExecute(new LegendTexture3dBuilder(texStorage,
                            netCDFArray));
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

    public Imau3dDatasetManager(File file1, int numIOThreads, int numCPUThreads) {
        logger.debug("Opening dataset with initial file: "
                + file1.getAbsolutePath());
        this.file1 = file1;

        ioQueue = new LinkedList<Runnable>();
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

        availableFrameSequenceNumbers = new ArrayList<Integer>();

        File currentFile = NetCDFUtil.getSeqLowestFile(file1);
        while (currentFile != null) {
            int nr = NetCDFUtil.getFrameNumber(currentFile);
            availableFrameSequenceNumbers.add(nr);

            currentFile = NetCDFUtil.getSeqNextFile(currentFile);
        }

        availableVariables = new ArrayList<String>();
        variableUnits = new HashMap<String, String>();
        variableFancyNames = new HashMap<String, String>();

        NetcdfFile ncFile1 = NetCDFUtil.open(file1);

        try {
            ArrayList<String> latQualifiers1 = NetCDFUtil
                    .getUsedDimensionNamesBySubstring(ncFile1,
                            settings.getHeightSubstring());
            ArrayList<String> lonQualifiers1 = NetCDFUtil
                    .getUsedDimensionNamesBySubstring(ncFile1,
                            settings.getWidthSubstring());

            ArrayList<String> varQualifiers1 = NetCDFUtil.getVarNames(ncFile1,
                    latQualifiers1, lonQualifiers1);

            for (String s : varQualifiers1) {
                availableVariables.add(s);
                variableUnits.put(s, NetCDFUtil.getFancyVarUnits(ncFile1, s));
                variableFancyNames.put(s,
                        NetCDFUtil.getFancyVarName(ncFile1, s));
            }
        } catch (NetCDFNoSuchVariableException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        logger.debug("Found variables: ");
        for (String s : availableVariables) {
            logger.debug(s);
        }

        float latMin = -90f;
        float latMax = 90f;
        try {
            ArrayList<String> latQualifiers1 = NetCDFUtil
                    .getUsedDimensionNamesBySubstring(ncFile1,
                            settings.getHeightSubstring());
            ArrayList<String> lonQualifiers1 = NetCDFUtil
                    .getUsedDimensionNamesBySubstring(ncFile1,
                            settings.getWidthSubstring());

            int i = 0;

            latArraySize = 0;
            while (latArraySize == 0) {
                Array t_lat = NetCDFUtil
                        .getData(ncFile1, latQualifiers1.get(i));
                latArraySize = (int) t_lat.getSize();

                latMin = t_lat.getFloat(0) + 90f;
                latMax = t_lat.getFloat(latArraySize - 1) + 90f;

                i++;
            }

            i = 0;
            lonArraySize = 0;
            while (lonArraySize == 0) {
                Array t_lon = NetCDFUtil
                        .getData(ncFile1, lonQualifiers1.get(i));
                lonArraySize = (int) t_lon.getSize();
                i++;
            }
        } catch (NetCDFNoSuchVariableException e) {
            e.printStackTrace();
        }

        NetCDFUtil.close(ncFile1);

        imageHeight = (int) Math.floor((180f / (latMax - latMin))
                * latArraySize);
        blankRows = (int) Math.floor(180f / latMax);

        this.texStorage = new Texture3dStorage(this,
                settings.getNumScreensRows() * settings.getNumScreensCols(),
                lonArraySize, imageHeight);
    }

    public void buildImages(SurfaceTexture3dDescription desc) {
        int frameNumber = desc.getFrameNumber();

        if (frameNumber < 0
                || frameNumber >= availableFrameSequenceNumbers
                        .get(availableFrameSequenceNumbers.size() - 1)) {
            logger.warn("buildImages : Requested frameNumber  " + frameNumber
                    + " out of range.");
        }

        NetcdfFile frameFile1;
        try {
            frameFile1 = NetCDFUtil.open(NetCDFUtil.getSeqFile(file1,
                    frameNumber));
            IOJobExecute(new Imau3dDataArray(frameFile1, desc));
        } catch (IOException e) {
            logger.error("buildImages : Requested frameNumber " + frameNumber
                    + " resulted in IOException.");
            e.printStackTrace();
        }
    }

    public Texture3dStorage getTextureStorage() {
        return texStorage;
    }

    public int getFrameNumberOfIndex(int index) {
        return availableFrameSequenceNumbers.get(index);
    }

    public int getIndexOfFrameNumber(int frameNumber) {
        return availableFrameSequenceNumbers.indexOf(frameNumber);
    }

    public int getPreviousFrameNumber(int frameNumber) throws IOException {
        int nextNumber = getIndexOfFrameNumber(frameNumber) - 1;

        if (nextNumber >= 0
                && nextNumber < availableFrameSequenceNumbers.size()) {
            return getFrameNumberOfIndex(nextNumber);
        } else {
            throw new IOException("Frame number not available: " + nextNumber);
        }
    }

    public int getNextFrameNumber(int frameNumber) throws IOException {
        int nextNumber = getIndexOfFrameNumber(frameNumber) + 1;

        if (nextNumber >= 0
                && nextNumber < availableFrameSequenceNumbers.size()) {
            return getFrameNumberOfIndex(nextNumber);
        } else {
            throw new IOException("Frame number not available: " + nextNumber);
        }
    }

    public int getNumFiles() {
        return availableFrameSequenceNumbers.size();
    }

    public ArrayList<String> getVariables() {
        return availableVariables;
    }

    public String getVariableFancyName(String varName) {
        return variableFancyNames.get(varName);
    }

    public String getVariableUnits(String varName) {
        return variableUnits.get(varName);
    }

    public int getImageWidth() {
        return lonArraySize;
    }

    public int getImageHeight() {
        return imageHeight;
    }
}
