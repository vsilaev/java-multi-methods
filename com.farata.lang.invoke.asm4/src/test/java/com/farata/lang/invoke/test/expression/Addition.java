package com.farata.lang.invoke.test.expression;

public class Addition extends BinaryOperator {
	
	public Addition(final IExpressionNode loperand, final IExpressionNode roperand) {
		super("+", loperand, roperand);
	}
}
