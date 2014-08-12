package com.farata.lang.invoke.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class HardReferenceMemoization<K, V> implements Memoization<K, V> {
	final private ConcurrentMap<K, Object> producerMutexes = new ConcurrentHashMap<K, Object>();
	final private ConcurrentMap<K, V> valueMap = new ConcurrentHashMap<K, V>();

	public V get(final K key, final ValueProducer<V> producer) {
		V value;

		// Try to get a cached value.
		value = valueMap.get(key);

		if (value != null && isValidValue(value)) {
			// A cached value was found.
			return value;
		}

		final Object mutex = getOrCreateMutex(key);
		synchronized (mutex) {
			try {
				// Double-check after getting mutex
				value = valueMap.get(key);
				if (value == null || !isValidValue(value)) {
					value = producer.create();
					valueMap.put(key, value);
				}
			} finally {
				producerMutexes.remove(key, mutex);
			}
		}

		return value;
	}
	
	protected boolean isValidValue(final V value) {
		return value != null;
	}
	
	public V remove(final K key) {
		final Object mutex = getOrCreateMutex(key);
		synchronized (mutex) {
			try {
				final V value = valueMap.remove(key);
				return value;
			} finally {
				producerMutexes.remove(key, mutex);
			}
		}		
	}

	protected Object getOrCreateMutex(final K key) {
		final Object createdMutex = new byte[0];
		final Object existingMutex = producerMutexes.putIfAbsent(key,
				createdMutex);

		if (existingMutex != null) {
			return existingMutex;
		} else {
			return createdMutex;
		}
	}

}
