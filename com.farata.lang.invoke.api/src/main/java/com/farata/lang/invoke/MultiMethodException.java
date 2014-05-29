package com.farata.lang.invoke;

public class MultiMethodException extends RuntimeException {
	final private static long serialVersionUID = 1L;

	public MultiMethodException() {
		
	}
	
	public MultiMethodException(final String message) {
		super(message);
	}
}
