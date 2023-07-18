package com.photon.util.auth;

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
}
