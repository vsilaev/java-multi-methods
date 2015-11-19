package com.farata.lang.invoke.asm5;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.farata.lang.invoke.spi.MultiMethodFactory;

class AsmMultiMethodFactory<T, O> implements MultiMethodFactory<T, O> {

	final private Constructor<T> constructor;
	
	AsmMultiMethodFactory(final Constructor<T> constructor) {
		this.constructor  = constructor;
	}
	
	public T create(final O delegate) {
		try {
			return constructor.newInstance(delegate, this);
		} catch (InstantiationException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

}
