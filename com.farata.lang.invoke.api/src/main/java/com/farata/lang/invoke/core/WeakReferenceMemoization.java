package com.farata.lang.invoke.core;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class WeakReferenceMemoization<K, V> implements Memoization<K, V> {
	final private ConcurrentMap<Reference<K>, Object> producerMutexes = new ConcurrentHashMap<Reference<K>, Object>();
	final private ConcurrentMap<Reference<K>, Entry<K, V>> valueMap = new ConcurrentHashMap<Reference<K>, Entry<K, V>>();
	final private ReferenceQueue<K> queue = new ReferenceQueue<K>();

	public V get(final K key, final ValueProducer<V> producer) {
		expungeStaleEntries();

		final Reference<K> lookupKeyRef = new KeyReference<K>(key);
		Entry<K, V> entry;

		// Try to get a cached value.
		entry = valueMap.get(lookupKeyRef);

		if (entry != null) {
			// A cached value was found.
			return entry.value;
		}

		final Object mutex = getOrCreateMutex(lookupKeyRef);
		synchronized (mutex) {
			try {
				// Double-check after getting mutex
				entry = valueMap.get(lookupKeyRef);
				if (entry == null) {
					final V value = producer.create();
					final Reference<K> actualKeyRef = new KeyReference<K>(key,
							queue);
					entry = new Entry<K, V>(actualKeyRef, value);
					valueMap.put(actualKeyRef, entry);
				}
			} finally {
				producerMutexes.remove(lookupKeyRef, mutex);
			}
		}

		return (entry != null) ? entry.value : null;
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
			final Object mutex = getOrCreateMutex(keyRef);
			synchronized (mutex) {
				final Entry<K, V> entry = valueMap.get(keyRef);
				if (null != entry && entry.keyReference == keyRef) {
					// Remove only if key is still equals by reference
					valueMap.remove(keyRef);
				}
				producerMutexes.remove(keyRef, mutex);
			}
		}
	}

	static class KeyReference<K> extends WeakReference<K> {
		KeyReference(final K key) {
			super(key);
		}

		KeyReference(final K key, final ReferenceQueue<K> queue) {
			super(key, queue);
		}

		public int hashCode() {
			final K referent = get();
			return null == referent ? 0 : referent.hashCode();
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

	static class Entry<K, V> {
		final Reference<K> keyReference;
		final V value;

		Entry(final Reference<K> keyReference, final V value) {
			this.keyReference = keyReference;
			this.value = value;
		}
	}
}
