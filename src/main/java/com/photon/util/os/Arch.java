package com.photon.util.os;

public enum Arch {
	x86(32), x64(64), UNKNOWN(0);

	public static final Arch CURRENT = getCurrent();
	public static final int MIN_MEMORY = 512;

	private final int bit;
	private final int arch;

	private final String sBit;
	private final String sArch;

	private Arch(int bit) {
		this.bit = bit;
		this.sBit = String.valueOf(bit);
		if (bit == 0) {
			this.sArch = toString();
			this.arch = 0;
		} else {
			this.sArch = toString().substring(1);
			this.arch = Integer.parseInt(this.sArch);
		}
	}

	public String getBitAsString() { return this.sBit; }

	public String getArchAsString() { return this.sArch; }

	public int getBit() { return this.bit; }

	public int getArch() { return this.arch; }

	public boolean isCurrent() { return this == CURRENT; }

	private static Arch getCurrent() {
		final String currentArch = System.getProperty("sun.arch.data.model");

		for (Arch arch : values()) {
			if (arch.sBit.equals(currentArch)) return arch;
		}
		return UNKNOWN;
	}
}