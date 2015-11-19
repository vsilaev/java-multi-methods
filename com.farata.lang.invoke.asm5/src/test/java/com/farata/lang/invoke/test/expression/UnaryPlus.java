package com.farata.lang.invoke.test.expression;

public class UnaryPlus extends UnaryOperator {
	
	public UnaryPlus(final IExpressionNode opernad) {
		super("+", opernad);
	}
}
