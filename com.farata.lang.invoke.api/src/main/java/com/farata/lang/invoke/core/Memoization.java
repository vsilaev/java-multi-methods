package com.farata.lang.invoke.core;

public interface Memoization<K, V> {
	public static interface ValueProducer<V> {
		abstract public V create();
	}
	
	abstract public V get(K key, ValueProducer<V> producer);
}
