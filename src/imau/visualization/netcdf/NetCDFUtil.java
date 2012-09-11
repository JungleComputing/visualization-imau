package imau.visualization.netcdf;

import imau.visualization.ImauSettings;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

//bla

public class NetCDFUtil {
    private final static ImauSettings settings = ImauSettings.getInstance();
    private final static Logger logger = LoggerFactory.getLogger(NetCDFUtil.class);

    static class ExtFilter implements FilenameFilter {
        private final String ext;

        public ExtFilter(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(ext));
        }
    }

    private static String getSequenceNumber(File file) {
        final String path = getPath(file);
        final String name = file.getName();
        final String fullPath = path + name;

        String[] split = fullPath.split("[^0-9]");

        boolean foundOne = false;
        String sequenceNumberString = "";
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            try {
                Integer.parseInt(s);
                if (s.length() > 3) {
                    sequenceNumberString = s;
                    if (!foundOne) {
                        foundOne = true;
                    } else {
                        System.err.println("ERROR: Filename includes two possible sequence numbers.");
                    }
                }
            } catch (NumberFormatException e) {
                // IGNORE
            }
        }

        return sequenceNumberString;
    }

    public static String getPrefix(File file) {
        final String path = getPath(file);
        final String name = file.getName();
        final String fullPath = path + name;

        String seqNum = getSequenceNumber(file);
        int index = fullPath.lastIndexOf(seqNum);

        return fullPath.substring(0, index);
    }

    public static String getPostfix(File file) {
        final String path = getPath(file);
        final String name = file.getName();
        final String fullPath = path + name;

        String seqNum = getSequenceNumber(file);
        int index = fullPath.lastIndexOf(seqNum) + seqNum.length();

        return fullPath.substring(index);
    }

    public static String getPath(File file) {
        final String path = file.getPath().substring(0, file.getPath().length() - file.getName().length());
        return path;
    }

    public static NetcdfFile open(String filename) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(filename);
        } catch (IOException ioe) {
            log("trying to open " + filename, ioe);
        }

        return ncfile;
    }

    public static NetcdfFile open(File file) {
        NetcdfFile ncfile = null;
        try {
            ncfile = NetcdfFile.open(file.getAbsolutePath());
        } catch (IOException ioe) {
            log("trying to open " + file.getAbsolutePath(), ioe);
        }

        return ncfile;
    }

    public static void close(NetcdfFile ncfile) {
        try {
            ncfile.close();
        } catch (IOException ioe) {
            log("trying to close file.", ioe);
        }
    }

    public static int getNumColorMaps() {
        final String[] ls = new File("colormaps").list(new ExtFilter("ncmap"));

        return ls.length;
    }

    public static String[] getColorMaps() {
        final String[] ls = new File("colormaps").list(new ExtFilter("ncmap"));
        final String[] result = new String[ls.length];

        for (int i = 0; i < ls.length; i++) {
            result[i] = ls[i].split("\\.")[0];
        }

        return result;
    }

    public static int getNumFiles(File file) {
        final String path = getPath(file);
        final String[] ls = new File(path).list(new ExtFilter(settings.getCurrentExtension()));

        return ls.length;
    }

    public static int getNumFiles(String pathName, String ext) {
        final String[] ls = new File(pathName).list(new ExtFilter(ext));

        return ls.length;
    }

    public static void printInfo(NetcdfFile ncfile) {
        System.out.println(ncfile.getDetailInfo());
    }

    public static Array getData(NetcdfFile ncfile, String varName) {
        Variable v = ncfile.findVariable(varName);
        Array data = null;
        if (null == v)
            return null;
        try {
            data = v.read();
        } catch (IOException ioe) {
            log("trying to read " + varName, ioe);
        }

        return data;
    }

    public static Array getDataSubset(NetcdfFile ncfile, String varName, String subsections) {
        Variable v = ncfile.findVariable(varName);
        Array data = null;
        if (null == v)
            return null;
        try {
            data = v.read(subsections);
        } catch (IOException ioe) {
            log("trying to read " + varName, ioe);
        } catch (InvalidRangeException e) {
            log("invalid Range for " + varName, e);
        }

        return data;
    }

    private static void log(String l, Exception e) {
        logger.error("Error : " + l);
        logger.debug(e.getMessage());
    }

    public static int getFrameNumber(File file) {
        String number = getSequenceNumber(file);

        return Integer.parseInt(number);
    }

    public static File getSeqLowestFile(File initialFile) {
        String prefix = getPrefix(initialFile);
        String postfix = getPostfix(initialFile);

        int numberLength = getSequenceNumber(initialFile).length();

        String format = "%0" + numberLength + "d";

        for (int i = 0; i < 100000; i++) {
            String number = String.format(format, i);

            File fileTry = new File(prefix + number + postfix);
            if (fileTry.exists())
                return fileTry;
        }

        return null;
    }

    public static boolean isAcceptableFile(File file) {
        String[] accExts = settings.getAcceptableExtensions();
        final String path = getPath(file);
        final String name = file.getName();
        final String fullPath = path + name;
        final String[] ext = fullPath.split("[.]");

        boolean result = false;
        for (int i = 0; i < accExts.length; i++) {
            if (ext[ext.length - 1].compareTo(accExts[i]) != 0) {
                result = true;
            }
        }

        return result;
    }

    public static File getSeqPreviousFile(File initialFile) {
        String prefix = getPrefix(initialFile);
        String postfix = getPostfix(initialFile);

        int initialNumber = Integer.parseInt(getSequenceNumber(initialFile));
        int numberLength = getSequenceNumber(initialFile).length();

        String format = "%0" + numberLength + "d";

        String number = String.format(format, initialNumber - 1);

        File fileTry = new File(prefix + number + postfix);
        if (fileTry.exists())
            return fileTry;

        return null;
    }

    public static File getSeqFile(File initialFile, int value) throws IOException {
        String prefix = getPrefix(initialFile);
        String postfix = getPostfix(initialFile);

        int numberLength = getSequenceNumber(initialFile).length();

        String format = "%0" + numberLength + "d";

        String number = String.format(format, value);

        File fileTry = new File(prefix + number + postfix);
        if (fileTry.exists())
            return fileTry;

        return null;
    }

    public static File getSeqNextFile(File initialFile) {
        String prefix = getPrefix(initialFile);
        String postfix = getPostfix(initialFile);

        int initialNumber = Integer.parseInt(getSequenceNumber(initialFile));
        int numberLength = getSequenceNumber(initialFile).length();

        String format = "%0" + numberLength + "d";

        String number = String.format(format, initialNumber + 1);

        File fileTry = new File(prefix + number + postfix);
        if (fileTry.exists())
            return fileTry;

        return null;
    }

}
