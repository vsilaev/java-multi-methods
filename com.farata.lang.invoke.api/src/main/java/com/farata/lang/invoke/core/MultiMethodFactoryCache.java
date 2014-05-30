package com.farata.lang.invoke.core;

import com.farata.lang.invoke.MultiMethodsCollector;
import com.farata.lang.invoke.spi.MultiMethodCompiler;
import com.farata.lang.invoke.spi.MultiMethodFactory;

public class MultiMethodFactoryCache {
	
	public <T, O> MultiMethodFactory<T, O> resolveFactory(final MultiMethodCompiler compiler, final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		final MemoizationBySamInterface byInterface = root.get(samInterface, createEntryPerSamInterfaceCache);
		final MemoizationByDelegateClass byDelegate = byInterface.get(delegateClass, createEntryPerDelegateClassCache);
		final MultiMethodFactory<?, ?> factory = byDelegate.get(methodsCollector,  
			new CompileMultiMethodFactory() {
				public MultiMethodFactory<?, ?> create() {
					return compiler.compile(samInterface, delegateClass, methodsCollector);
				}
			}
		);
		
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

	
	protected static class MemoizationByDelegateClass extends HardReferenceMemoization<MultiMethodsCollector, MultiMethodFactory<?,?>> {}
	
	protected static class MemoizationBySamInterface extends WeakReferenceMemoization<Class<?>, MemoizationByDelegateClass> {}
	
	protected static class RootEntry extends WeakReferenceMemoization<Class<?>, MemoizationBySamInterface> {}
	
	abstract static class CompileMultiMethodFactory implements Memoization.ValueProducer<MultiMethodFactory<?, ?>> {}
}
