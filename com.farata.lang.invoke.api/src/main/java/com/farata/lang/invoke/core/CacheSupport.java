package com.farata.lang.invoke.core;

import java.util.Map;

abstract public class CacheSupport<L> {

	abstract public static class ValueFactory<K, V> {
		abstract public V create(K key);
	}

	abstract public <K, V> V getOrCreate(final Map<K, V> cache, final K key, final L lock, final ValueFactory<K, V> factory);
}
