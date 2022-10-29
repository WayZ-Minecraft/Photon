package com.photon.util;

import java.io.PrintStream;

import javax.swing.JTextArea;

public class RememberAllWrittenTextPrintStream extends PrintStream {

	private static final String newLine = System.getProperty("line.separator");
	private final PrintStream original;
	private final JTextArea textList;
	
	public RememberAllWrittenTextPrintStream(PrintStream original, JTextArea textList) {
		super(original);
		this.original = original;
		this.textList = textList;
	}
	    
	public void print(double d) {
		textList.append(String.valueOf(d));
		textList.append(newLine);
		original.print(d);
	}

	public void print(String s) {
		textList.append(s);
		textList.append(newLine);
		original.print(s);
	}

	public void println(String s) {
		textList.append(s);
		textList.append(newLine);
		original.println(s);
	}

	public void println() {
		textList.append(newLine);
		original.println();
	}

	public PrintStream printf(String s, Object... args) {
		textList.append(String.format(s, args));
		return original.printf(s, args);
	}
}