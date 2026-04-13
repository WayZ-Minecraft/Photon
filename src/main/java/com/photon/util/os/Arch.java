package com.photon.util.os;

public enum Arch {
	x86(32), x64(64), UNKNOWN(0);

	public static final Arch CURRENT = getCurrent();

	public final int bit;
	public final String strigifiedBit;

	private Arch(int bit) {
		this.bit = bit;
		this.strigifiedBit = String.valueOf(bit);
	}

	public boolean isCurrent() { return this == CURRENT; }

	private static Arch getCurrent() {
		final String CURRENT_ARCH = System.getProperty("sun.arch.data.model");
		for (final Arch ARCH : values()) {
			if (ARCH.strigifiedBit.equals(CURRENT_ARCH)) return ARCH;
		}
		return UNKNOWN;
	}
}