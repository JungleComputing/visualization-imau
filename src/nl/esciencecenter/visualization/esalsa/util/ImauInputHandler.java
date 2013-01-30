package nl.esciencecenter.visualization.esalsa.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import nl.esciencecenter.visualization.esalsa.ImauSettings;
import nl.esciencecenter.visualization.openglCommon.math.VecF3;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

public class ImauInputHandler implements TouchEventHandler, MouseListener,
        KeyListener {
    private final ImauSettings settings = ImauSettings.getInstance();

    public static enum octants {
        PPP, PPN, PNP, PNN, NPP, NPN, NNP, NNN
    }

    protected VecF3           rotation;
    protected float           viewDist            = -150f;

    protected float           rotationXorigin     = 0;
    protected float           rotationX;

    protected float           rotationYorigin     = 0;
    protected float           rotationY;

    protected float           dragLeftXorigin;
    protected float           dragLeftYorigin;

    private octants           current_view_octant = octants.PPP;

    private Socket            touchSocket;
    private ConnectionHandler touchConnection;

    protected float           initialResizeDist;

    int                       currentTouchID      = 0;

    private static class SingletonHolder {
        public static final ImauInputHandler instance = new ImauInputHandler();
    }

    public static ImauInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ImauInputHandler() {
        rotation = new VecF3();

        try {
            if (settings.isTouchConnected()) {
                touchSocket = new Socket("145.100.39.13", 12345);

                this.touchConnection = new ConnectionHandler(this, touchSocket);
                new Thread(touchConnection).start();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (touchConnection != null) {
                touchSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnTouchPoints(double timestamp, TouchPoint[] points, int n) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isButtonDown(MouseEvent.BUTTON1)) {
            dragLeftXorigin = e.getX();
            dragLeftYorigin = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        rotationXorigin = rotationX;
        rotationYorigin = rotationY;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isButtonDown(MouseEvent.BUTTON1)) {
            // x/y reversed because of axis orientation. (up/down => x axis
            // rotation in OpenGL)
            if (e.isShiftDown()) {
                rotationX = ((e.getX() - dragLeftXorigin) / 10f + rotationXorigin) % 360;
                rotationY = ((e.getY() - dragLeftYorigin) / 10f + rotationYorigin) % 360;
            } else {
                rotationX = ((e.getX() - dragLeftXorigin) + rotationXorigin) % 360;
                rotationY = ((e.getY() - dragLeftYorigin) + rotationYorigin) % 360;
            }
            // Make sure the numbers are always positive (so we can determine
            // the octant we're in more easily)
            if (rotationX < 0)
                rotationX = 360f + rotationX % 360;
            if (rotationY < 0)
                rotationY = 360f + rotationY % 360;

            rotation.set(0, rotationY);
            rotation.set(1, rotationX);
            rotation.set(2, 0f); // We never rotate around the Z axis.
            setCurrentOctant(rotation);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        float newViewDist = this.viewDist;

        if (e.isShiftDown()) {
            newViewDist -= e.getWheelRotation() * 2;
        } else {
            newViewDist -= e.getWheelRotation() * 10;
        }
        viewDist = newViewDist;
    }

    private void setCurrentOctant(VecF3 rotation) {
        float x = rotation.get(0);
        int qx = (int) Math.floor(x / 90f);
        float y = rotation.get(1);
        int qy = (int) Math.floor(y / 90f);

        if (qx == 0 && qy == 0) {
            current_view_octant = octants.NPP;
        } else if (qx == 0 && qy == 1) {
            current_view_octant = octants.NPN;
        } else if (qx == 0 && qy == 2) {
            current_view_octant = octants.PPN;
        } else if (qx == 0 && qy == 3) {
            current_view_octant = octants.PPP;

        } else if (qx == 1 && qy == 0) {
            current_view_octant = octants.PPN;
        } else if (qx == 1 && qy == 1) {
            current_view_octant = octants.PPP;
        } else if (qx == 1 && qy == 2) {
            current_view_octant = octants.NPP;
        } else if (qx == 1 && qy == 3) {
            current_view_octant = octants.NPN;

        } else if (qx == 2 && qy == 0) {
            current_view_octant = octants.PNN;
        } else if (qx == 2 && qy == 1) {
            current_view_octant = octants.PNP;
        } else if (qx == 2 && qy == 2) {
            current_view_octant = octants.NNP;
        } else if (qx == 2 && qy == 3) {
            current_view_octant = octants.NNN;

        } else if (qx == 3 && qy == 0) {
            current_view_octant = octants.NNP;
        } else if (qx == 3 && qy == 1) {
            current_view_octant = octants.NNN;
        } else if (qx == 3 && qy == 2) {
            current_view_octant = octants.PNN;
        } else if (qx == 3 && qy == 3) {
            current_view_octant = octants.PNP;
        }
    }

    public octants getCurrentOctant() {
        return current_view_octant;
    }

    public VecF3 getRotation() {
        return rotation;
    }

    public void setRotation(VecF3 rotation) {
        this.rotation = rotation;
    }

    public float getViewDist() {
        return viewDist;
    }

    public void setViewDist(float dist) {
        this.viewDist = dist;
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }
}
