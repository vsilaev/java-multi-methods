package com.farata.lang.invoke.spi;

public interface MultiMethodFactory<T, O> {
	abstract public T create(O delegate);
}
