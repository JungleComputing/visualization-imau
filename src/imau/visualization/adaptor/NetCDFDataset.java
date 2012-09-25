package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFDataset {
    private final static Logger              logger                        = LoggerFactory
                                                                                   .getLogger(NetCDFDataset.class);
    private final HashMap<String, Variable>  variables                     = new HashMap<String, Variable>();
    private final HashMap<String, Dimension> dimensions                    = new HashMap<String, Dimension>();
    private final ArrayList<Integer>         availableFrameSequenceNumbers = new ArrayList<Integer>();

    public NetCDFDataset(File initialFile) {
        File currentFile = NetCDFUtil.getSeqLowestFile(initialFile);
        while (currentFile != null) {
            int nr = NetCDFUtil.getFrameNumber(currentFile);
            availableFrameSequenceNumbers.add(nr);

            currentFile = NetCDFUtil.getSeqNextFile(currentFile);
        }

        try {
            NetcdfFile ncfile = NetcdfFile.open(initialFile.getAbsolutePath());
            List<Dimension> dims = ncfile.getDimensions();
            for (Dimension d : dims) {
                dimensions.put(d.getName(), d);
            }

            List<Variable> vars = ncfile.getVariables();
            for (Variable v : vars) {
                variables.put(v.getShortName(), v);
            }
        } catch (IOException ioe) {
            logger.error("trying to open " + initialFile.getAbsolutePath(), ioe);
        }
    }

    public int getNumFiles() {
        return availableFrameSequenceNumbers.size();
    }

    public List<String> getVariableShortNames() {
        ArrayList<String> vars = new ArrayList<String>();
        for (String s : variables.keySet()) {
            vars.add(s);
        }
        return vars;
    }

    public List<String> getDimensionNames() {
        ArrayList<String> dims = new ArrayList<String>();
        for (String s : dimensions.keySet()) {
            dims.add(s);
        }
        return dims;
    }
}
