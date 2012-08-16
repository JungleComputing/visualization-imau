package imau.visualization;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ImauInputHandler;

public class ImauApp {
    private final static ImauSettings settings = ImauSettings.getInstance();
    private final static Logger       log      = LoggerFactory
                                                       .getLogger(ImauApp.class);

    private static JFrame             frame;
    private static ImauPanel          imauPanel;
    private static ImauWindow         imauWindow;

    public static void main(String[] arguments) {
        String cmdlnfileName = null;
        String cmdlnfileName2 = null;
        String path = "";

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].equals("-o")) {
                i++;
                cmdlnfileName = arguments[i];
                final File cmdlnfile = new File(cmdlnfileName);
                path = cmdlnfile.getPath().substring(
                        0,
                        cmdlnfile.getPath().length()
                                - cmdlnfile.getName().length());
            } else if (arguments[i].equals("-o2")) {
                i++;
                cmdlnfileName2 = arguments[i];
            } else if (arguments[i].equals("-resume")) {
                i++;
                ImauApp.settings.setInitial_simulation_frame(Integer
                        .parseInt(arguments[i]));
                i++;
                ImauApp.settings.setInitial_rotation_x(Float
                        .parseFloat(arguments[i]));
                i++;
                ImauApp.settings.setInitial_rotation_y(Float
                        .parseFloat(arguments[i]));
            } else {
                cmdlnfileName = null;
                path = System.getProperty("user.dir");
            }
        }

        frame = new JFrame("Imau Visualization");
        frame.setPreferredSize(new Dimension(ImauApp.settings
                .getDefaultScreenWidth(), ImauApp.settings
                .getDefaultScreenHeight()));

        imauWindow = new ImauWindow(ImauInputHandler.getInstance(), true);
        imauPanel = new ImauPanel(imauWindow, path, cmdlnfileName,
                cmdlnfileName2);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    frame.getContentPane().add(imauPanel);

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent we) {
                            imauPanel.close();
                            System.exit(0);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });

        // Display the window.
        frame.pack();

        // center on screen
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    public static BufferedImage getFrameImage() {
        Component component = frame.getContentPane();
        BufferedImage image = new BufferedImage(component.getWidth(),
                component.getHeight(), BufferedImage.TYPE_INT_RGB);

        // call the Component's paint method, using
        // the Graphics object of the image.
        component.paint(image.getGraphics());

        return image;
    }

    public static Point getCanvaslocation() {
        return imauPanel.getCanvasLocation();
    }

    public static void getImage() {
        try {
            ImageIO.write(imauWindow.getScreenshot(), "png", new File(
                    "screenshot.png"));

            System.out.println("screenshot!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void log(String l, IOException ioe) {
        log.warn(l);
        log.debug(ioe.getMessage());
    }
}
