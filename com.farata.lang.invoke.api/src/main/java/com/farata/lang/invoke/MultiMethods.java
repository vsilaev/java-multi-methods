package com.farata.lang.invoke;

import java.util.EnumSet;
import java.util.ServiceLoader;

import com.farata.lang.invoke.core.MultiMethodFactoryCache;
import com.farata.lang.invoke.spi.MultiMethodCompiler;
import com.farata.lang.invoke.spi.MultiMethodFactory;
import com.farata.lang.invoke.util.MethodModifier;
import com.farata.lang.invoke.util.ReflectiveMultimethodsCollector;

public class MultiMethods {
	
	public static <T, O> T create(final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		return create(samInterface, null, delegateClass, methodsCollector);
	}
	
	@SuppressWarnings("unchecked")
	public static <T, O> T create(Class<T> samInterface, O delegate, MultiMethodsCollector methodsCollector) {
		return create(samInterface, delegate, (Class<O>)delegate.getClass(), methodsCollector);
	}
	
	protected static <T, O> T create(final Class<T> samInterface, final O delegate, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		final MultiMethodFactory<T, O> factory = resolveDispatcherFactory(samInterface, delegateClass, methodsCollector);
		return factory.create(delegate);
	}	
	
	public static MultiMethodsCollector publicMethodsByName(final String methodName) {
		return new ReflectiveMultimethodsCollector(methodName);
	}
	
	public static MultiMethodsCollector publicInstanceMethodsByName(final String methodName) {
		return new ReflectiveMultimethodsCollector(methodName, EnumSet.of(MethodModifier.PUBLIC, MethodModifier.INSTANCE));
	}

	public static MultiMethodsCollector publicStaticMethodsByName(final String methodName) {
		return new ReflectiveMultimethodsCollector(methodName, EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC));
	}
	
	private static <T, O> MultiMethodFactory<T, O> resolveDispatcherFactory(final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		return MultiMethodFactoryCache.resolveFactory(COMPILER, samInterface, delegateClass, methodsCollector);
	}

	final private static MultiMethodCompiler COMPILER;
	static {
		MultiMethodCompiler installedCompiler = null;
		for (final MultiMethodCompiler compiler : ServiceLoader.load(MultiMethodCompiler.class)) {
			installedCompiler = compiler;
			break;
		}
		if (null == installedCompiler) {
			throw new IllegalStateException("No MultiMethod compiler installed");
		} else {
			COMPILER = installedCompiler;
		}
	}
}
