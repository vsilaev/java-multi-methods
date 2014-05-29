package com.farata.lang.invoke.test.expression;

public class Negation extends UnaryOperator {
	
	public Negation(final IExpressionNode opernad) {
		super("-", opernad);
	}
}
