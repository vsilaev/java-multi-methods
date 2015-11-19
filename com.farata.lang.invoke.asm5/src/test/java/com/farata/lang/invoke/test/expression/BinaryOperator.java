package com.farata.lang.invoke.test.expression;

import com.farata.lang.invoke.test.expression.IExpressionNode;

abstract public class BinaryOperator implements IExpressionNode {
	final public String name;
	final public IExpressionNode loperand;
	final public IExpressionNode roperand;
	
	public BinaryOperator(final String name, final IExpressionNode loperand, final IExpressionNode roperand) {
		this.name = name;
		this.loperand = loperand;
		this.roperand = roperand;
	}
}
