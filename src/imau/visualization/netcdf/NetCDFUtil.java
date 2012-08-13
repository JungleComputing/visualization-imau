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

public class NetCDFUtil {
    private final static ImauSettings settings = ImauSettings.getInstance();
    private final static Logger       logger   = LoggerFactory
                                                       .getLogger(NetCDFUtil.class);

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

    public static String getPrefix(File file) {
        int numberLength = settings.getFileNumberLength();

        final String path = getPath(file);

        final String name = file.getName();
        final String fullPath = path + name;
        final String[] ext = fullPath.split("[.]");
        String prefix = "";
        for (int i = 0; i < ext.length - 1; i++) {
            prefix += ext[i] + ".";
        }

        return prefix.substring(0, prefix.length() - numberLength - 1);
    }

    public static String getPath(File file) {
        final String path = file.getPath().substring(0,
                file.getPath().length() - file.getName().length());
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
        final String[] ls = new File(path).list(new ExtFilter(settings
                .getCurrentExtension()));

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

    public static Array getDataSubset(NetcdfFile ncfile, String varName,
            String subsections) {
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
        String prefix = getPrefix(file);
        String postfix = settings.getCurrentExtension();

        String number = file.getPath().substring(prefix.length(),
                file.getPath().length() - postfix.length());

        return Integer.parseInt(number);
    }

    public static int getLowestFileNumber(File file) {
        String prefix = getPrefix(file);
        String postfix = ".nc";

        int result = -1;
        for (int i = 0; i < 100000; i++) {
            String number = String.format("%0" + settings.getFileNumberLength()
                    + "d", i);

            // System.out.println("Trying: " + prefix + number + postfix);
            File fileTry = new File(prefix + number + postfix);
            if (fileTry.exists())
                return i;
        }

        return result;
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

    public static File getPreviousFile(File file, int last) throws IOException {
        String prefix = getPrefix(file);
        String postfix = settings.getCurrentExtension();

        File result = null;
        for (int i = last + 1; i >= 0; i--) {
            String number = String.format("%0" + settings.getFileNumberLength()
                    + "d", i);

            // System.out.println("Trying: " + prefix + number + postfix);
            result = new File(prefix + number + postfix);
            if (result.exists()) {
                return result;
            }
        }

        throw new IOException("No such file.");
    }

    public static File getFile(File file, int value) throws IOException {
        String prefix = getPrefix(file);
        String postfix = settings.getCurrentExtension();
        String number = String.format("%0" + settings.getFileNumberLength()
                + "d", value);

        File result = new File(prefix + number + postfix);
        if (result.exists()) {
            return result;
        } else {
            throw new IOException("No such file.");
        }
    }

    public static File getNextFile(File file, int last) throws IOException {
        String prefix = getPrefix(file);
        String postfix = settings.getCurrentExtension();

        File result = null;
        for (int i = last + 1; i < 100000; i++) {
            String number = String.format("%0" + settings.getFileNumberLength()
                    + "d", i);

            // System.out.println("Trying: " + prefix + number + postfix);
            result = new File(prefix + number + postfix);
            if (result.exists()) {
                return result;
            }
        }

        throw new IOException("No such file.");
    }

}
