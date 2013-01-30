package nl.esciencecenter.visualization.esalsa;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import nl.esciencecenter.visualization.esalsa.util.ImauInputHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;

public class ImauApp {
    private final static ImauSettings settings  = ImauSettings.getInstance();
    private final static Logger       log       = LoggerFactory
                                                        .getLogger(ImauApp.class);

    private static JFrame             frame;
    private static ImauPanel          imauPanel;
    private static ImauWindow         imauWindow;

    static int                        screenIdx = 0;

    private static void createScreen(boolean forceGL3, String path,
            String cmdlnfileName, String cmdlnfileName2) {
        final GLProfile glp;
        if (forceGL3) {
            glp = GLProfile.get(GLProfile.GL3);
        } else {
            glp = GLProfile.get(GLProfile.GLES2);
        }

        // Set up the GL context
        final GLCapabilities caps = new GLCapabilities(glp);
        caps.setBackgroundOpaque(true);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(true);

        // Add Anti-Aliasing
        caps.setSampleBuffers(true);
        caps.setAlphaBits(4);
        caps.setNumSamples(4);
                
        // Create the Newt Window and AWT canvas
        Display dpy = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(dpy, screenIdx);

        final GLWindow glWindow = GLWindow.create(screen, caps);
        final NewtCanvasAWT canvas = new NewtCanvasAWT(glWindow);

        // Create the Swing interface elements
        imauPanel = new ImauPanel(canvas, path, cmdlnfileName, cmdlnfileName2);

        // Create the GLEventListener
        imauWindow = new ImauWindow(ImauInputHandler.getInstance(), true);

        // Add listeners
        glWindow.addMouseListener(imauWindow.getInputHandler());
        glWindow.addKeyListener(imauWindow.getInputHandler());
        glWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent arg0) {
                System.exit(0);
            }
        });
        glWindow.addGLEventListener(imauWindow);

        // Create the Animator
        final Animator animator = new Animator();
        animator.add(glWindow);

        // Create the frame
        final JFrame frame = new JFrame("eSalsa Visualization");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setSize(
                ImauApp.settings.getDefaultScreenWidth()
                        + ImauApp.settings.getDefaultScreenWidthExtension(),
                ImauApp.settings.getDefaultScreenHeight()
                        + ImauApp.settings.getDefaultScreenHeightExtension());

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    frame.getContentPane().add(imauPanel);
                    animator.start();
                } catch (final Exception e) {
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            }
        });
        
        if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0) {
        	System.out.println("Crappy OS detected, switching to lame 2-frame mode");
	        glWindow.setSize(	ImauApp.settings.getDefaultScreenWidth(),
	                			ImauApp.settings.getDefaultScreenHeight());
	       
	        glWindow.setVisible(true);
        }

        frame.setVisible(true);  
    }

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

        // frame = new JFrame("Imau Visualization");
        // frame.setPreferredSize(new Dimension(ImauApp.settings
        // .getDefaultScreenWidth()
        // + ImauApp.settings.getDefaultScreenWidthExtension(),
        // ImauApp.settings.getDefaultScreenHeight()
        // + ImauApp.settings.getDefaultScreenHeightExtension()));

        // imauWindow = new ImauWindow(ImauInputHandler.getInstance(), true);
        createScreen(true, path, cmdlnfileName, cmdlnfileName2);

        // javax.swing.SwingUtilities.invokeLater(new Runnable() {
        // @Override
        // public void run() {
        // try {
        // frame.getContentPane().add(imauPanel);
        //
        // // frame.addWindowListener(new WindowAdapter() {
        // //
        // // public void windowClosing(WindowEvent we) {
        // // imauPanel.close();
        // // System.exit(0);
        // // }
        // // });
        // } catch (final Exception e) {
        // e.printStackTrace(System.err);
        // System.exit(1);
        // }
        // }
        // });
        //
        // // center on screen
        // frame.setLocationRelativeTo(null);
        //
        // // Display the window.
        // frame.pack();
        // frame.setVisible(true);

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

    public static Dimension getFrameSize() {
        return frame.getContentPane().getSize();
    }

    public static Point getCanvaslocation() {
        return imauPanel.getCanvasLocation();
    }

    public static void feedMouseEventToPanel(int x, int y) {
        Point p = new Point(x, y);
        SwingUtilities.convertPointFromScreen(p, frame.getContentPane());

        System.out.println("x " + x + " y " + y);
        System.out.println("p.x " + p.x + " p.y " + p.y);

        if ((p.x > 0 && p.x < frame.getWidth())
                && (p.y > 0 && p.y < frame.getHeight())) {
            Component comp = SwingUtilities.getDeepestComponentAt(
                    frame.getContentPane(), p.x, p.y);

            System.out.println(comp.toString());

            Toolkit.getDefaultToolkit()
                    .getSystemEventQueue()
                    .postEvent(
                            new MouseEvent(comp, MouseEvent.MOUSE_PRESSED, 0,
                                    0, p.x, p.y, 1, false));
            Toolkit.getDefaultToolkit()
                    .getSystemEventQueue()
                    .postEvent(
                            new MouseEvent(comp, MouseEvent.MOUSE_RELEASED, 0,
                                    0, p.x, p.y, 1, false));
            Toolkit.getDefaultToolkit()
                    .getSystemEventQueue()
                    .postEvent(
                            new MouseEvent(comp, MouseEvent.MOUSE_CLICKED, 0,
                                    0, p.x, p.y, 1, false));
        }
    }
}
