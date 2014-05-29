package com.farata.lang.invoke.spi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.farata.lang.invoke.MultiMethodEntry;
import com.farata.lang.invoke.MultiMethodsCollector;

abstract public class AbstractMultiMethodCompiler implements MultiMethodCompiler {
	
	final public <T,O> MultiMethodFactory<T, O> compile(final Class<T> samInterface, final Class<O> delegateClass, final MultiMethodsCollector methodsCollector) {
		final Method interfaceMethod = IntrospectionUtils.singleAbstractMethodOf(samInterface);
		final List<MultiMethodEntry> entries = methodsCollector.collect(interfaceMethod, delegateClass);
		Collections.sort(entries, MultiMethodEntry.COMPARE_BY_DISTANCE);
		final List<Method> implementationMethods = new ArrayList<Method>(entries.size());
		for (final MultiMethodEntry e : entries) {
			implementationMethods.add(e.method);
		}
		return compileInternal(samInterface, interfaceMethod, delegateClass, implementationMethods);
	}
	
	abstract protected <T,O> MultiMethodFactory<T, O> compileInternal(Class<T> interfaceClass, Method interfaceMethod, Class<O> delegateClass, List<Method> implementationMethods);
}
