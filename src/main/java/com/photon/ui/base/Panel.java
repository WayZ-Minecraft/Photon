package com.photon.ui.base;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class Panel extends JPanel
{
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static final Color LITTLE_TRANSPARENT_WHITE = new Color(255, 255, 255, 50);
	public Frame frame;
	
	public Panel(Frame parent) { this.frame = parent; }
	
	public void addComponent(JComponent component) {
		this.add(component);
	}
		
	@Override
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.drawPanel(g);
	}
	
	public abstract void drawPanel(Graphics g);
}
