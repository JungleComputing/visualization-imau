package imau.visualization.adaptor;

public class TGridPoint {
    public final float t_lat;
    public final float t_lon;

    public final float t_depth;

    public final float ssh;
    public final float salinity;
    public final float temp;

    public TGridPoint(float t_lat, float t_lon, float t_depth, float ssh, float salinity, float temp) {
        this.t_lat = t_lat;
        this.t_lon = t_lon;

        this.t_depth = t_depth;

        this.ssh = ssh;
        this.salinity = salinity;
        this.temp = temp;
    }

    public String toString() {
        return "lat  : " + t_lat + "\n" + "lon  : " + t_lon + "\n" + "depth: " + t_depth + "\n" + "ssh: " + ssh + "\n"
                + "salt : " + salinity + "\n" + "temp : " + temp + "\n";
    }
}
