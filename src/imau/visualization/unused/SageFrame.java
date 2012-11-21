package imau.visualization.unused;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class SageFrame extends JFrame {
    private static final long serialVersionUID = -8833329921950038250L;

    public native int setup(int width, int height, int fps);

    public native int display(int[] rgb);

    boolean setupDone = false;

    public SageFrame(String frameName) {
        super(frameName);
    }

    @Override
    public void setVisible(boolean bool) {
        super.setVisible(bool);

        try {
            System.loadLibrary("sail");
            System.loadLibrary("quanta");
            System.loadLibrary("im-Linux-amd64");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load sage libraries.");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        setup(getWidth(), getHeight(), 10);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Component component = getContentPane();
        BufferedImage image = new BufferedImage(component.getWidth(),
                component.getHeight(), BufferedImage.TYPE_INT_RGB);

        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint(image.getGraphics());

        int w = getWidth(), h = getHeight();

        int[] rgbArray = new int[getWidth() * getHeight()];

        display(image.getRGB(0, 0, w, h, rgbArray, 0, w));

        display(rgbArray);
    }
}
