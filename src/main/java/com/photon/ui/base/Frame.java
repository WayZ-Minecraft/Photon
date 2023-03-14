package com.photon.ui.base;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.photon.ui.PhotonInterfaceUtils;
import com.photon.ui.components.utils.WindowMover;

@SuppressWarnings("serial")
public abstract class Frame extends JFrame {
	
	boolean debugMode;
	
	public Frame() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public Frame(boolean debug) {
		this();
		this.debugMode = debug;
	}
	
	public void setMover(Component component) {
		final WindowMover mover = new WindowMover(this);
		component.addMouseListener(mover);
		component.addMouseMotionListener(mover);
	}
		
	public void setFrameUILookAndFeel() { setFrameUILookAndFeel(0, 0); }

	public void setFrameUILookAndFeel(double arcw, double arch) {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
		this.setUndecorated(true);
		this.setBackground(PhotonInterfaceUtils.LITTLE_TRANSPARENT_WHITE);
		this.addComponentListener(new ComponentAdapter() {
            @Override
             public void componentResized(ComponentEvent e) { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arcw, arch)); }
		});
	}
}
