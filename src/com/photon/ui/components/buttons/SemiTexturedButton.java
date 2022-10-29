package com.photon.ui.components.buttons;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.AbstractButton;

@SuppressWarnings("serial")
public class SemiTexturedButton extends AbstractButton {

	private Image image;
	private Color color;
    private Color colorHover;
    private Color colorDisabled;
	
	public SemiTexturedButton(Image img) { this(img, Color.white, null, null); }
	
	public SemiTexturedButton(Image img, Color color) { this(img, color, null, null); }

    public SemiTexturedButton(Image img, Color color, Color colorHover) { this(img, color, colorHover, null); }

    public SemiTexturedButton(Image img, Color color, Color colorHover, Color colorDisabled) {
    	if(img == null) { throw new IllegalArgumentException("image == null"); }
    	this.image = img;
        this.color = color;

        if(colorHover == null) { this.colorHover = color.brighter(); }
        else { this.colorHover = colorHover; }

        if(colorDisabled == null) { this.colorDisabled = color.darker(); }
        else { this.colorDisabled = colorDisabled; }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Color color;
        if(!this.isEnabled()) { color = colorDisabled; }
        else if (this.isHover()) { color = colorHover; }
        else if (this.isSelectable() && this.isSelected()) { color = this.selectedColor; }
        else { color = this.color; }
        
        PhotonInterfaceUtils.drawImage(g, PhotonInterfaceUtils.colorImage(this.image, color), 0, 0, this.getWidth(), this.getHeight());

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
    
    public void setImage(Image image) {
        if(image == null) { throw new IllegalArgumentException("image == null"); }
        this.image = image;
        repaint();
    }

    public Image getImage() { return this.image; }
    
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
