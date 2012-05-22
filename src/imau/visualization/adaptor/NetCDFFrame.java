package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;

public class NetCDFFrame implements Runnable {
    private int frameNumber;

    private boolean initialized, doneProcessing;

    private boolean error;
    private String errMessage;

    private File initialFile;

    public NetCDFFrame(int frameNumber, File ncfile) {
        this.frameNumber = frameNumber;
        this.initialFile = ncfile;

        this.initialized = false;
        this.doneProcessing = false;

        this.error = false;
        this.errMessage = "";
    }

    public int getNumber() {
        return frameNumber;
    }

    public synchronized void init() {
        if (!initialized) {
            try {
                File myFile = NetCDFUtil.getFile(initialFile, frameNumber);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            }
        }
    }

    public synchronized void process() {
        if (!initialized) {
            init();
        }
        doneProcessing = true;
    }

    @Override
    public void run() {
        init();
        process();
    }

    public boolean isError() {
        return error;
    }

}
