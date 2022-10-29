package com.photon.ui.components.buttons;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.AbstractButton;

@SuppressWarnings("serial")
public class TexturedButton extends AbstractButton {
	
	private Image texture;
    private Image textureHover;
    private Image textureDisabled;
    
    public TexturedButton(Image texture) { this(texture, null, null); }

    public TexturedButton(Image texture, Image textureHover) { this(texture, textureHover, null); }

    public TexturedButton(Image texture, Image textureHover, Image textureDisabled) {
        if(texture == null) { throw new IllegalArgumentException("texture == null"); }
        this.texture = texture;

        if(textureHover == null) { this.textureHover = PhotonInterfaceUtils.colorImage(texture, Color.white.brighter()); }
        else { this.textureHover = textureHover; }

        if(textureDisabled == null) { this.textureDisabled = PhotonInterfaceUtils.colorImage(texture, Color.white.darker()); }
        else { this.textureDisabled = textureDisabled; }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Image texture;
        
        if(!this.isEnabled()) { texture = textureDisabled; }
        else if (super.isHover()) { texture = textureHover; }
        else { texture = this.texture; }

        PhotonInterfaceUtils.drawRoundedImage(g, texture, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight());
        
        if(this.getText() != null) {
            PhotonInterfaceUtils.activateAntialias(g);
            PhotonInterfaceUtils.drawCenteredText(g, this.getText(), this.getBounds(), this.getTextColor(), this.getFont());
        }
        
        if(this.getButtonImage() != null) {
        	if(this.getText() == null) { PhotonInterfaceUtils.drawCenteredImage(g, PhotonInterfaceUtils.colorImage(this.getButtonImage(), this.getButtonImageColor()), this.getBounds(), this.getButtonImageWidth(), this.getButtonImageHeight()); }
        	else {
        		FontMetrics fm = g.getFontMetrics();
        		Rectangle2D stringBounds = fm.getStringBounds(this.getText(), g);
        		PhotonInterfaceUtils.drawCenteredOnYImage(g, PhotonInterfaceUtils.colorImage(this.getButtonImage(), this.getButtonImageColor()), this.getHeight(), (int)stringBounds.getWidth() + this.getButtonImageXPosition(), this.getButtonImageWidth(), this.getButtonImageHeight());
        	}
        }
    }

    public void setTexture(Image texture) {
        if(texture == null) { throw new IllegalArgumentException("texture == null"); }
        this.texture = texture;
        repaint();
    }

    public void setTextureHover(Image textureHover) {
        if(textureHover == null) { throw new IllegalArgumentException("textureHover == null"); }
        this.textureHover = textureHover;
        repaint();
    }

    public void setTextureDisabled(Image textureDisabled) {
        if(textureDisabled == null) { throw new IllegalArgumentException("textureDisabled == null"); }
        this.textureDisabled = textureDisabled;
        repaint();
    }

    public Image getTexture() { return this.texture; }

    public Image getTextureHover() { return this.textureHover; }

    public Image getTextureDisabled() { return this.textureDisabled; }
}
