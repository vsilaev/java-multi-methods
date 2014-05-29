package com.farata.lang.invoke.asm3;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.farata.lang.invoke.spi.AbstractMultiMethodCompiler;
import com.farata.lang.invoke.spi.IntrospectionUtils;
import com.farata.lang.invoke.spi.MultiMethodFactory;

public class AsmMultiMethodCompiler extends AbstractMultiMethodCompiler {

	protected <T,O> MultiMethodFactory<T, O> compileInternal(final Class<T> interfaceClass, final Method interfaceMethod, final Class<O> delegateClass, final List<Method> implementationMethods) {
		final byte[] classDef = new MultiMethodDispatcherClassGenerator().generateClass(interfaceClass, interfaceMethod, delegateClass, implementationMethods);
		final ClassLoader classLoader = IntrospectionUtils.classLoaderOf(delegateClass);
		try {
			@SuppressWarnings("unchecked")
			final Class<T> targetClass = (Class<T>)DEFINE_CLASS.invoke(classLoader, null, classDef, 0, classDef.length);
			final Constructor<T> dispatcherConstructor = targetClass.getConstructor(delegateClass);
			dispatcherConstructor.setAccessible(true);
			return new AsmMultiMethodFactory<T, O>(dispatcherConstructor);
		} catch (final IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (final InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException(ex); 
		}
		
	}
	
	final public static Method DEFINE_CLASS;
	static {
		try {
			DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		} catch (final NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		} 
		DEFINE_CLASS.setAccessible(true);
	}
}
