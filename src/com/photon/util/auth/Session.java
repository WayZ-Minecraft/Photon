package com.photon.util.auth;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.photon.ui.images.Scalr;
import com.photon.ui.images.Scalr.Method;

public class Session {

	public String username;
	public String token;
	public String uuId;

	public Session() {}

	public Session(String user, String tken, String uid) {
		this.username = user;
		this.token = tken;
		this.uuId = uid;
	}

	public Session(Session s) {
		this.username = s.username;
		this.token = s.token;
		this.uuId = s.uuId;
	}

	public String getUsername() {
		return username;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getUuid() {
		return uuId;
	}
	
	public void setUsername(String name) {
		this.username = name;
	}
	
	public void setToken(String tkn) {
		this.token = tkn;
	}
	
	public void setUuid(String id) {
		this.uuId = id;
	}
	
	public String getPlayerMCAvatar() { return "https://crafatar.com/avatars/" + this.getUuid() + ".png"; }
	
	public BufferedImage getPlayerMCAvatarAsImage() {
		try {
			return ImageIO.read(new URL(getPlayerMCAvatar()).openStream());
		} catch (IOException e) { return null; }
	}
	
	public BufferedImage getPlayerMCAvatarAsImageSmooth(int smoothLevel) {
		return Scalr.resize(getPlayerMCAvatarAsImage(), Method.ULTRA_QUALITY, smoothLevel, Scalr.OP_ANTIALIAS);
	}
}
