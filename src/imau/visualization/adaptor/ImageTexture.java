package imau.visualization.adaptor;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import openglCommon.textures.Texture2D;

public class ImageTexture extends Texture2D {
    public ImageTexture(String filename, int glMultiTexUnit) {
        super(glMultiTexUnit);

        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int x = 0, y = 0, w = bi.getWidth(), h = bi.getHeight();

        this.width = w;
        this.height = h;

        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(bi, x, y, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return;
        }

        ByteBuffer tempBuffer = ByteBuffer.allocate(w * h * 4);

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {

                tempBuffer.put((byte) ((pixels[j * w + i] >> 16) & 0xff)); // red
                tempBuffer.put((byte) ((pixels[j * w + i] >> 8) & 0xff)); // green
                tempBuffer.put((byte) ((pixels[j * w + i]) & 0xff)); // blue
                tempBuffer.put((byte) ((pixels[j * w + i] >> 24) & 0xff)); // alpha
            }
        }

        tempBuffer.flip();

        pixelBuffer = tempBuffer;
    }
}