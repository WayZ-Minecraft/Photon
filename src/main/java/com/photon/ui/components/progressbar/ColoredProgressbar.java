package com.photon.ui.components.progressbar;

import java.awt.Color;
import java.awt.Graphics;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.AbstractProgressbar;

@SuppressWarnings("serial")
public class ColoredProgressbar extends AbstractProgressbar {

    private Color background;
    private Color foreground;
    
    public ColoredProgressbar(Color background) { this(background, null); }

    public ColoredProgressbar(Color background, Color foreground) {
        if(background == null) { throw new IllegalArgumentException("background == null"); }
        this.background = background;

        if(foreground == null) { this.foreground = background.brighter(); }
        else { this.foreground = foreground; }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight(), this.background);

        int fgSize = crossMult(getValue(), getMaximum(), isVertical() ? this.getHeight() : this.getWidth());
        
        if(fgSize > 0) { PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, isVertical() ? this.getWidth() : fgSize, isVertical() ? fgSize : this.getHeight(), this.getArcWidth(), this.getArcHeight(), this.foreground); }

        if(this.isStringPainted() && this.getString() != null) {
        	PhotonInterfaceUtils.activateAntialias(g);
            PhotonInterfaceUtils.drawCenteredText(g, getString(), this.getBounds(), this.getStringColor(), this.getFont());
        }
    }
    
    public void setBackground(Color background) {
        if(background == null) { throw new IllegalArgumentException("background == null"); }
        this.background = background;
        repaint();
    }

    public Color getBackground() { return background; }

    public void setForeground(Color foreground) {
        if(foreground == null) { throw new IllegalArgumentException("foreground == null"); }
        this.foreground = foreground;
        repaint();
    }

    public Color getForeground() { return foreground; }
}