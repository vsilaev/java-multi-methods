package com.farata.lang.invoke.test.expression;

public class Division extends BinaryOperator {
	public Division(final IExpressionNode loperand, final IExpressionNode roperand) {
		super("/", loperand, roperand);
	}
}
