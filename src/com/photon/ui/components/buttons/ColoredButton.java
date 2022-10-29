package com.photon.ui.components.buttons;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.AbstractButton;

@SuppressWarnings("serial")
public class ColoredButton extends AbstractButton {

    private Color color;
    private Color colorHover;
    private Color colorDisabled;

    public ColoredButton(Color color) { this(color, null, null); }

    public ColoredButton(Color color, Color colorHover) { this(color, colorHover, null); }

    public ColoredButton(Color color, Color colorHover, Color colorDisabled) {
        if(color == null) { throw new IllegalArgumentException("Color == null"); }
        this.color = color;

        if(colorHover == null) { this.colorHover = color.brighter(); }
        else { this.colorHover = colorHover; }

        if(colorDisabled == null) { this.colorDisabled = color.darker(); }
        else { this.colorDisabled = colorDisabled; }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color color;
        if(!this.isEnabled()) { color = colorDisabled; }
        else if (super.isHover()) { color = colorHover; }
        else { color = this.color; }

        PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight(), color);

        if(this.getText() != null) {
            PhotonInterfaceUtils.activateAntialias(g);
            PhotonInterfaceUtils.drawCenteredText(g, this.getText(), this.getBounds(), this.getTextColor(), this.getFont());
        }
        
        if(this.getButtonImage() != null) {
        	if(this.getText() == null) { PhotonInterfaceUtils.drawCenteredImage(g, PhotonInterfaceUtils.colorImage(this.getButtonImage(), this.getButtonImageColor()), this.getBounds(), this.getButtonImageWidth(), this.getButtonImageHeight()); }
        	else {
        		final FontMetrics fm = g.getFontMetrics();
        		final Rectangle2D stringBounds = fm.getStringBounds(this.getText(), g);
        		PhotonInterfaceUtils.drawCenteredOnYImage(g, PhotonInterfaceUtils.colorImage(this.getButtonImage(), this.getButtonImageColor()), this.getHeight(), (int)stringBounds.getWidth() + this.getButtonImageXPosition(), this.getButtonImageWidth(), this.getButtonImageHeight());
        	}
        }
    }

    public void setColor(Color color) {
        if(color == null) { throw new IllegalArgumentException("Color == null"); }
        this.color = color;
        repaint();
    }

    public Color getColor() { return this.color; }

    public void setColorHover(Color colorHover) {
        if(colorHover == null) { throw new IllegalArgumentException("colorHover == null"); }
        this.colorHover = colorHover;
        repaint();
    }

    public Color getColorHover() { return this.colorHover; }

    public void setColorDisabled(Color colorDisabled) {
        if(colorDisabled == null) { throw new IllegalArgumentException("colorDisabled == null"); }
        this.colorDisabled = colorDisabled;
        repaint();
    }

    public Color getColorDisabled() { return this.colorDisabled; }
}