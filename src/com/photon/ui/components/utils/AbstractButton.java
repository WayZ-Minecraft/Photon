package com.photon.ui.components.utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.photon.ui.base.EventListener;
import com.photon.ui.base.EventListener.PhotonEvent;
import com.photon.util.os.FileLocation;

@SuppressWarnings("serial")
public abstract class AbstractButton extends JComponent implements MouseListener {

	private int arcWidth = 0;
    private int arcHeight = 0;
	
    private Image buttonImage;
    private int imgWidth;
    private int imgHeight;
    private int imgPosX;
    private Color btnImgBaseColor;
    
    private String text;
    private Color textColor;
    private ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();
    private boolean hover = false;
    
    private boolean selectable = false;
    private boolean selected = false;
    protected Color selectedColor;
    
    private String clickSound;
    private String hoverSound;

    public AbstractButton() {
    	this.addMouseListener(this);
    	this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setSelectable() { this.selectable = true; }
    
    public boolean isSelectable() { return this.selectable; }
    
    public void setSelected(boolean b) { this.selected = b; }
    
    public boolean isSelected() { return this.selected; }
    
    public void setSelectedColor(Color color) { this.selectedColor = color; }
    
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
    	if(this.isEnabled() && this.clickSound !=null) { FileLocation.loadSound(this.clickSound).start(); }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(this.isEnabled() && e.getButton() == MouseEvent.BUTTON1) {
            for(EventListener eventListener : this.eventListeners) { eventListener.onEvent(new PhotonEvent(this, PhotonEvent.BUTTON_CLICKED_EVENT)); }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.hover = true;
    	if(this.isEnabled() && this.hoverSound !=null) FileLocation.loadSound(this.hoverSound).start();
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
    	this.hover = false;
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        repaint();
    }
    
    public void setText(String text) {
        if(text == null) { throw new IllegalArgumentException("text == null");  }
        this.text = text;
        repaint();
    }
    
    public String getText() { return text; }
    
    public void setTextColor(Color textColor) {
        if(textColor == null) { throw new IllegalArgumentException("textColor == null"); }
        this.textColor = textColor;
        repaint();
    }
    
    public Color getTextColor() { return textColor; }

    public void addEventListener(EventListener eventListener) {
        if(eventListener == null) { throw new IllegalArgumentException("eventListener == null"); }
        this.eventListeners.add(eventListener);
    }

    public ArrayList<EventListener> getEventListeners() { return this.eventListeners; }

    public boolean isHover() { return this.hover; }
    
    public void setClickSound(String sound) { this.clickSound = sound; }
        
    public void setHoverSound(String sound) { this.hoverSound = sound; }
    
    public void setArcSize(int w, int h) {
    	this.arcWidth = w;
    	this.arcHeight = h;
    }
    
    public int getArcWidth() { return this.arcWidth; }
        
    public int getArcHeight() { return this.arcHeight; }
    
    public void setButtonImage(Image img, int imgWidth, int imgHeight, int imgPosX, Color imgBaseColor) {
    	this.buttonImage = img;
    	this.imgWidth = imgWidth;
    	this.imgHeight = imgHeight;
    	this.imgPosX = imgPosX;
    	this.btnImgBaseColor = imgBaseColor;
    	repaint();
    }
    
    public int getButtonImageWidth() { return this.imgWidth; }
    
    public int getButtonImageHeight() { return this.imgHeight; }
    
    public int getButtonImageXPosition() { return this.imgPosX; }
    
    public Image getButtonImage() { return this.buttonImage; }
    
    public Color getButtonImageColor() { return this.btnImgBaseColor; }
    
    public void doClick() { this.doClick(68); }
    
    public void doClick(int pressTime) {
    	final MouseEvent me = new MouseEvent(this, 0, 0, 0, (int)this.getBounds().getCenterX(), (int)this.getBounds().getCenterY(), 1, false, MouseEvent.BUTTON1);
    	this.mousePressed(me);
    	this.mouseReleased(me);
    }
}