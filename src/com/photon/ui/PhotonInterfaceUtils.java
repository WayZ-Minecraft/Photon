package com.photon.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedString;

public class PhotonInterfaceUtils {

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static final Color LITTLE_TRANSPARENT_WHITE = new Color(255, 255, 255, 50);
    
    private static Point getStringCenterPos(Rectangle parent, String str, FontMetrics fontMetrics, Graphics g)
    {
        Rectangle2D stringBounds = fontMetrics.getStringBounds(str, g);
        double x = ((parent.getWidth() - stringBounds.getWidth()) / 2);
        double y = ((parent.getHeight() - stringBounds.getHeight()) / 2 + fontMetrics.getAscent());
        return new Point((int) x, (int) y);
    }
    
    private static Point getTextLayoutCenterPos(Rectangle parent, TextLayout fontMetrics, Graphics g)
    {
    	Rectangle2D stringBounds = fontMetrics.getBounds();
    	double x = ((parent.getWidth() - stringBounds.getWidth()) / 2);
    	double y = ((parent.getHeight() - stringBounds.getHeight()) / 2 + fontMetrics.getAscent());
    	return new Point((int) x, (int) y);
    }

    public static void drawCenteredParagraph(Graphics2D g, String paragraph, Rectangle parent, double width, Color color, Font font) {
        FontRenderContext frc = g.getFontMetrics(font).getFontRenderContext();
        AttributedString attributer = new AttributedString(paragraph);
        attributer.addAttribute(TextAttribute.FONT, font);
    	LineBreakMeasurer linebreaker = new LineBreakMeasurer(attributer.getIterator(), frc);

    	int y = 0;
    	while (linebreaker.getPosition() < paragraph.length()) {
        	TextLayout textLayout = linebreaker.nextLayout((float)width);
        	Point centerPos = getTextLayoutCenterPos(parent, textLayout, g);
        	y += textLayout.getAscent();
        	if(color !=null) { g.setColor(color); }
        	if(font !=null) { g.setFont(font); }
        	textLayout.draw(g, (int) centerPos.getX(), y + (int) centerPos.getY() / 2);
        	y += textLayout.getDescent() + textLayout.getLeading();
    	}
    }
    
    public static void drawParagraph(Graphics2D g, String paragraph, int posX, int posY, double width, Color color) {
    	LineBreakMeasurer linebreaker = new LineBreakMeasurer(new AttributedString(paragraph).getIterator(), g.getFontRenderContext());
    	int y = 0;
    	while (linebreaker.getPosition() < paragraph.length()) {
        	TextLayout textLayout = linebreaker.nextLayout((float)width);

        	y += textLayout.getAscent();
        	if(color !=null) { g.setColor(color); }
        	textLayout.draw(g, posX, posY + y);
        	y += textLayout.getDescent() + textLayout.getLeading();
    	}
    }
    
    public static void drawText(Graphics g, String text, int x, int y, Color color, Font font)
    {
    	if(font == null) { font = g.getFont(); }
        if(color !=null) { g.setColor(color); }
        if(font !=null) { g.setFont(font); }
        g.drawString(text, x, y);
    }
    
    public static void drawTextAlignedRight(Graphics g, String text, int width, int posY, Color color, Font font) {
    	if(font == null) font = g.getFont();
		final FontMetrics fm = g.getFontMetrics(font);
        final double x = width - fm.getStringBounds(text, g).getWidth();
        if(color !=null) g.setColor(color);
        if(font !=null) g.setFont(font);
        g.drawString(text, (int) x, posY);
	}
    
    public static void drawCenteredText(Graphics g, String text, Rectangle parent, Color color, Font font)
    {
    	if(font == null) { font = g.getFont(); }
        FontMetrics fm = g.getFontMetrics();
        Point centerPos = getStringCenterPos(parent, text, fm, g);
        if(color !=null) { g.setColor(color); }
        if(font !=null) { g.setFont(font); }
        g.drawString(text, (int) centerPos.getX(), (int) centerPos.getY());
    }
    
    public static void drawTextCenteredX(Graphics g, String text, int width, int posY, Color color, Font font) {
    	if(font == null) { font = g.getFont(); }
		final FontMetrics fm = g.getFontMetrics(font);
        final double x = (width - fm.getStringBounds(text, g).getWidth()) / 2;
        if(color !=null) { g.setColor(color); }
        if(font !=null) { g.setFont(font); }
        g.drawString(text, (int) x, posY);
	}
    
    public static void drawTextCenteredY(Graphics g, String text, int height, int posX, Color color, Font font) {
    	if(font == null) { font = g.getFont(); }
		final FontMetrics fm = g.getFontMetrics(font);
        final double y = (height - fm.getStringBounds(text, g).getHeight()) / 2 + + fm.getAscent();
        if(color !=null) { g.setColor(color); }
        if(font !=null) { g.setFont(font); }

        g.drawString(text, posX, (int) y);
	}
    
    public static void activateSmoothing(Graphics g) { ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); }
    
    public static void activateAntialias(Graphics g) {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static BufferedImage getImageAsBufferedImage(Image img) {
        if (img instanceof BufferedImage) { return (BufferedImage) img; }
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }
    
    public static BufferedImage colorImage(Image image, Color color) { return colorImage(getImageAsBufferedImage(image), color); }
    
    public static BufferedImage colorImage(BufferedImage image, Color color) {
    	BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(image, null, 0, 0);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
        return img;
    }
    
    public static BufferedImage colorImage(Image image, int red, int green, int blue) { return colorImage(image, new Color(red, green, blue, 255)); }
    
    public static BufferedImage colorImage(BufferedImage image, int red, int green, int blue) { return colorImage(image, new Color(red, green, blue, 255)); }
    
    public static BufferedImage roundImageCorner(Image image, int arcw, int arch) { return roundImageCorner(getImageAsBufferedImage(image), arcw, arch); }
    
    public static BufferedImage roundImageCorner(BufferedImage image, int arcw, int arch) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arcw, arch));
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        
        return output;
    }
    
    public static void drawImage(Graphics g, Image img, int x, int y, int w, int h) {
    	activateSmoothing(g);
    	g.drawImage(img, x, y, w, h, null); 
    }
    
    public static void drawCenteredImage(Graphics g, Image img, Rectangle parent, int w, int h) {
    	activateSmoothing(g);
    	g.drawImage(img, (int)(parent.getWidth() - w) / 2, (int)(parent.getHeight() - h) / 2, w, h, null); 
    }
    
    public static void drawCenteredOnXImage(Graphics g, Image img, int parentw, int y, int w, int h) {
    	activateSmoothing(g);
    	g.drawImage(img, (parentw - w) / 2, y, w, h, null); 
    }
    
    public static void drawCenteredOnYImage(Graphics g, Image img, int parenth, int x, int w, int h) {
    	activateSmoothing(g);
    	g.drawImage(img, x,  (parenth - w) / 2, w, h, null); 
    }
    
    public static void drawRoundedImage(Graphics g, Image img, int x, int y, int w, int h, int arcw, int arch) {
    	activateSmoothing(g);
    	g.drawImage(roundImageCorner(img, arcw, arch), x, y, w, h, null);
    }
    
    public static void drawCenteredRoundedImage(Graphics g, Image img, Rectangle parent, int w, int h, int arcw, int arch) {
    	activateSmoothing(g);
    	g.drawImage(roundImageCorner(img, arcw, arch), (int)(parent.getWidth() - w) / 2, (int)(parent.getHeight() - h) / 2, w, h, null); 
    }
    
    public static void drawCenteredOnXRoundedImage(Graphics g, Image img, int parentw, int y, int w, int h, int arcw, int arch) {
    	activateSmoothing(g);
    	g.drawImage(roundImageCorner(img, arcw, arch), (parentw - w) / 2, y, w, h, null); 
    }
    
    public static void drawCenteredOnYRoundedImage(Graphics g, Image img, int parenth, int x, int w, int h, int arcw, int arch) {
    	activateSmoothing(g);
    	g.drawImage(roundImageCorner(img, arcw, arch), x,  (parenth - w) / 2, w, h, null); 
    }
    
    public static void drawRect(Graphics g, int x, int y, int w, int h, Color color) { drawRoundedRect(g, x, y, w, h, 0, 0, color, color.getAlpha()); }
    
    public static void drawRect(Graphics g, int x, int y, int w, int h, Color color, int alpha) { drawRoundedRect(g, x, y, w, h, 0, 0, color, alpha); }
    
    public static void drawRoundedRect(Graphics g, int x, int y, int w, int h, int arcw, int arch, Color color) { drawRoundedRect(g, x, y, w, h, arcw, arch, color, color.getAlpha()); }
    	
    public static void drawRoundedRect(Graphics g, int x, int y, int w, int h, int arcw, int arch, Color color, int alpha) {
    	activateAntialias(g);
    	g.setColor(color == null ? Color.black : new Color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, alpha / 255.0F));
    	g.fillRoundRect(x, y, w, h, arcw, arch);
    }
}
