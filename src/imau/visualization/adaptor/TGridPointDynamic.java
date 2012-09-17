package imau.visualization.adaptor;

import imau.visualization.netcdf.NetCDFNoSuchVariableException;

import java.util.HashMap;

public class TGridPointDynamic {
    HashMap<String, Float> variables;

    public TGridPointDynamic() {
        variables = new HashMap<String, Float>();
    }

    public void setVariable(String name, float value) {
        variables.put(name, value);
    }

    public float getValue(String name) throws NetCDFNoSuchVariableException {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            throw new NetCDFNoSuchVariableException(
                    "No such variable in dynamic gridpoint");
        }
    }
}
