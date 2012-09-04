package util;

import imau.visualization.ImauApp;
import imau.visualization.ImauSettings;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import openglCommon.math.VecF3;
import openglCommon.util.InputHandler;

public class ImauInputHandler extends InputHandler implements TouchEventHandler {
    private final ImauSettings settings           = ImauSettings.getInstance();
    private final InputHandler superClassInstance = InputHandler.getInstance();

    private Socket             touchSocket;
    private ConnectionHandler  touchConnection;

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
        superClassInstance.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        superClassInstance.mouseReleased(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        superClassInstance.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Empty - unneeded
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        superClassInstance.mouseWheelMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if (e.getKeyCode() == KeyEvent.VK_P) {
            ImauApp.getImage();
        }
    }

    @Override
    public octants getCurrentOctant() {
        return superClassInstance.getCurrentOctant();
    }

    @Override
    public VecF3 getRotation() {
        return superClassInstance.getRotation();
    }

    @Override
    public float getViewDist() {
        return superClassInstance.getViewDist();
    }

    @Override
    public void setRotation(VecF3 rotation) {
        superClassInstance.setRotation(rotation);
    }

    @Override
    public void setViewDist(float dist) {
        superClassInstance.setViewDist(dist);
    }

    @Override
    public void OnTouchPoints(double timestamp, TouchPoint[] points, int n) {
        // TODO
        System.out.println("TOUCH: " + timestamp + " nr:" + n);
    }
}
