package com.photon.util.auth;

public class PhotonAuthSession {

	public String username;
	public String token;
	public String uuId;

	public PhotonAuthSession() {}

	public PhotonAuthSession(String user, String tken, String uid) {
		this.username = user;
		this.token = tken;
		this.uuId = uid;
	}

	public PhotonAuthSession(PhotonAuthSession s) {
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
}
