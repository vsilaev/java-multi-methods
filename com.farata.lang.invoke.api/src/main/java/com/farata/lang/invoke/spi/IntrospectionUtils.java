package com.farata.lang.invoke.spi;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class IntrospectionUtils {
	
	
	public static Method singleAbstractMethodOf(final Class<?> samInterface) {
		if (null == samInterface) {
			throw new IllegalArgumentException("Single Abstract Method interface class may not be null");
		}
		
		if (!samInterface.isInterface()) {
			throw new IllegalArgumentException(String.format("Class %s passed as Single Anstract Method interface is not an interface", samInterface.getName()));
		}
		
		final Method[] methods = samInterface.getMethods();
		switch (methods.length) {
			case 0:
				throw new IllegalArgumentException(String.format("Interface %s has no methods declared", samInterface.getName()));
			case 1:
				break;
			default:
				throw new IllegalArgumentException(String.format("Interface %s must declare single method but %d were found", samInterface.getName(), methods.length));
		}
		
		return methods[0];
	}
	
	public static ClassLoader classLoaderOf(final Class<?> clazz) {
		final ClassLoader exactClassLoader = clazz.getClassLoader();
		return null == exactClassLoader ? ClassLoader.getSystemClassLoader() : exactClassLoader;
	}
	
	public static int inheritanceDistance(final Class<?> from, final Class<?> to) {
		if (null == from || null == to)
			return -1;
		
		if (from == to) {
			// Equals
			return 0;
		}
		
		if (Modifier.isFinal(to.getModifiers())) {
			// No exact equality and "to" can't be extended
			return -1;
		}
		
		final Class<?> fromSuper = from.getSuperclass();
		int result = inheritanceDistance(fromSuper, to);
		
		if (result >= 0) {
			// Found in superclasses, so increase depth and return result
			// Cache positive result here
			return result + 1;
		}
		
		if (to.isInterface()) {
			result = maxInterfacesInheritanceDistance(from, to);
			if (result >= 0) {
				// Cache positive result here				
				return result + 1;
			} else {
				// Cache negative result here
				return -1;
			}
		} else {
			// Cache negative result here			
			return -1;
		}
	}
	
	protected static int maxInterfacesInheritanceDistance(final Class<?> from, final Class<?> to) {
		int max = -1;
		for (final Class<?> superInterface : from.getInterfaces()) {
			final int distance = inheritanceDistance(superInterface, to/*, visited*/);
			
			if (distance >= 0) {
				// Cache positive result here
				if (distance > max)
					max = distance;
			} else {
				// Cache negative result here
			}
		}
		return max;
	}
}
