package imau.visualization.adaptor;

public class TGridPoint2 {
    public final float t_lat;
    public final float t_lon;

    public final float ssh;
    public final float shf;
    public final float sfwf;
    public final float hmxl;

    public DepthPoint[] depthPoints;

    public class DepthPoint {
        public final float depth;
        public final float salinity;
        public final float temp;

        public DepthPoint(float depth, float salinity, float temp) {
            this.depth = depth;
            this.salinity = salinity;
            this.temp = temp;
        }
    }

    public TGridPoint2(float t_lat, float t_lon, float ssh, float shf, float sfwf, float hmxl, DepthPoint[] depthPoints) {
        this.t_lat = t_lat;
        this.t_lon = t_lon;

        this.ssh = ssh;
        this.shf = shf;
        this.sfwf = sfwf;
        this.hmxl = hmxl;

        this.depthPoints = depthPoints;
    }
}
