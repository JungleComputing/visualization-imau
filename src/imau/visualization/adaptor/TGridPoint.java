package imau.visualization.adaptor;

public class TGridPoint {
    public final float t_lat;
    public final float t_lon;

    public final float t_depth;

    public final float ssh;
    public final float shf;
    public final float sfwf;
    public final float hmxl;

    public final float salinity;
    public final float temp;

    public final float uvel;
    public final float vvel;
    public final float ke;
    public final float pd;
    public final float taux;
    public final float tauy;
    public final float h2;

    public TGridPoint(float t_lat, float t_lon, float t_depth, float ssh,
            float shf, float sfwf, float hmxl, float salinity, float temp,
            float uvel, float vvel, float ke, float pd, float taux, float tauy,
            float h2) {
        this.t_lat = t_lat;
        this.t_lon = t_lon;

        this.t_depth = t_depth;

        this.ssh = ssh;
        this.shf = shf;
        this.sfwf = sfwf;
        this.hmxl = hmxl;

        this.salinity = salinity;
        this.temp = temp;

        this.uvel = uvel;
        this.vvel = vvel;
        this.ke = ke;
        this.pd = pd;
        this.taux = taux;
        this.tauy = tauy;
        this.h2 = h2;
    }
}
