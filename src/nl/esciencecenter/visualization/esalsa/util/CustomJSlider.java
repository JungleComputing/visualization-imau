package nl.esciencecenter.visualization.esalsa.util;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

public class CustomJSlider extends JSlider {
    private static final long serialVersionUID = -3067450096465148814L;

    public CustomJSlider(final BasicSliderUI ui) {
        MouseListener[] listeners = getMouseListeners();
        for (MouseListener l : listeners)
            removeMouseListener(l); // remove UI-installed TrackListener
        setUI(ui);

        ColoredCustomJSlider.TrackListener tl = ui.new TrackListener() {
            // this is where we jump to absolute value of click
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int value = ui.valueForXPosition(p.x);

                setValue(value);
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            // disable check that will invoke scrollDueToClickInTrack
            @Override
            public boolean shouldScroll(int dir) {
                return false;
            }
        };
        addMouseListener(tl);
    }

    public void setUpperValue(int value) {
        // Compute new extent.
        int lowerValue = getValue();
        // Set extent to set upper value.
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum()
                - lowerValue);
        setExtent(newExtent);
    }
}