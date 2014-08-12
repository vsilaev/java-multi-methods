package com.farata.lang.invoke.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class WeakReferenceMemoization<K, V> implements Memoization<K, V> {
	final private ConcurrentMap<Reference<K>, Object> producerMutexes = new ConcurrentHashMap<Reference<K>, Object>();
	final private ConcurrentMap<Reference<K>, V> valueMap = new ConcurrentHashMap<Reference<K>, V>();
	final private ReferenceQueue<K> queue = new ReferenceQueue<K>();

	public V get(final K key, final ValueProducer<V> producer) {
		expungeStaleEntries();

		final Reference<K> lookupKeyRef = new KeyReference<K>(key);
		V value;

		// Try to get a cached value.
		value = valueMap.get(lookupKeyRef);

		if (value != null) {
			// A cached value was found.
			return value;
		}

		final Object mutex = getOrCreateMutex(lookupKeyRef);
		synchronized (mutex) {
			try {
				// Double-check after getting mutex
				value = valueMap.get(lookupKeyRef);
				if (value == null) {
					value = producer.create();
					final Reference<K> actualKeyRef = new KeyReference<K>(key, queue);
					valueMap.put(actualKeyRef, value);
				}
			} finally {
				producerMutexes.remove(lookupKeyRef, mutex);
			}
		}

		return value;
	}
	
	public V remove(final K key) {
		final Reference<K> lookupKeyRef = new KeyReference<K>(key);
		final Object mutex = getOrCreateMutex(lookupKeyRef);
		synchronized (mutex) {
			try {
				final V value = valueMap.remove(lookupKeyRef);
				return value;
			} finally {
				producerMutexes.remove(lookupKeyRef, mutex);
			}
		}		
	}

	protected Object getOrCreateMutex(final Reference<K> keyRef) {
		final Object createdMutex = new byte[0];
		final Object existingMutex = producerMutexes.putIfAbsent(keyRef,
				createdMutex);

		if (existingMutex != null) {
			return existingMutex;
		} else {
			return createdMutex;
		}
	}
	
	private void expungeStaleEntries() {
		for (Reference<? extends K> ref; (ref = queue.poll()) != null;) {
			@SuppressWarnings("unchecked")
			final Reference<K> keyRef = (Reference<K>) ref;
			// keyRef now is equal only to itself while referent is cleared already
			// so it's safe to remove it without ceremony (like getOrCreateMutex(keyRef) usage)
			valueMap.remove(keyRef);
		}
	}

	static class KeyReference<K> extends WeakReference<K> {
		final private int referentHashCode;
		
		KeyReference(final K key) {
			this(key, null);
		}

		KeyReference(final K key, final ReferenceQueue<K> queue) {
			super(key, queue);
			referentHashCode = key == null ? 0 : key.hashCode();
		}

		public int hashCode() {
			return referentHashCode;
		}

		public boolean equals(final Object other) {
			if (this == other)
				return true;
			if (null == other || other.getClass() != KeyReference.class)
				return false;
			final Object r1 = this.get();
			final Object r2 = ((KeyReference<?>) other).get();
			return null == r1 ? null == r2 : r1.equals(r2);
		}
	}

}
