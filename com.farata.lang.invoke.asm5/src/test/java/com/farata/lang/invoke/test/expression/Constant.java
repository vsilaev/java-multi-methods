package com.farata.lang.invoke.test.expression;


public class Constant implements IExpressionNode {
	final public double value;
	
	public Constant(final double value) {
		this.value = value;
	}
}
