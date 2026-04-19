package com.photon.objects;

public record ObjectContentPack(double size, int connectionID, String name, boolean isLast, boolean dir, String sha1, byte[] fileContent) {
}
