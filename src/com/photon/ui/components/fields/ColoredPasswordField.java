package com.photon.ui.components.fields;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JPasswordField;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.util.os.FileLocation;

@SuppressWarnings("serial")
public class ColoredPasswordField extends JPasswordField implements KeyListener, MouseListener
{	
	private JComponent prevComponent;
	private JComponent nextComponent;
	private boolean switchAfterCharLimitHit;

	private String tooltipText;
	private Color tooltipColor;
	private Font tooltipFont;

	private Color background;
	private char hideChar;
	private boolean isTextVisible;
	
	private String keyTypedSound;
	private String clickSound;
	private String hoverSound;
		
	private int arcWidth = 0;
    private int arcHeight = 0;
	
    public ColoredPasswordField() { this(new Color(0, 0, 0, 0), Color.white, '*'); }
    
    public ColoredPasswordField(Color background) { this(background, Color.white, '*'); }
    
    public ColoredPasswordField(Color background, Color color) { this(background, color, '*'); }
    
	public ColoredPasswordField(Color background, Color color, char textChar) {
        if(background == null) { throw new IllegalArgumentException("background Color == null"); }
        this.background = background;
        this.setOpaque(false);
        this.setBorder(null);
        this.setEchoChar(this.hideChar = textChar);
        
        this.setDisabledTextColor(color.darker());
        this.setForeground(color);
        this.setCaretColor(color);
        this.setSelectionColor(color);
        
        this.addKeyListener(this);
        this.addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
    	PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight(), this.background);
    	if(this.tooltipText !=null && !this.tooltipText.isEmpty() && this.getPassword().length <= 0 && !this.hasFocus()) {
    		PhotonInterfaceUtils.drawTextCenteredY(g, this.tooltipText, this.getHeight(), 2, this.tooltipColor, this.tooltipFont);
    	}
    	super.paintComponent(g);
    }
    
    @Override
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        repaint();
    }

    public void setCharLimit(int charLimit) { this.setDocument(new FieldCharLimit(charLimit, this.nextComponent, this.switchAfterCharLimitHit)); }
    
    public void setTooltip(String tooltipText, Color tooltipColor) {
    	this.tooltipText = tooltipText;
    	this.tooltipColor = tooltipColor;
    	this.tooltipFont = this.getFont().deriveFont(0, 13);
    	repaint();
    }
    
    public void setBackground(Color background) {
        if(background == null) { throw new IllegalArgumentException("background == null"); }
        this.background = background;
        repaint();
    }

    public Color getBackground() { return this.background; }
    
    public String getPasswordAsText() { return String.valueOf(this.getPassword()); }
    
    public void setArcSize(int w, int h) {
    	this.arcWidth = w;
    	this.arcHeight = h;
    }
    
    public int getArcWidth() { return this.arcWidth; }
        
    public int getArcHeight() { return this.arcHeight; }
    
    public void setKeyTypedSound(String sound) { this.keyTypedSound = sound; }

    public void setClickSound(String sound) { this.clickSound = sound; }
    
    public void setHoverSound(String sound) { this.hoverSound = sound; }
     
    public boolean isTextIsVisible() { return this.isTextVisible; }
    
    public void showHideText() {
    	this.isTextVisible = !this.isTextVisible;
    	this.setEchoChar(this.isTextVisible ? (char) 0 : hideChar);
    }
        
    public void setNextComponent(JComponent prevComponent, JComponent nextComponent, boolean b) {
    	this.prevComponent = prevComponent;
    	this.nextComponent = nextComponent;
    	this.switchAfterCharLimitHit = b;
    }

	@Override
	public void keyTyped(KeyEvent e) {
		if(this.keyTypedSound != null) { FileLocation.loadSound(this.keyTypedSound).start(); }
		if(this.nextComponent !=null && e.getKeyChar() == KeyEvent.VK_ENTER) { FieldCharLimit.switchComponent(this.nextComponent); }
		if(this.prevComponent !=null && this.getPasswordAsText().length() <= 0 && e.getKeyChar() == KeyEvent.VK_BACK_SPACE) { FieldCharLimit.switchComponent(this.prevComponent); }
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(this.isEnabled() && this.clickSound != null) { FileLocation.loadSound(this.clickSound).start(); }
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if(this.isEnabled() && this.hoverSound != null) { FileLocation.loadSound(this.hoverSound).start(); }
	}

	@Override
	public void mouseExited(MouseEvent e) {}
}
