package com.farata.lang.invoke.core;

import java.util.Map;
import java.util.concurrent.locks.Lock;

public class ExclusiveLockCacheSupport extends CacheSupport<Lock> {
	public <K, V> V getOrCreate(final Map<K, V> cache, final K key, final Lock lock, final ValueFactory<K, V> factory) {
		lock.lock();
		V value;
		try {
			value = cache.get(key);
			if (null == value) {
				value = factory.create(key);
				cache.put(key, value);
			}
		} finally {
			lock.unlock();
		}
		
		return value;
	}
}
