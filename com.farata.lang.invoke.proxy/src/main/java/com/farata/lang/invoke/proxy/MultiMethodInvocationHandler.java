package com.farata.lang.invoke.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import com.farata.lang.invoke.MultiMethodException;
import com.farata.lang.invoke.spi.IntrospectionUtils;

public class MultiMethodInvocationHandler implements InvocationHandler {

	final private Object delegate;
	final private Collection<Method> methods;
	
	@SuppressWarnings("unused")
	final private Object factoryReference;
	
	public MultiMethodInvocationHandler(final Object delegate, final Collection<Method> methods, final Object factoryReference) {
		this.delegate = delegate;
		this.methods = methods;
		
		this.factoryReference = factoryReference;
	}

	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if (Object.class == method.getDeclaringClass()) {
			return method.invoke(this, args);
		}

		final Class<?>[] actualParameterTypes = new Class<?>[args.length];
		for (int i = args.length - 1; i >= 0; i--) {
			final Object arg = args[i];
			actualParameterTypes[i] = arg == null ? null : arg.getClass();
		}
		
		Method best = null;
		int minDistance = Integer.MAX_VALUE;
		
		for (final Method m : methods) {
			final Class<?>[] formalParameterTypes = m.getParameterTypes();
			if (formalParameterTypes.length != actualParameterTypes.length) {
				throw new IllegalArgumentException();
			}
			
			int methodDistance = 0;
			
			for (int i = formalParameterTypes.length - 1; i >= 0; i-- ) {
				final Class<?> parameterType = formalParameterTypes[i];
				final Class<?> argType = actualParameterTypes[i];
				int parameterDistance = parameterType.isPrimitive() || argType == null ? 0 : IntrospectionUtils.inheritanceDistance(argType, parameterType);
				if (parameterDistance < 0) {
					methodDistance = -1;
					break;
				}
				methodDistance += parameterDistance;
			}
			
			
			if (methodDistance >= 0) {
				// Match found
				if (minDistance > methodDistance) {
					// Better match
					minDistance = methodDistance;
					best = m;
				} else if (minDistance == methodDistance) {
					// Probably match of same distance, error
					if (best != null) {
						throw new IllegalArgumentException(String.format("Ambiguent methods found:\n%s\n%s", best.toGenericString(), m.toGenericString()));
					}
				} else {
					// Weaker match after stronger one
					if (best != null) {
						break;
					}
				}
			}
		}
		if (best == null) {
			throw new MultiMethodException("Unable to find matching method");
		}
		try {
			return best.invoke(delegate, args);
		} catch (final InvocationTargetException ex) {
			throw ex.getCause();
		}
	}
	
	public String toString() {
		if (null == delegate)
			return super.toString();
		else
			return super.toString() + "[delegate={" + delegate.toString() + "}]";
	}
}
