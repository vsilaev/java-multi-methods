package com.farata.lang.invoke.core;

import java.lang.ref.WeakReference;

import com.farata.lang.invoke.MultiMethodsCollector;
import com.farata.lang.invoke.spi.MultiMethodCompiler;
import com.farata.lang.invoke.spi.MultiMethodFactory;

public class MultiMethodFactoryCache {
	
	public <T, O> MultiMethodFactory<T, O> resolveFactory(final MultiMethodCompiler compiler, final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		final MemoizationBySamInterface byInterface = root.get(samInterface, createEntryPerSamInterfaceCache);
		final MemoizationByDelegateClass byDelegate = byInterface.get(delegateClass, createEntryPerDelegateClassCache);
		final MultiMethodFactory<?, ?>[] factoryHardReference = {null}; 
		final WeakReference<MultiMethodFactory<?, ?>> factoryWeakReference = byDelegate.get(methodsCollector,  
			new CompileMultiMethodFactory() {
				public WeakReference<MultiMethodFactory<?, ?>> create() {
					factoryHardReference[0] = compiler.compile(samInterface, delegateClass, methodsCollector);
					return new WeakReference<MultiMethodFactory<?,?>>(factoryHardReference[0]);
				}
			}
		);
		
		final MultiMethodFactory<?, ?> factory = factoryWeakReference.get();
		if (null == factory) {
			// With extremely low probability we can get weak reference 
			// that was cleared right away after being returned from cache.
			// Recursive call will be always successful while we keep
			// hard reference to factory during creation
			return resolveFactory(compiler, samInterface, delegateClass, methodsCollector);
		}
		
		@SuppressWarnings("unchecked")
		final MultiMethodFactory<T, O> result = (MultiMethodFactory<T, O>)factory;
		return result;
	}

	final protected RootEntry root = new RootEntry();

	final protected Memoization.ValueProducer<MemoizationBySamInterface> createEntryPerSamInterfaceCache = 
		new Memoization.ValueProducer<MemoizationBySamInterface>() {
			public MemoizationBySamInterface create() {
				return new MemoizationBySamInterface();
			}
		};

	final protected Memoization.ValueProducer<MemoizationByDelegateClass> createEntryPerDelegateClassCache = 
		new Memoization.ValueProducer<MemoizationByDelegateClass>() {
			public MemoizationByDelegateClass create() {
				return new MemoizationByDelegateClass();
			}
		};

	
	protected static class MemoizationByDelegateClass extends HardReferenceMemoization<MultiMethodsCollector, WeakReference<MultiMethodFactory<?,?>>> {
		protected boolean isValidValue(final  WeakReference<MultiMethodFactory<?,?>> value) {
			return value != null && value.get() != null;
		}
	}
	
	protected static class MemoizationBySamInterface extends WeakReferenceMemoization<Class<?>, MemoizationByDelegateClass> {}
	
	protected static class RootEntry extends WeakReferenceMemoization<Class<?>, MemoizationBySamInterface> {}
	
	abstract static class CompileMultiMethodFactory implements Memoization.ValueProducer<WeakReference<MultiMethodFactory<?, ?>>> {}
}
