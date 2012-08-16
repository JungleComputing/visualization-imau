package util;

import imau.visualization.ImauApp;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import openglCommon.math.VecF3;
import openglCommon.util.InputHandler;
import openglCommon.util.Settings;

public class ImauInputHandler extends InputHandler {
    private final Settings     settings           = Settings.getInstance();
    private final InputHandler superClassInstance = InputHandler.getInstance();

    private static class SingletonHolder {
        public static final ImauInputHandler instance = new ImauInputHandler();
    }

    public static ImauInputHandler getInstance() {
        return SingletonHolder.instance;
    }

    protected ImauInputHandler() {
        super();
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
}
