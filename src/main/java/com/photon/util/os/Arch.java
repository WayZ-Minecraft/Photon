package com.photon.util.os;

@SuppressWarnings("unused")
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

	public String getBit() {
		return this.sBit;
	}

	public boolean isCurrent() {
		return this == CURRENT;
	}

	private static Arch getCurrent() {
		String curArch = System.getProperty("sun.arch.data.model");
		Arch[] var4;
		int var3 = (var4 = values()).length;
		for (int var2 = 0; var2 < var3; var2++) {
			Arch arch = var4[var2];
			if (arch.sBit.equals(curArch)) {
				return arch;
			}
		}
		return UNKNOWN;
	}
}