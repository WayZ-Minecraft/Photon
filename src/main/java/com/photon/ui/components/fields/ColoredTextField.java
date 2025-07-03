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
import javax.swing.JTextField;
import javax.swing.text.DefaultEditorKit;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.util.os.FileLocation;

public class ColoredTextField extends JTextField implements KeyListener, MouseListener
{	
	private JComponent nextComponent;
	public JComponent prevComponent;
	private boolean switchAfterCharLimitHit;
	
	private String tooltipText;
	private Color tooltipColor;
	private Font tooltipFont;
	
	private Color background;
	private Color disabledBackground;
	
	private String keyTypedSound;
	private String clickSound;
	private String hoverSound;
		
	private int arcWidth = 0;
    private int arcHeight = 0;
    
    public ColoredTextField() { this(new Color(0, 0, 0, 0)); }
    
    public ColoredTextField(Color background) { this(background, Color.white, null); }
    
	public ColoredTextField(Color background, Color color, Color disabledBackground) {
        if(background == null) { throw new IllegalArgumentException("Background Color == null"); }
        this.background = background;
        
        if(disabledBackground == null) this.disabledBackground = background.darker();
        else this.disabledBackground = background;
        
        this.setOpaque(false);
        this.setBorder(null);
        
        this.setDisabledTextColor(color.darker());
        this.setForeground(color);
        this.setCaretColor(color);
        this.setSelectionColor(color);
        
        this.addKeyListener(this);
        this.addMouseListener(this);
        
        this.getActionMap().get(DefaultEditorKit.beepAction).setEnabled(false);
        
        this.setHorizontalAlignment(JTextField.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
    	PhotonInterfaceUtils.drawRoundedRect(g, 0, 0, this.getWidth(), this.getHeight(), this.getArcWidth(), this.getArcHeight(), this.isEnabled() ? this.background : this.disabledBackground);
    	if(this.tooltipText !=null && !this.tooltipText.isEmpty() && this.getText().length() <= 0 && !this.hasFocus()) PhotonInterfaceUtils.drawTextCenteredY(g, this.tooltipText, this.getHeight(), 2, this.tooltipColor, this.tooltipFont);
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
    
    public void setDisabledBackground(Color disabledBackground) {
    	if(disabledBackground == null) { throw new IllegalArgumentException("disabledBackground"); }
    	this.disabledBackground = disabledBackground;
    	repaint();
    }
    
    public Color getDisabledBackground() { return this.background; }
    
    public void setBackground(Color background) {
        if(background == null) { throw new IllegalArgumentException("background == null"); }
        this.background = background;
        repaint();
    }

    public Color getBackground() { return this.background; }
    
    public void setArcSize(int w, int h) {
    	this.arcWidth = w;
    	this.arcHeight = h;
    }
    
    public int getArcWidth() { return this.arcWidth; }
        
    public int getArcHeight() { return this.arcHeight; }
    
    public void setKeyTypedSound(String sound) { this.keyTypedSound = sound; }

    public void setClickSound(String sound) { this.clickSound = sound; }
    
    public void setHoverSound(String sound) { this.hoverSound = sound; }
    
    public void setNextComponent(JComponent prevComponent, JComponent nextComponent, boolean b) {
    	this.prevComponent = prevComponent;
    	this.nextComponent = nextComponent;
    	this.switchAfterCharLimitHit = b;
    }
    
	@Override
	public void keyTyped(KeyEvent e) {
		if(this.keyTypedSound != null) { FileLocation.playSound(this.keyTypedSound); }
		if(this.nextComponent !=null && e.getKeyChar() == KeyEvent.VK_ENTER) { FieldCharLimit.switchComponent(this.nextComponent); }
		if(this.prevComponent !=null && this.getText().length() <= 0 && e.getKeyChar() == KeyEvent.VK_BACK_SPACE) { FieldCharLimit.switchComponent(this.prevComponent); }
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
		if(this.isEnabled() && this.clickSound != null) FileLocation.playSound(this.clickSound);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if(this.isEnabled() && this.hoverSound != null) FileLocation.playSound(this.hoverSound);
	}

	@Override
	public void mouseExited(MouseEvent e) {}
}
