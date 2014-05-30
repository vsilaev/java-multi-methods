package com.farata.lang.invoke.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HardReferenceMemoization<K, V> implements Memoization<K, V> {
	final private ConcurrentMap<K, Object> producerMutexes = new ConcurrentHashMap<K, Object>();
	final private ConcurrentMap<K, Entry<K, V>> valueMap = new ConcurrentHashMap<K, Entry<K, V>>();

	public V get(final K key, final ValueProducer<V> producer) {
		Entry<K, V> entry;

		// Try to get a cached value.
		entry = valueMap.get(key);

		if (entry != null) {
			// A cached value was found.
			return entry.value;
		}

		final Object mutex = getOrCreateMutex(key);
		synchronized (mutex) {
			try {
				// Double-check after getting mutex
				entry = valueMap.get(key);
				if (entry == null) {
					final V value = producer.create();
					entry = new Entry<K, V>(key, value);
					valueMap.put(key, entry);
				}
			} finally {
				producerMutexes.remove(key, mutex);
			}
		}

		return (entry != null) ? entry.value : null;
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

	static class Entry<K, V> {
		final K key;
		final V value;

		Entry(final K key, final V value) {
			this.key = key;
			this.value = value;
		}
	}
}
