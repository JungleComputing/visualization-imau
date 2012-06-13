package imau.visualization.adaptor;

import imau.visualization.ImauSettings;
import imau.visualization.ImauSettings.varNames;
import imau.visualization.netcdf.NetCDFUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import javax.media.opengl.GL3;

import openglCommon.textures.Texture2D;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFrame implements Runnable {
    private ImauSettings settings = ImauSettings.getInstance();

    private varNames redBand = settings.getRedBand();
    private varNames greenBand = settings.getGreenBand();
    private varNames blueBand = settings.getBlueBand();

    private int selectedDepth = settings.getDepthDef();

    private int frameNumber;

    private boolean initialized, doneProcessing;

    private boolean error;
    private String errMessage;

    private int pixels, width, height;
    private TGridPoint[] tGridPoints;
    private Texture2D image;

    private File initialFile;

    private float epsilon = settings.getEpsilon();

    float latMin, latMax;

    private DataTexture dataTex;

    private HashMap<BandCombination, Texture2D> storedTextures;

    public NetCDFFrame(int frameNumber, File initialFile) {
        this.frameNumber = frameNumber;
        this.initialFile = initialFile;

        this.initialized = false;
        this.doneProcessing = false;

        this.error = false;
        this.errMessage = "";

        storedTextures = new HashMap<BandCombination, Texture2D>();
    }

    public int getNumber() {
        return frameNumber;
    }

    public synchronized void init() {
        if (!initialized) {
            try {
                // Open the correct file as a NetCDF specific file.
                File myFile = NetCDFUtil.getFile(initialFile, frameNumber);
                NetcdfFile ncfile = NetCDFUtil.open(myFile);

                // Read data
                Array t_lat = NetCDFUtil.getData(ncfile, "t_lat");
                Array t_lon = NetCDFUtil.getData(ncfile, "t_lon");
                int tlat_dim = 0;
                int tlon_dim = 0;

                Array t_depth = NetCDFUtil.getData(ncfile, "depth_t");
                int tdepth_dim = 0;

                // Array u_lat = NetCDFUtil.getData(ncfile, "u_lat");
                // Array u_lon = NetCDFUtil.getData(ncfile, "u_lon");
                // int ulat_dim = 0;
                // int ulon_dim = 0;

                List<Dimension> dims = ncfile.getDimensions();
                for (Dimension d : dims) {
                    if (d.getName().compareTo("t_lat") == 0) {
                        tlat_dim = d.getLength();
                    } else if (d.getName().compareTo("t_lon") == 0) {
                        tlon_dim = d.getLength();
                    } else if (d.getName().compareTo("depth_t") == 0) {
                        tdepth_dim = d.getLength();
                        // } else if (d.getName().compareTo("u_lat") == 0) {
                        // ulat_dim = d.getLength();
                        // } else if (d.getName().compareTo("u_lon") == 0) {
                        // ulon_dim = d.getLength();
                    }
                }

                this.height = tlat_dim;
                this.width = tlon_dim;

                latMin = t_lat.getFloat(0) + 90f;
                latMax = t_lat.getFloat(height - 1) + 90f;
                // float span = latMax - latMin;
                //
                // System.out.println("latMin " + latMin);
                // System.out.println("latMax " + latMax);
                //
                // HashMap<Integer, Float> latitudes = new HashMap<Integer,
                // Float>();
                // for (int i = 0; i < tlat_dim; i++) {
                // float lat = t_lat.getFloat(i) + 90f;
                // latitudes.put(i, lat);
                // }
                //
                // FloatBuffer tempBuffer = FloatBuffer.allocate(100 * 100);
                // for (float row = 0f; row < 100f; row++) {
                // float value = (row / 100) * 180f;
                //
                // int floor = getFlooredIndex(latitudes, value);
                // float lat = t_lat.getFloat(floor) + 90f;
                //
                // if (lat > latMin && lat < latMax) {
                // if (frameNumber == 75) {
                // System.out.println("i: " + value + " : " + floor + " : " +
                // lat);
                // }
                //
                // for (float col = 0f; col < 100f; col++) {
                // tempBuffer.put((lat / 180f));
                // }
                // } else {
                // if (frameNumber == 75) {
                // System.out.println("i: " + value + " : -1 : -1");
                // }
                // for (float col = 0f; col < 100f; col++) {
                // tempBuffer.put(-1f);
                // }
                // }
                // }
                // tempBuffer.rewind();
                //
                // dataTex = new DataTexture(GL3.GL_TEXTURE11, tempBuffer, 100,
                // 100);

                Variable vssh = ncfile.findVariable("SSH");
                Variable vshf = ncfile.findVariable("SHF");
                Variable vsfwf = ncfile.findVariable("SFWF");
                Variable vhmxl = ncfile.findVariable("HMXL");

                Variable vsalt = ncfile.findVariable("SALT");
                Variable vtemp = ncfile.findVariable("TEMP");

                Array ssh = vssh.read();
                Array shf = vshf.read();
                Array sfwf = vsfwf.read();
                Array hmxl = vhmxl.read();

                this.pixels = tlat_dim * tlon_dim;

                tGridPoints = new TGridPoint[pixels];

                int[] origin = new int[] { 0, 0, 0 };
                int[] size = new int[] { 1, tlat_dim, tlon_dim };

                // for (int tdepth_i = 0; tdepth_i < tdepth_dim; tdepth_i++) {
                int tdepth_i = selectedDepth;
                float tdepth = t_depth.getFloat(tdepth_i);

                origin[0] = tdepth_i;
                Array salt = vsalt.read(origin, size).reduce(0);
                Array temp = vtemp.read(origin, size).reduce(0);

                Index ssh_index = ssh.getIndex();
                Index shf_index = shf.getIndex();
                Index sfwf_index = sfwf.getIndex();
                Index hmxl_index = hmxl.getIndex();

                Index salt_index = salt.getIndex();
                Index temp_index = temp.getIndex();

                for (int tlon_i = 0; tlon_i < tlon_dim; tlon_i++) {
                    float tlon = t_lon.getFloat(tlon_i);

                    for (int tlat_i = 0; tlat_i < tlat_dim; tlat_i++) {
                        float tlat = t_lat.getFloat(tlat_i);

                        ssh_index.set(tlat_i, tlon_i);
                        shf_index.set(tlat_i, tlon_i);
                        sfwf_index.set(tlat_i, tlon_i);
                        hmxl_index.set(tlat_i, tlon_i);

                        salt_index.set(tlat_i, tlon_i);
                        temp_index.set(tlat_i, tlon_i);

                        float seaHeight = ssh.getFloat(ssh_index);
                        float surfHeatFlux = shf.getFloat(shf_index);
                        float saltFlux = sfwf.getFloat(sfwf_index);
                        float mixedLayerDepth = hmxl.getFloat(hmxl_index);

                        float salinity = salt.getFloat(salt_index);
                        float temperature = temp.getFloat(temp_index);

                        tGridPoints[(tlat_i * tlon_dim) + tlon_i] = new TGridPoint(tlat, tlon, tdepth, seaHeight,
                                surfHeatFlux, saltFlux, mixedLayerDepth, salinity, temperature);
                    }
                }
                // }

                NetCDFUtil.close(ncfile);
            } catch (IOException e) {
                error = true;
                errMessage = e.getMessage();
            } catch (InvalidRangeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            initialized = true;
        }
    }

    private int getFlooredIndex(HashMap<Integer, Float> map, float latitude) {
        int index = 0;

        while (index < map.size()) {
            float value = map.get(index);

            if (value < latitude) {
                index++;
            } else {
                break;
            }
        }

        return index;
    }

    private float getFlooredLatitude(HashMap<Integer, Float> map, int index) {
        float result = 0f;

        while (index > 0) {
            if (map.containsKey(index)) {
                result = map.get(index);
            }
            index--;
        }

        return result;
    }

    public DataTexture getTexCoords() {
        return dataTex;
    }

    public Texture2D getImage() {
        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = settings.getDepthDef();
            initialized = false;
            doneProcessing = false;
            init();
        }
        if (settings.getRedBand() != redBand) {
            redBand = settings.getRedBand();
            doneProcessing = false;
        }
        if (settings.getGreenBand() != greenBand) {
            greenBand = settings.getGreenBand();
            doneProcessing = false;
        }
        if (settings.getBlueBand() != blueBand) {
            blueBand = settings.getBlueBand();
            doneProcessing = false;
        }

        if (!doneProcessing) {
            process();
        }

        return image;
    }

    public Texture2D getImage2(int glMultitexNumber, BandCombination bandComboLT) {
        return getImage2(glMultitexNumber, bandComboLT.redBand, bandComboLT.greenBand, bandComboLT.blueBand);
    }

    public Texture2D getImage2(int glMultitexNumber, varNames redBand, varNames greenBand, varNames blueBand) {
        if (settings.getDepthDef() != selectedDepth) {
            selectedDepth = settings.getDepthDef();
            initialized = false;
        }

        if (!initialized) {
            init();
        }

        BandCombination combo = new BandCombination(selectedDepth, redBand, greenBand, blueBand);

        if (storedTextures.containsKey(combo)) {
            return storedTextures.get(combo);
        } else {
            int newHeight = (int) Math.floor((180f / ((float) latMax - (float) latMin)) * (float) height);

            int newStart = (int) Math.floor(180f / latMin);
            int newStop = (int) Math.floor(180f / latMax);

            int newPixels = newHeight * width;

            ByteBuffer outBuf = ByteBuffer.allocate(newPixels * 4);
            outBuf.clear();
            outBuf.rewind();

            float max = Float.MIN_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].temp;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
            // System.out.println("MAX: " + max);
            // System.out.println("MIN: " + min);

            float max_ssh = settings.getMaxSsh();
            float min_ssh = settings.getMinSsh();
            float diff_ssh = max_ssh - min_ssh;

            float max_shf = settings.getMaxShf();
            float min_shf = settings.getMinShf();
            float diff_shf = max_shf - min_shf;

            float max_sfwf = settings.getMaxSfwf();
            float min_sfwf = settings.getMinSfwf();
            float diff_sfwf = max_sfwf - min_sfwf;

            float max_hmxl = settings.getMaxHmxl();
            float min_hmxl = settings.getMinHmxl();
            float diff_hmxl = max_hmxl - min_hmxl;

            float max_salt = settings.getMaxSalt();
            float min_salt = settings.getMinSalt();
            float diff_salt = max_salt - min_salt;

            float max_temp = settings.getMaxTemp();
            float min_temp = settings.getMinTemp();
            float diff_temp = max_temp - min_temp;

            for (int i = newHeight - newStop; i < newHeight; i++) {
                for (int w = 0; w < width; w++) {
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                }
            }

            for (int row = height - 1; row >= 0; row--) {
                // for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int i = (row * width + col);

                    byte red = 0, green = 0, blue = 0;

                    if (redBand == varNames.SSH) {
                        red = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (redBand == varNames.SHF) {
                        red = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (redBand == varNames.SFWF) {
                        red = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (redBand == varNames.HMXL) {
                        red = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (redBand == varNames.SALT) {
                        red = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (redBand == varNames.TEMP) {
                        red = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    if (greenBand == varNames.SSH) {
                        green = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (greenBand == varNames.SHF) {
                        green = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (greenBand == varNames.SFWF) {
                        green = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (greenBand == varNames.HMXL) {
                        green = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (greenBand == varNames.SALT) {
                        green = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (greenBand == varNames.TEMP) {
                        green = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    if (blueBand == varNames.SSH) {
                        blue = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (blueBand == varNames.SHF) {
                        blue = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (blueBand == varNames.SFWF) {
                        blue = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (blueBand == varNames.HMXL) {
                        blue = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (blueBand == varNames.SALT) {
                        blue = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (blueBand == varNames.TEMP) {
                        blue = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    outBuf.put(red);
                    outBuf.put(green);
                    outBuf.put(blue);
                    outBuf.put((byte) 0xFF);
                }
            }

            while (outBuf.hasRemaining()) {
                outBuf.put((byte) 0);
            }

            outBuf.flip();

            Texture2D image = new NetCDFTexture(glMultitexNumber, outBuf, width, newHeight);

            storedTextures.put(combo, image);
            return image;
        }
    }

    public synchronized void process() {
        if (!initialized) {
            init();
        }

        if (!doneProcessing) {
            int newHeight = (int) Math.floor((180f / ((float) latMax - (float) latMin)) * (float) height);

            int newStart = (int) Math.floor(180f / latMin);
            int newStop = (int) Math.floor(180f / latMax);

            int newPixels = newHeight * width;

            ByteBuffer outBuf = ByteBuffer.allocate(newPixels * 4);
            outBuf.clear();
            outBuf.rewind();

            float max = Float.MIN_VALUE;
            float min = Float.MAX_VALUE;
            for (int i = 0; i < pixels; i++) {
                float val = tGridPoints[i].temp;
                if (val > max) {
                    max = val;
                }
                if (val < min && val > -1E33) {
                    min = val;
                }
            }
            // System.out.println("MAX: " + max);
            // System.out.println("MIN: " + min);

            float max_ssh = settings.getMaxSsh();
            float min_ssh = settings.getMinSsh();
            float diff_ssh = max_ssh - min_ssh;

            float max_shf = settings.getMaxShf();
            float min_shf = settings.getMinShf();
            float diff_shf = max_shf - min_shf;

            float max_sfwf = settings.getMaxSfwf();
            float min_sfwf = settings.getMinSfwf();
            float diff_sfwf = max_sfwf - min_sfwf;

            float max_hmxl = settings.getMaxHmxl();
            float min_hmxl = settings.getMinHmxl();
            float diff_hmxl = max_hmxl - min_hmxl;

            float max_salt = settings.getMaxSalt();
            float min_salt = settings.getMinSalt();
            float diff_salt = max_salt - min_salt;

            float max_temp = settings.getMaxTemp();
            float min_temp = settings.getMinTemp();
            float diff_temp = max_temp - min_temp;

            for (int i = newHeight - newStop; i < newHeight; i++) {
                for (int w = 0; w < width; w++) {
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                    outBuf.put((byte) 0);
                }
            }

            for (int row = height - 1; row >= 0; row--) {
                // for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int i = (row * width + col);

                    byte red = 0, green = 0, blue = 0;

                    if (redBand == varNames.SSH) {
                        red = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (redBand == varNames.SHF) {
                        red = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (redBand == varNames.SFWF) {
                        red = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (redBand == varNames.HMXL) {
                        red = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (redBand == varNames.SALT) {
                        red = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (redBand == varNames.TEMP) {
                        red = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    if (greenBand == varNames.SSH) {
                        green = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (greenBand == varNames.SHF) {
                        green = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (greenBand == varNames.SFWF) {
                        green = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (greenBand == varNames.HMXL) {
                        green = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (greenBand == varNames.SALT) {
                        green = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (greenBand == varNames.TEMP) {
                        green = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    if (blueBand == varNames.SSH) {
                        blue = calc(min_ssh, diff_ssh, tGridPoints[i].ssh);
                    } else if (blueBand == varNames.SHF) {
                        blue = calc(min_shf, diff_shf, tGridPoints[i].shf);
                    } else if (blueBand == varNames.SFWF) {
                        blue = calc(min_sfwf, diff_sfwf, tGridPoints[i].sfwf);
                    } else if (blueBand == varNames.HMXL) {
                        blue = calc(min_hmxl, diff_hmxl, tGridPoints[i].hmxl);
                    } else if (blueBand == varNames.SALT) {
                        blue = calc(min_salt, diff_salt, tGridPoints[i].salinity);
                    } else if (blueBand == varNames.TEMP) {
                        blue = calc(min_temp, diff_temp, tGridPoints[i].temp);
                    }

                    outBuf.put(red);
                    outBuf.put(green);
                    outBuf.put(blue);
                    outBuf.put((byte) 0xFF);
                }
            }

            while (outBuf.hasRemaining()) {
                outBuf.put((byte) 0);
            }

            outBuf.flip();

            this.image = new NetCDFTexture(GL3.GL_TEXTURE1, outBuf, width, newHeight);
            // this.image = new ImageTexture("textures/earth_flat_map.jpg",
            // GL3.GL_TEXTURE1);

            doneProcessing = true;
        }

    }

    private byte calc(float min, float diff, float var) {
        float result = (var - min) / diff;
        if (result < epsilon) {
            result = 0;
        }

        return (byte) (result * 255);
    }

    @Override
    public void run() {
        init();
        process();
    }

    public boolean isError() {
        init();

        return error;
    }

    public String getError() {
        return errMessage;
    }

}
