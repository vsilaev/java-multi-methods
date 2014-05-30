package com.farata.lang.invoke.core;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.farata.lang.invoke.MultiMethodsCollector;
import com.farata.lang.invoke.spi.MultiMethodCompiler;
import com.farata.lang.invoke.spi.MultiMethodFactory;

public class MultiMethodFactoryCache {
	
	public static <T, O> MultiMethodFactory<T, O> resolveFactory(final MultiMethodCompiler compiler, final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		final CachedEntryBySamInterface byInterface = CACHE_SUPPORT.getOrCreate(ROOT.value, samInterface, ROOT.lock, CREATE_ENTRY_PER_SAM_INTERFACE_CACHE);
		final CachedEntryByDelegateClass byDelegate = CACHE_SUPPORT.getOrCreate(byInterface.value, delegateClass, byInterface.lock, CREATE_ENTRY_PER_DELEGATE_CLASS_CACHE);
		final MultiMethodFactory<?, ?> factory = CACHE_SUPPORT.getOrCreate(
			byDelegate.value, methodsCollector, byDelegate.lock, 
			new CompileMultiMethodFactory() {
				public MultiMethodFactory<?, ?> create(final MultiMethodsCollector ignore) {
					return compiler.compile(samInterface, delegateClass, methodsCollector);
				}
			}
		);
		
		@SuppressWarnings("unchecked")
		final MultiMethodFactory<T, O> result = (MultiMethodFactory<T, O>)factory;
		return result;
	}

	final private static RootEntry ROOT = new RootEntry();
	final private static RWLockCacheSupport CACHE_SUPPORT = new RWLockCacheSupport();

	final static CacheSupport.ValueFactory<Class<?>, CachedEntryBySamInterface> CREATE_ENTRY_PER_SAM_INTERFACE_CACHE = 
		new CacheSupport.ValueFactory<Class<?>, CachedEntryBySamInterface>() {
			@Override
			public CachedEntryBySamInterface create(final Class<?> key) {
				return new CachedEntryBySamInterface();
			}
		};

	
	final static CacheSupport.ValueFactory<Class<?>, CachedEntryByDelegateClass> CREATE_ENTRY_PER_DELEGATE_CLASS_CACHE = 
		new CacheSupport.ValueFactory<Class<?>, CachedEntryByDelegateClass>() {
			@Override
			public CachedEntryByDelegateClass create(final Class<?> key) {
				return new CachedEntryByDelegateClass();
			}
		};

	
	static class CachedEntryByDelegateClass extends GuardedValue<ReadWriteLock, Map<MultiMethodsCollector,MultiMethodFactory<?,?>>> {
		public CachedEntryByDelegateClass() {
			super(new ReentrantReadWriteLock(), new HashMap<MultiMethodsCollector, MultiMethodFactory<?,?>>());
		}
	}
	
	static class CachedEntryBySamInterface extends GuardedValue<ReadWriteLock, Map<Class<?>, CachedEntryByDelegateClass>> {
		public CachedEntryBySamInterface() {
			super(new ReentrantReadWriteLock(), new WeakHashMap<Class<?>, CachedEntryByDelegateClass>());
		}
	}
	
	static class RootEntry extends GuardedValue<ReadWriteLock, Map<Class<?>, CachedEntryBySamInterface>> {
		public RootEntry() {
			super(new ReentrantReadWriteLock(), new WeakHashMap<Class<?>, CachedEntryBySamInterface>());
		}
		
	}
	
	abstract static class CompileMultiMethodFactory extends CacheSupport.ValueFactory<MultiMethodsCollector, MultiMethodFactory<?, ?>> {}
}
