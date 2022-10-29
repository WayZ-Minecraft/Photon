package com.photon.ui.components.utils;

import java.awt.Color;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public abstract class AbstractProgressbar extends JComponent {
	
	private int arcWidth = 0;
	private int arcHeight = 0;

    private int value;
    private int maximum;

    private String string;
    private boolean stringPainted;
    private Color stringColor;
    
    private boolean vertical = false;

    public static int crossMult(int value, int maximum, int coefficient) { return (int) ((double) value / (double) maximum * (double) coefficient); }
    
    public void setArcSize(int w, int h) {
    	this.arcWidth = w;
    	this.arcHeight = h;
    }
    
    public int getArcWidth() { return this.arcWidth; }
        
    public int getArcHeight() { return this.arcHeight; }
    
    public void setValue(int value) {
        this.value = value;
        repaint();
    }

    public int getValue() { return value; }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
        repaint();
    }

    public int getMaximum() { return maximum; }

    public void setString(String string) {
        if(string == null) { throw new IllegalArgumentException("string == null"); }
        this.string = string;
        repaint();
    }

    public String getString() { return string; }

    public void setStringPainted(boolean stringPainted) {
        this.stringPainted = stringPainted;
        repaint();
    }

    public boolean isStringPainted() { return stringPainted; }

    public void setStringColor(Color stringColor) {
        if(stringColor == null) { throw new IllegalArgumentException("stringColor == null"); }
        this.stringColor = stringColor;
        repaint();
    }

    public Color getStringColor() { return stringColor; }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
        repaint();
    }

    public boolean isVertical() { return this.vertical; }
}