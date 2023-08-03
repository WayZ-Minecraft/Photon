package com.photon.ui.base;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
@SuppressWarnings("serial")
public class Panel extends JComponent
{
	
	public Panel() { super(); }

	/**
	 * Add padding to the panel
	 * @param paddingTop
	 * @param paddingRight
	 * @param paddingBottom
	 * @param paddingLeft
	 */
	public void setPadding(int paddingTop, int paddingRight, int paddingBottom, int paddingLeft) { 
		this.setBorder(new EmptyBorder(paddingTop, paddingLeft, paddingBottom, paddingRight));
	}

	public void setPadding(int paddingTopBottom, int paddingLeftRight) { this.setPadding(paddingTopBottom, paddingLeftRight, paddingTopBottom, paddingLeftRight); }

	public void setPadding(int padding) { this.setPadding(padding, padding, padding, padding); }
	
	@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(18, 200, 0, 50));
		g.fillRect(0, 0, 100, 150);

		g.setColor(new Color(0, 0, 0, 255));
		g.drawString("Test Encore oui", 0, 5);

    }

	
}
