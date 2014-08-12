package com.farata.lang.invoke;

import java.lang.reflect.Method;
import java.util.Comparator;

public class MultiMethodEntry {
	final public Method method;
	final public int distance;
	
	public MultiMethodEntry(final Method method, final int distance) {
		this.method = method;
		this.distance = distance;
	}
	
	final public static Comparator<MultiMethodEntry> COMPARE_BY_DISTANCE = new Comparator<MultiMethodEntry>() {
		public int compare(final MultiMethodEntry a, final MultiMethodEntry b) {
			final int byDistance = b.distance - a.distance; // Reverse sort
			if (byDistance != 0)
				return byDistance;
			return b.method.hashCode() - a.method.hashCode(); // Just to have stable sort, irrelevant for task
		}
		
	}; 
	
	@Override
	public String toString() {
		return new StringBuilder(method.toGenericString()).append(", D=").append(distance).toString();
	}
}
