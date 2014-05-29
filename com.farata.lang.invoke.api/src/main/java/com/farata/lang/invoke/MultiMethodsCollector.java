package com.farata.lang.invoke;

import java.lang.reflect.Method;
import java.util.List;

public interface MultiMethodsCollector {
	abstract public List<MultiMethodEntry> collect(Method interfaceMethod, Class<?> implementationClass);
}
