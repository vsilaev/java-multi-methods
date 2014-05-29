package com.farata.lang.invoke.test.expression;

public class Subtraction extends BinaryOperator {
	
	public Subtraction(final IExpressionNode loperand, final IExpressionNode roperand) {
		super("-", loperand, roperand);
	}
}
