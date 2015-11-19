package com.farata.lang.invoke.test.expression;

public class UnaryMinus extends UnaryOperator {
	
	public UnaryMinus(final IExpressionNode opernad) {
		super("-", opernad);
	}
}
