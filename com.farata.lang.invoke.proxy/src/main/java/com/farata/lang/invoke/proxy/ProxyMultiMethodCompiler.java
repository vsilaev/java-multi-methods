package com.farata.lang.invoke.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import com.farata.lang.invoke.spi.AbstractMultiMethodCompiler;
import com.farata.lang.invoke.spi.IntrospectionUtils;
import com.farata.lang.invoke.spi.MultiMethodFactory;

public class ProxyMultiMethodCompiler extends AbstractMultiMethodCompiler {
	
	protected <T,O> MultiMethodFactory<T, O> compileInternal(final Class<T> interfaceClass, final Method interfaceMethod, final Class<O> delegateClass, final List<Method> implementationMethods) {
		@SuppressWarnings("unchecked")
		final Class<T> proxyClass = (Class<T>)Proxy.getProxyClass(IntrospectionUtils.classLoaderOf(delegateClass), interfaceClass, ProxyMultiMethodDispatch.class);

		for (final Method m : implementationMethods) {
			m.setAccessible(true);
		}
		
		final Constructor<T> proxyConstructor;
		try {
			proxyConstructor = proxyClass.getConstructor(InvocationHandler.class);
			proxyConstructor.setAccessible(true);
		} catch (final NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
		
		return new ProxyMultiMethodFactory<T, O>(proxyConstructor, implementationMethods);
	}
	
	public static interface ProxyMultiMethodDispatch {};
}
