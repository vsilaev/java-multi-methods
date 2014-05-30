package com.farata.lang.invoke.core;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class RWLockCacheSupport extends CacheSupport<ReadWriteLock> {
	
	public <K, V> V getOrCreate(final Map<K, V> cache, final K key, final ReadWriteLock rwLock, final ValueFactory<K, V> factory) {
		rwLock.readLock().lock();
		V value;
		try {
			value = cache.get(key);
		} finally {
			rwLock.readLock().unlock();
		}
		
		if (null == value) {
			rwLock.writeLock().lock();
			try {
				// Recheck state because another thread might have
				// acquired write lock and changed state before we did.
				value = cache.get(key);
				if (null == value) {
					value = factory.create(key);
					cache.put(key, value);
				}
			} finally {
				rwLock.writeLock().unlock(); // Unlock write, still hold read
			}
		}
		
		return value;
	}
}
