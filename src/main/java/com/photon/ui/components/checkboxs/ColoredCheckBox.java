package com.photon.ui.components.checkboxs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.AbstractCheckbox;
import com.photon.util.os.FileLocation;

public class ColoredCheckBox extends AbstractCheckbox {
	
	private Image checkImage;
	private Color backgroundColor;
	private Color checkColor;
	
	public ColoredCheckBox(Color backgroundColor) { this(backgroundColor, Color.white); }
	
	public ColoredCheckBox(Color backgroundColor, Color checkColor) {
		this.backgroundColor = backgroundColor;
		this.checkColor = checkColor;
		Image img = FileLocation.loadImage("checkbox_check");
		this.checkImage = img;
	}
	
	@Override
    public void paintComponent(Graphics g) {
		PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight(), this.backgroundColor);
		if(this.isChecked()) {
			final Image check = PhotonInterfaceUtils.colorImage(this.checkImage, this.checkColor);
			PhotonInterfaceUtils.drawImage(g, check, spaceX, spaceY, this.getWidth() - (spaceX * 2), this.getHeight() - (spaceY * 2));
		}
	}
	
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
		repaint();
	}
	
	public void setCheckColor(Color checkColor) {
		this.checkColor = checkColor;
		repaint();
	}
	
	public void setCheckImage(Image img) {
		this.checkImage = img;
		repaint();
	}
}
