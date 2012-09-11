package util;

import imau.visualization.ImauApp;
import imau.visualization.ImauSettings;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import openglCommon.math.VecF2;
import openglCommon.math.VecF3;
import openglCommon.math.VectorFMath;
import openglCommon.util.InputHandler;

public class ImauInputHandler extends InputHandler implements TouchEventHandler {
    private final ImauSettings settings = ImauSettings.getInstance();

    private Socket touchSocket;
    private ConnectionHandler touchConnection;

    protected float initialResizeDist;

    private static class SingletonHolder {
        public static final ImauInputHandler instance = new ImauInputHandler();
    }

    public static ImauInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ImauInputHandler() {
        super();

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
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (e.getKeyCode() == KeyEvent.VK_P) {
            ImauApp.getImage();
        }
    }

    public VecF3 getRotation() {
        return super.rotation;
    }

    public void setRotation(VecF3 rotation) {
        super.rotation = rotation;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isShiftDown()) {
            setDeltaViewDist(e.getWheelRotation() * 2);
        } else {
            setDeltaViewDist(e.getWheelRotation() * 10);
        }
    }

    public float getViewDist() {
        return super.viewDist;
    }

    public void setViewDist(float dist) {
        super.viewDist = dist;
    }

    public void setDeltaViewDist(float delta) {
        float potential = super.viewDist - delta;

        if (potential > -50f) {
            potential = -50f;
        } else if (potential < -400f) {
            potential = -400f;
        }

        super.viewDist = potential;
    }

    int currentTouchID = 0;

    @Override
    public void OnTouchPoints(double timestamp, TouchPoint[] points, int n) {
        if (n == 1) {
            int x = (int) (((points[0].tx - 0.5f) * 1900f) * 2f);
            int y = (int) (points[0].ty * 1200);
            if (points[0].state == 0) {
                ImauApp.feedMouseEventToPanel(x, y);
            }

        } else if (n == 6) { // TODO should be one obviously
            int x = (int) ((points[0].tx - 0.5f) * 1900);
            int y = (int) (points[0].ty * 1200);

            if (points[0].state == 0) {
                dragLeftXorigin = x;
                dragLeftYorigin = y;
            } else if (points[0].state == 1) {
                // x/y reversed because of axis orientation. (up/down => x axis
                // rotation in OpenGL)

                rotationX = ((x - dragLeftXorigin) + rotationXorigin) % 360;
                rotationY = ((y - dragLeftYorigin) + rotationYorigin) % 360;

                if (rotationX < 0)
                    rotationX = 360f + rotationX % 360;
                if (rotationY < 0)
                    rotationY = 360f + rotationY % 360;

                rotation.set(0, rotationY);
                rotation.set(1, rotationX);
                rotation.set(2, 0f); // We never rotate around the Z axis.

            } else if (points[0].state == 2) {
                rotationXorigin = rotationX;
                rotationYorigin = rotationY;
            }
        } else if (n == 2) {
            VecF2 v0 = new VecF2((points[0].tx - 0.5f), points[0].ty);
            VecF2 v1 = new VecF2((points[1].tx - 0.5f), points[1].ty);

            if (points[1].state == 0 || points[0].state == 0) {
                initialResizeDist = VectorFMath.length((v0.sub(v1)));
            } else if (points[1].state == 1 && points[0].state == 1) {
                float amountShorterThanInitial = VectorFMath.length((v0.sub(v1))) - initialResizeDist;

                setDeltaViewDist(amountShorterThanInitial * 10);
            }
        }

    }
}
