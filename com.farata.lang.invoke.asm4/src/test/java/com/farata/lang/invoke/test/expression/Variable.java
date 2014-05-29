package com.farata.lang.invoke.test.expression;

public class Variable implements IExpressionNode {
	final public String name;
	
	public Variable(final String name) {
		this.name = name;
	}
}
