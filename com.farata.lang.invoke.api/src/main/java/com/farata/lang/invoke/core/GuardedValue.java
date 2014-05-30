package com.farata.lang.invoke.core;

public class GuardedValue<L,V> {
	final public L lock;
	final public V value;
	
	public GuardedValue(final L lock, final V value) {
		this.lock = lock;
		this.value = value;
	}
}
