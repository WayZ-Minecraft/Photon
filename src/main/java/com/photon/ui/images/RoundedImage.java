package com.photon.ui.images;

import javax.swing.*;

import com.photon.ui.PhotonInterfaceUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RoundedImage extends JPanel {
    private BufferedImage image;

    private int arcW = 0;
    private int arcH = 0;

    public RoundedImage(String imagePath) {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setBackground(new Color(0, true));
    }

    public RoundedImage(BufferedImage image) {
        this.image = image;
    }

    public RoundedImage(String imagePath, int arcW, int arcH) {
        this(imagePath);
        this.arcW = arcW;
        this.arcH = arcH;
    }

    public RoundedImage(BufferedImage image, int arcW, int arcH) {  
        this(image);
        this.arcW = arcW;
        this.arcH = arcH;
    }

    public void setArc(int arcW, int arcH) {
        this.arcW = arcW;
        this.arcH = arcH;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            PhotonInterfaceUtils.drawRoundedImage(g, image, 0, 0, this.getWidth(), this.getHeight(), arcW, arcH, this);
        }
    }

}
