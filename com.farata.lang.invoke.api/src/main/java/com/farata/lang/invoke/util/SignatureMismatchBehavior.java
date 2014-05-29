package com.farata.lang.invoke.util;

import java.lang.reflect.Method;

public interface SignatureMismatchBehavior {

	abstract public void report(Method implementationMethod, Method interfaceMethod, String message);

	public static enum Standard implements SignatureMismatchBehavior {
		SKIP {
			public void report(final Method implementationMethod, final Method interfaceMethod, final String message) {
				System.err.println(message);
			}
		}, 
		
		ERROR {
			public void report(final Method implementationMethod, final Method interfaceMethod, final String message) {
				throw new IllegalArgumentException(message);
			}
		}
		;
	
	}
}
