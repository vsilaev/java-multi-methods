package com.farata.lang.invoke.spi;

import com.farata.lang.invoke.MultiMethodsCollector;

public interface MultiMethodCompiler {
	abstract public <T,O> MultiMethodFactory<T, O> compile(Class<T> samInterface, Class<O> delegateClass, MultiMethodsCollector methodsCollector);
}
