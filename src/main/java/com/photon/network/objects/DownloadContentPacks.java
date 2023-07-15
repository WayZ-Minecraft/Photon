package com.photon.network.objects;

public class DownloadContentPacks {
	public double size = -1;
    public int connectionID;
    public String name = "";
    public boolean isLast = false;
    public boolean dir = false;
    public String sha1 = "";
	public byte[] fileContent = new byte[] {};
}
