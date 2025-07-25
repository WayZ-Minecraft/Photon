package com.photon.ui.components.utils;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JComponent;

import com.photon.ui.base.EventListener;
import com.photon.ui.base.EventListener.PhotonEvent;
import com.photon.util.os.FileLocation;

public class AbstractCheckbox extends JComponent implements MouseListener {
	
	private boolean checked;
    private ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();
	
    protected int spaceX;
    protected int spaceY;
    
	private int arcWidth = 0;
    private int arcHeight = 0;
    
    private String clickSound;
    private String hoverSound;
    
    public AbstractCheckbox() {
        this.addMouseListener(this);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void switchChecked() { setChecked(!this.checked); }
    
    public void setChecked(boolean checked) {
        this.checked = checked;
        repaint();
    }

    public void addEventListener(EventListener eventListener) {
        if(eventListener == null) { throw new IllegalArgumentException("eventListener == null"); }
        this.eventListeners.add(eventListener);
    }

    public ArrayList<EventListener> getEventListeners() { return this.eventListeners; }
    
    public boolean isChecked() { return this.checked; }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
    	if(this.isEnabled()) {
    		this.setChecked(!checked);
    		if(this.clickSound !=null) { FileLocation.playSound(this.clickSound); }
    	}
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for(EventListener eventListener : this.eventListeners) { eventListener.onEvent(new PhotonEvent(this, PhotonEvent.CHECK_BOX_CLICKED_EVENT)); }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    	if(this.isEnabled() && this.hoverSound !=null) { FileLocation.playSound(this.hoverSound); }
    	repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) { repaint(); }
    
    public void setClickSound(String sound) { this.clickSound = sound; }
    
    public void setHoverSound(String sound) { this.hoverSound = sound; }
    
    public void setArcSize(int w, int h) {
    	this.arcWidth = w;
    	this.arcHeight = h;
    }
    
    public int getArcWidth() { return this.arcWidth; }
        
    public int getArcHeight() { return this.arcHeight; }
    
    public void setScale(int x, int y) {
    	this.spaceX = x;
    	this.spaceY = y;
    	repaint();
    }
}
