package com.farata.lang.invoke.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.farata.lang.invoke.spi.MultiMethodFactory;

class ProxyMultiMethodFactory<T, O> implements MultiMethodFactory<T, O> {

	final private Constructor<T> constructor;
	final private List<Method> methods;
	
	ProxyMultiMethodFactory(final Constructor<T> constructor, final List<Method> methods) {
		this.constructor = constructor;
		this.methods = methods;
	}

	public T create(final O delegate) {
		try {
			return constructor.newInstance(new MultiMethodInvocationHandler(delegate, methods));
		} catch (final InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (final IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (final IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (final InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

}
