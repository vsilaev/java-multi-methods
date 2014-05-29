package com.farata.lang.invoke.test.expression;

public class Multiplication extends BinaryOperator {
	
	public Multiplication(final IExpressionNode loperand, final IExpressionNode roperand) {
		super("*", loperand, roperand);
	}
}
